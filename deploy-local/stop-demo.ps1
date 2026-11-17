# =====================================================================
# 文物藏品管理与溯源系统 · 停止本机演示环境
# 关闭：Spring Boot、Cloudflare 隧道（以及本次启动器拉起的 Nginx）
# 不关闭：MySQL（本机其它项目可能还在用）
# =====================================================================

$ErrorActionPreference = 'Continue'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$StateFile = Join-Path $ScriptDir '.runtime.json'

if (-not (Test-Path $StateFile)) {
    Write-Host '[提示] 没有找到运行记录，可能已经停止。' -ForegroundColor Yellow
    Read-Host '按回车退出' | Out-Null
    exit 0
}

$state = Get-Content $StateFile -Raw | ConvertFrom-Json

Write-Host ''
Write-Host '==> 停止后端与隧道' -ForegroundColor Cyan

foreach ($item in @(
    @{ Name = 'Spring Boot'; Pid = $state.backendPid },
    @{ Name = 'cloudflared'; Pid = $state.cloudflaredPid }
)) {
    if (-not $item.Pid) { continue }
    $proc = Get-Process -Id $item.Pid -ErrorAction SilentlyContinue
    if ($proc) {
        Stop-Process -Id $item.Pid -Force -ErrorAction SilentlyContinue
        Write-Host "    已停止 $($item.Name) (PID $($item.Pid))" -ForegroundColor Green
    } else {
        Write-Host "    $($item.Name) 已不在运行" -ForegroundColor Gray
    }
}

if ($state.startedNginx) {
    $nginxDir = Join-Path (Split-Path -Parent $ScriptDir) 'frontend\deploy\nginx-1.28.0'
    $nginxExe = Join-Path $nginxDir 'nginx.exe'
    if (Test-Path $nginxExe) {
        & $nginxExe -s quit 2>$null
        Write-Host '    已停止 Nginx' -ForegroundColor Green
    }
} else {
    Write-Host '    Nginx 为启动前已运行，保持不动' -ForegroundColor Gray
}

Remove-Item $StateFile -Force -ErrorAction SilentlyContinue

Write-Host ''
Write-Host '演示环境已停止。' -ForegroundColor Green
Write-Host ''
Read-Host '按回车退出' | Out-Null
