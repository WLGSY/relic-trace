<template>
  <div class="wrap">
    <PageHeader title="扫码溯源" index="03" subtitle="扫描藏品二维码，查看完整流转经历" />

    <div class="scan-wrap">
      <div class="scan-card">
        <div class="scan-seal">溯</div>
        <h2 class="detail-title" style="font-size: 28px">扫描藏品二维码</h2>
        <p class="muted" style="margin-top: 8px">二维码由编目建档时生成，内嵌藏品唯一编号</p>

        <div class="scan-frame" :class="{ 'is-live': cameraOn }">
          <video
            v-show="cameraOn"
            ref="videoEl"
            class="scan-video"
            playsinline
            muted
            aria-label="摄像头预览"
          />
          <canvas ref="canvasEl" class="scan-canvas" aria-hidden="true" />
          <span v-if="!cameraOn" class="scan-beam" />
          <span v-if="!cameraOn" class="mono" style="letter-spacing: 0.16em">CAMERA PREVIEW</span>
          <span v-if="cameraOn" class="scan-hint">{{ cameraHint }}</span>
        </div>

        <button class="btn-solid" style="width: 100%" @click="toggleCamera">
          {{ cameraOn ? '停止扫描' : '开启摄像头扫码' }}
        </button>

        <div class="scan-sub-actions">
          <button class="link-btn" :disabled="scanning" @click="simulateScan">
            {{ scanning ? '识别中…' : '演示模式：随机取一件藏品' }}
          </button>
        </div>

        <div class="divider-text"><span>或手动输入藏品编号</span></div>

        <div class="row" style="gap: 8px">
          <el-input v-model="code" placeholder="如 TC-0001" size="large" @keyup.enter="queryByCode" />
          <el-button type="primary" size="large" :loading="querying" @click="queryByCode">查询</el-button>
        </div>

        <p v-if="error" class="error-text">{{ error }}</p>

        <div class="scan-tips">
          <div class="tip-row"><span class="mono">01</span><span>手机扫码将直接打开该藏品的溯源详情页，无需手动输入</span></div>
          <div class="tip-row"><span class="mono">02</span><span>摄像头识别需要浏览器授权；页面须运行在 https:// 或 localhost 下（手机请用 https 访问电脑 IP）</span></div>
          <div class="tip-row"><span class="mono">03</span><span>若提示"二维码无效"，说明该藏品已注销或编号有误</span></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import jsQR from 'jsqr'
import PageHeader from '@/components/PageHeader.vue'
import { getHighlights, getRelicByCode } from '@/api/relic'

/**
 * 扫码溯源（前台）
 * 覆盖 2.3.4 用例 017：
 * 1) 浏览器摄像头（getUserMedia）实时取帧，jsQR 解码真实二维码；
 * 2) 二维码内容为 {域名}/#/scan?code=藏品编号，扫码后自动查询并跳转详情；
 * 3) 摄像头不可用时，保留手动输入与演示模式（随机公开藏品）两条兜底路径。
 */
const route = useRoute()
const router = useRouter()

const code = ref(typeof route.query.code === 'string' ? route.query.code : '')
const error = ref('')
const scanning = ref(false)
const querying = ref(false)
const cameraOn = ref(false)
const cameraHint = ref('正在识别…')

const videoEl = ref(null)
const canvasEl = ref(null)

let stream = null
let rafId = 0
let decoding = false

/** 打开详情：按编号查询，命中后跳转 */
async function openByCode(value) {
  const relic = await getRelicByCode(value)
  if (relic?.id) {
    router.push({ name: 'relic-detail', params: { id: relic.id } })
  }
}

async function queryByCode() {
  const value = code.value.trim()
  if (!value) {
    error.value = '请输入藏品编号'
    return
  }
  error.value = ''
  querying.value = true
  try {
    await openByCode(value)
  } catch (err) {
    error.value = err?.message || '二维码无效或已失效'
  } finally {
    querying.value = false
  }
}

/**
 * 解析二维码文本。
 * 支持三种形态：完整溯源链接（含 ?code=）、藏品详情链接（含 #/relic/{id}）、纯编号。
 */
function parseQrText(text) {
  const value = (text || '').trim()
  const idMatch = value.match(/#\/relic\/([A-Za-z0-9]+)/)
  if (idMatch) return { id: idMatch[1] }
  const codeMatch = value.match(/[?&]code=([^&#]+)/)
  if (codeMatch) return { code: decodeURIComponent(codeMatch[1]) }
  return { code: value }
}

/** 解码成功：停止摄像头，按内容跳转 */
async function handleDecoded(text) {
  const parsed = parseQrText(text)
  stopCamera()
  error.value = ''
  if (navigator.vibrate) navigator.vibrate(80)
  try {
    if (parsed.id) {
      router.push({ name: 'relic-detail', params: { id: parsed.id } })
      return
    }
    code.value = parsed.code
    await openByCode(parsed.code)
  } catch (err) {
    error.value = err?.message || '二维码无效或已失效'
  }
}

/** 逐帧抓取 video 画面交给 jsQR 解码 */
function tick() {
  if (!decoding) return
  const video = videoEl.value
  const canvas = canvasEl.value
  if (video && canvas && video.readyState === video.HAVE_ENOUGH_DATA && video.videoWidth) {
    const scale = Math.min(1, 720 / video.videoWidth)
    canvas.width = Math.round(video.videoWidth * scale)
    canvas.height = Math.round(video.videoHeight * scale)
    const ctx = canvas.getContext('2d', { willReadFrequently: true })
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    const image = ctx.getImageData(0, 0, canvas.width, canvas.height)
    const result = jsQR(image.data, image.width, image.height, { inversionAttempts: 'dontInvert' })
    if (result?.data) {
      handleDecoded(result.data)
      return
    }
  }
  rafId = requestAnimationFrame(tick)
}

/** 开启摄像头并开始解码 */
async function startCamera() {
  error.value = ''
  // 浏览器仅在「安全上下文」放行 getUserMedia：HTTPS 或 localhost。
  // 手机用 http://<局域网IP> 访问时 navigator.mediaDevices 会被整体隐藏，
  // 必须先区分这两种情况，否则会被误判成浏览器不支持。
  if (!window.isSecureContext) {
    error.value = `当前是 http 明文地址，浏览器已禁用摄像头。请改用 https://${location.hostname} 访问，或手动输入藏品编号`
    return
  }
  if (!navigator.mediaDevices?.getUserMedia) {
    error.value = '当前浏览器不支持摄像头取流，请改用 Chrome / Edge，或手动输入编号'
    return
  }
  try {
    stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: { ideal: 'environment' }, width: { ideal: 1280 }, height: { ideal: 720 } },
      audio: false,
    })
    cameraOn.value = true
    cameraHint.value = '将二维码置于取景框内'
    await nextTick()
    videoEl.value.srcObject = stream
    await videoEl.value.play()
    decoding = true
    rafId = requestAnimationFrame(tick)
  } catch (err) {
    stopCamera()
    const name = err?.name || ''
    if (name === 'NotAllowedError') {
      error.value = '摄像头权限被拒绝，请在浏览器地址栏左侧允许摄像头后重试'
    } else if (name === 'NotFoundError') {
      error.value = '未检测到可用摄像头，可改用手动输入或演示模式'
    } else {
      error.value = `无法开启摄像头：${err?.message || '未知错误'}`
    }
  }
}

/** 释放摄像头与动画帧 */
function stopCamera() {
  decoding = false
  if (rafId) {
    cancelAnimationFrame(rafId)
    rafId = 0
  }
  if (stream) {
    stream.getTracks().forEach((track) => track.stop())
    stream = null
  }
  if (videoEl.value) videoEl.value.srcObject = null
  cameraOn.value = false
}

function toggleCamera() {
  if (cameraOn.value) {
    stopCamera()
  } else {
    startCamera()
  }
}

/** 演示模式：摄像头不可用时，从公开藏品中随机取一件走真实解析链路 */
async function simulateScan() {
  error.value = ''
  scanning.value = true
  try {
    const list = await getHighlights()
    if (!list?.length) {
      error.value = '暂无可扫码的公开藏品'
      return
    }
    const picked = list[Math.floor(Math.random() * list.length)]
    code.value = picked.relicCode
    await openByCode(picked.relicCode)
  } catch (err) {
    error.value = err?.message || '二维码无效'
  } finally {
    scanning.value = false
  }
}

onMounted(() => {
  if (code.value) queryByCode()
})

onBeforeUnmount(stopCamera)
</script>

<style scoped>
.scan-canvas {
  display: none;
}
.scan-frame.is-live {
  width: 100%;
  height: auto;
  aspect-ratio: 4 / 3;
  background: #0c0a08;
}
.scan-video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.scan-hint {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 10px;
  text-align: center;
  font-size: var(--fs-micro);
  color: #fff;
  letter-spacing: 0.08em;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
}
.scan-sub-actions {
  margin-top: var(--sp-3);
  text-align: center;
}
.link-btn {
  border: 0;
  background: none;
  padding: 4px;
  font-size: var(--fs-micro);
  color: var(--text-muted);
  text-decoration: underline;
  text-underline-offset: 3px;
  cursor: pointer;
}
.link-btn:hover { color: var(--accent); }
.link-btn:disabled { cursor: default; opacity: 0.6; text-decoration: none; }
.divider-text {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin: var(--sp-6) 0 var(--sp-4);
  font-size: var(--fs-micro);
  color: var(--text-muted);
  letter-spacing: 0.06em;
}
.divider-text::before,
.divider-text::after {
  content: "";
  height: 1px;
  flex: 1;
  background: var(--border);
}
.error-text {
  margin-top: var(--sp-3);
  font-size: var(--fs-caption);
  color: var(--danger);
}
.scan-tips {
  margin-top: var(--sp-6);
  padding-top: var(--sp-4);
  border-top: 1px solid var(--border);
  text-align: left;
}
.tip-row {
  display: flex;
  gap: var(--sp-3);
  padding: 6px 0;
  font-size: var(--fs-micro);
  color: var(--text-muted);
  line-height: 1.7;
}
.tip-row .mono { color: var(--accent); flex: none; }
</style>
