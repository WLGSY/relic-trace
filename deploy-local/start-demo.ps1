param([switch]$NonInteractive)

# =====================================================================
# 文物藏品管理与溯源系统 · 本机演示启动器
# 作用：Nginx(80) + Spring Boot(8080) + Cloudflare 隧道 -> 公网 HTTPS 地址
# 用法：双击同目录下的 start-demo.bat
# =====================================================================

$ErrorActionPreference = 'Stop'

# PowerShell 5.1 默认不启用 TLS 1.2，访问 Cloudflare 会报「发送时发生错误」
try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12 } catch { }

$ScriptDir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$Root       = Split-Path -Parent $ScriptDir
$Jar        = Join-Path $Root 'relic-trace-boot\target\relic-trace-boot.jar'
$DistIndex  = Join-Path $Root 'frontend\dist\index.html'
$NginxDir   = Join-Path $Root 'frontend\deploy\nginx-1.28.0'
$CfExe      = Join-Path $ScriptDir 'bin\cloudflared.exe'
$LogDir     = Join-Path $ScriptDir 'logs'
$StateFile  = Join-Path $ScriptDir '.runtime.json'
$BackendLog = Join-Path $LogDir 'backend.log'

function Write-Step($msg) { Write-Host ""; Write-Host "==> $msg" -ForegroundColor Cyan }
function Write-Ok($msg)   { Write-Host "    [OK]   $msg" -ForegroundColor Green }
function Write-Warn($msg) { Write-Host "    [警告] $msg" -ForegroundColor Yellow }
function Fail($msg) {
    Write-Host ""
    Write-Host "[错误] $msg" -ForegroundColor Red
    if (-not $NonInteractive) { Read-Host "按回车退出" | Out-Null }
    exit 1
}

function Test-Port([int]$Port) {
    $client = New-Object Net.Sockets.TcpClient
    try { $client.Connect('127.0.0.1', $Port); return $true }
    catch { return $false }
    finally { $client.Dispose() }
}

# 不用 Start-Process：部分机器环境变量存在大小写重复键会直接抛异常
function Start-Hidden([string]$FileName, [string]$Arguments, [string]$WorkDir) {
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName  = $FileName
    $psi.Arguments = $Arguments
    if ($WorkDir) { $psi.WorkingDirectory = $WorkDir }
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow  = $true
    $psi.WindowStyle     = [System.Diagnostics.ProcessWindowStyle]::Hidden
    return [System.Diagnostics.Process]::Start($psi)
}

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

# ---------------------------------------------------------------- 1 构建产物
Write-Step '检查构建产物'
if (-not (Test-Path $Jar)) {
    Fail "未找到后端 jar：$Jar `n请先在项目根目录执行 mvn clean package -DskipTests"
}
$jarMB = [math]::Round((Get-Item $Jar).Length / 1MB, 1)
Write-Ok "后端 jar $jarMB MB"
if (-not (Test-Path $DistIndex)) {
    Fail "未找到前端产物：$DistIndex `n请先执行 cd frontend 然后 npm run build"
}
Write-Ok '前端 dist 就绪'

# ---------------------------------------------------------------- 2 依赖服务
Write-Step '检查依赖服务'
if (-not (Test-Port 3307)) {
    Fail 'MySQL 未监听 3307 端口。请先启动本机 MySQL 服务（services.msc 里找 MySQL80）'
}
Write-Ok 'MySQL 127.0.0.1:3307 在线'

$startedNginx = $false
if (-not (Test-Port 80)) {
    Write-Warn 'Nginx 未运行，正在启动'
    $nginxExe = Join-Path $NginxDir 'nginx.exe'
    if (-not (Test-Path $nginxExe)) { Fail "未找到 $nginxExe" }
    Start-Hidden $nginxExe '' $NginxDir | Out-Null
    Start-Sleep -Seconds 2
    if (-not (Test-Port 80)) { Fail 'Nginx 启动失败，请手动运行 frontend\deploy\start-nginx.bat 查看报错' }
    $startedNginx = $true
    Write-Ok 'Nginx 80 已启动'
} else {
    Write-Ok 'Nginx 80 在线'
}

# ---------------------------------------------------------------- 3 隧道
# 清理上一次未正常退出的进程，保证脚本可以反复运行
if (Test-Path $StateFile) {
    $old = Get-Content $StateFile -Raw | ConvertFrom-Json
    foreach ($p in @($old.backendPid, $old.cloudflaredPid)) {
        if ($p) { Stop-Process -Id $p -Force -ErrorAction SilentlyContinue }
    }
    Remove-Item $StateFile -Force -ErrorAction SilentlyContinue
    Write-Host '    已清理上次残留进程' -ForegroundColor DarkGray
}

Write-Step '建立 Cloudflare 隧道（约 5-15 秒）'
if (-not (Test-Path $CfExe)) {
    Fail "未找到 $CfExe `n请下载 cloudflared-windows-amd64.exe 放到 deploy-local\bin\ 目录"
}
$cfLog = Join-Path $LogDir 'cloudflared.log'
Remove-Item $cfLog -Force -ErrorAction SilentlyContinue

$cfArgs = 'tunnel --no-autoupdate --url http://127.0.0.1:80 --logfile "' + $cfLog + '"'
$cfProc = Start-Hidden $CfExe $cfArgs $ScriptDir

$publicUrl = $null
for ($i = 1; $i -le 45; $i++) {
    Start-Sleep -Seconds 1
    if (Test-Path $cfLog) {
        $hit = Select-String -Path $cfLog -Pattern 'https://[a-z0-9-]+\.trycloudflare\.com' -ErrorAction SilentlyContinue |
               Select-Object -First 1
        if ($hit) { $publicUrl = $hit.Matches[0].Value; break }
    }
    Write-Host ("    等待隧道地址 {0}s" -f $i)
}
if (-not $publicUrl) {
    Stop-Process -Id $cfProc.Id -Force -ErrorAction SilentlyContinue
    Fail "隧道地址获取超时，请查看 $cfLog"
}
Write-Ok "公网地址 $publicUrl"

# ---------------------------------------------------------------- 4 后端
Write-Step '启动 Spring Boot'
$backendPid = $null
if (Test-Port 8080) {
    Write-Warn '8080 已被占用，复用现有后端（其二维码地址可能仍是旧值）'
} else {
    $env:RELIC_QR_BASE_URL = $publicUrl
    $env:RELIC_UPLOAD_DIR  = (Join-Path $Root 'uploads')
    Remove-Item $BackendLog -Force -ErrorAction SilentlyContinue

    # 显式指定端口：部分环境存在 SERVER__PORT 之类的注入变量会覆盖 application.yml
    $javaArgs = '-Xmx512m -Dfile.encoding=UTF-8 -Dlogging.file.name="' + $BackendLog + '" -jar "' + $Jar + '" --server.port=8080'
    $javaProc = Start-Hidden 'java' $javaArgs $Root
    $backendPid = $javaProc.Id

    $up = $false
    for ($i = 1; $i -le 90; $i++) {
        Start-Sleep -Seconds 1
        if (Test-Port 8080) { $up = $true; break }
        if ($i % 5 -eq 0) { Write-Host ("    等待后端启动 {0}s" -f $i) }
    }
    if (-not $up) {
        Stop-Process -Id $javaProc.Id -Force -ErrorAction SilentlyContinue
        Stop-Process -Id $cfProc.Id -Force -ErrorAction SilentlyContinue
        Fail "后端未在 90 秒内启动，请查看 $BackendLog"
    }
    Write-Ok '后端 8080 已就绪'
}

# ---------------------------------------------------------------- 5 自检
# 这台机器上 .NET 的 Invoke-RestMethod 过不了 TLS（代理叠加），改用系统自带 curl.exe
$env:HTTP_PROXY = '';  $env:HTTPS_PROXY = ''
$env:http_proxy = '';  $env:https_proxy = ''

$Curl = Join-Path $env:SystemRoot 'System32\curl.exe'
if (-not (Test-Path $Curl)) { $Curl = 'curl.exe' }

Write-Step '公网链路自检'
$apiOk = $false
$probe = (& $Curl -s --noproxy '*' -m 25 "$publicUrl/api/relics/public/highlights") -join ''
if ($probe -match '"code"\s*:\s*200') { $apiOk = $true }
if ($apiOk) {
    Write-Ok '页面与 /api 反向代理均正常'
} else {
    Write-Warn '接口自检未通过，若域名刚生成请等 10 秒后刷新页面重试'
}

# ---------------------------------------------------------------- 6 重建二维码
Write-Step '重建二维码（指向新的公网地址）'
try {
    $loginFile = Join-Path $env:TEMP 'relic-login.json'
    Set-Content -Path $loginFile -Value '{"loginName":"admin","loginPassword":"123456"}' -Encoding ASCII

    # 注意：不要用 ConvertFrom-Json 解析，控制台按 GBK 解码 curl 的 UTF-8 输出会破坏中文字段
    $loginRaw = (& $Curl -s --noproxy '*' -m 25 -X POST -H 'Content-Type: application/json' `
                 --data-binary "@$loginFile" "$publicUrl/api/auth/login") -join ''
    Remove-Item $loginFile -Force -ErrorAction SilentlyContinue

    $token = $null
    if ($loginRaw -match '"token"\s*:\s*"([A-Za-z0-9._\-]+)"') { $token = $matches[1] }

    if ($token) {
        $rbRaw = (& $Curl -s --noproxy '*' -m 60 -X POST -H "Authorization: Bearer $token" `
                  "$publicUrl/api/relics/qrcodes/rebuild") -join ''
        if ($rbRaw -match '"code"\s*:\s*200') {
            $count = '全部'
            if ($rbRaw -match '"data"\s*:\s*(\d+)') { $count = $matches[1] }
            Write-Ok ("已重建 {0} 张二维码" -f $count)
        } else {
            Write-Warn '二维码重建接口返回异常，可登录后台手动点「批量重建」'
        }
    } else {
        Write-Warn '登录未返回 token，跳过二维码重建'
    }
} catch {
    Write-Warn "二维码重建失败（可登录后台手动点「批量重建」）：$($_.Exception.Message)"
}

# ---------------------------------------------------------------- 7 状态落盘
$state = @{
    publicUrl      = $publicUrl
    cloudflaredPid = $cfProc.Id
    backendPid     = $backendPid
    startedNginx   = $startedNginx
    startedAt      = (Get-Date).ToString('s')
}
$state | ConvertTo-Json | Set-Content -Path $StateFile -Encoding utf8

# ---------------------------------------------------------------- 8 汇总
Write-Host ''
Write-Host '==========================================================' -ForegroundColor Green
Write-Host '  演示环境已就绪' -ForegroundColor Green
Write-Host '==========================================================' -ForegroundColor Green
Write-Host ''
Write-Host "  公网地址   $publicUrl" -ForegroundColor White
Write-Host "  本机地址   http://localhost"
Write-Host ''
Write-Host '  演示账号   admin   / 123456   系统管理员'
Write-Host '             zhangly / 123456   录入员'
Write-Host '             lishen  / 123456   审核员'
Write-Host ''
Write-Host '  免费域名每次重启都会变，二维码已自动重建。' -ForegroundColor Yellow
Write-Host '  演示结束后运行 stop-demo.bat 释放进程。' -ForegroundColor Yellow
Write-Host ''
Write-Host "  日志目录   $LogDir" -ForegroundColor DarkGray
Write-Host ''
if (-not $NonInteractive) { Read-Host '按回车关闭本窗口（服务继续在后台运行）' | Out-Null }
