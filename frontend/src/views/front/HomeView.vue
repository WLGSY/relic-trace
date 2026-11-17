<template>
  <div class="wrap">
    <!-- 主视觉：白墙展签式布局 -->
    <section class="hero">
      <div class="hero-copy">
        <div class="eyebrow">馆藏档案 · 全流程溯源</div>
        <h1>让每一件文物<br />都有迹可循</h1>
        <p>
          从征集入藏到修复、借展与归还，藏品在其生命周期中的每一次交接，
          都被记录为一条可回溯的档案。
        </p>
        <div class="hero-actions">
          <router-link class="btn-solid" to="/search">检索藏品</router-link>
          <router-link class="link-arrow" to="/scan">扫码溯源 <span aria-hidden="true">→</span></router-link>
        </div>
        <div class="hero-meta">
          <div>
            <div class="m-label">公开藏品</div>
            <div class="m-value">{{ formatNumber(stats.total) }}<span style="font-size: 12px; color: var(--text-muted); margin-left: 4px">件</span></div>
          </div>
          <div>
            <div class="m-label">藏品分类</div>
            <div class="m-value">{{ formatNumber(stats.categories?.length) }}<span style="font-size: 12px; color: var(--text-muted); margin-left: 4px">类</span></div>
          </div>
          <div>
            <div class="m-label">累计扫码</div>
            <div class="m-value">{{ formatNumber(stats.scanTotal) }}<span style="font-size: 12px; color: var(--text-muted); margin-left: 4px">次</span></div>
          </div>
        </div>
      </div>

      <div class="hero-figure">
        <div class="hero-frame">
          <ArtFrame :key="current.id || index" :image="current.cover" :alt="current.relicName" />
        </div>
        <div class="hero-caption">
          <span>
            NO. <b>{{ current.relicCode || '—' }}</b>
            <span v-if="current.relicName"> · {{ current.relicName }}</span>
          </span>
          <div class="hero-dots">
            <button
              v-for="(item, i) in slides"
              :key="item.id"
              type="button"
              :class="{ active: i === index }"
              :aria-label="`查看第 ${i + 1} 件藏品`"
              @click="index = i"
            />
          </div>
        </div>
      </div>
    </section>

    <!-- 精选藏品 -->
    <section class="section">
      <SectionHead title="精选藏品" sub="馆藏公开藏品选介，点击查看档案与溯源记录" link="/search" />
      <div v-if="loading" class="relic-grid">
        <div v-for="i in 3" :key="i" class="skeleton" />
      </div>
      <div v-else-if="highlights.length" class="relic-grid">
        <RelicCard v-for="relic in highlights.slice(0, 6)" :key="relic.id" :relic="relic" />
      </div>
      <EmptyState v-else title="暂无公开藏品" msg="请先在后台完成编目建档并设为公开" />
    </section>

    <!-- 流转动态 + 快速溯源 -->
    <section class="section grid-aside">
      <div>
        <SectionHead title="最新流转动态" sub="近期发生的入藏、出库、修复与借展记录" link="/dashboard" link-text="进入数据看板 →" />
        <div class="card card-pad">
          <TraceTimeline :traces="recentTraces" />
        </div>
      </div>

      <div>
        <SectionHead title="快速溯源" sub="扫描藏品二维码，或输入藏品编号" />
        <div class="card card-pad text-center">
          <button
            v-if="qrImage"
            type="button"
            class="qr-figure"
            :title="qrRelic.relicName ? `查看「${qrRelic.relicName}」的溯源档案` : '查看溯源档案'"
            @click="openQrRelic"
          >
            <img :src="qrImage" :alt="`${qrRelic.relicName || '藏品'} 的溯源二维码`" @error="qrImage = ''" />
          </button>
          <div v-else class="qr-figure is-empty" aria-hidden="true"><span class="mono">QR</span></div>

          <div class="panel-label" style="margin-bottom: 8px">
            NO. {{ qrRelic.relicCode || '—' }}
            <template v-if="qrRelic.relicName"> · {{ qrRelic.relicName }}</template>
          </div>
          <p class="muted" style="margin-bottom: var(--sp-4)">
            {{ qrImage ? '手机相机扫码，直接打开这件藏品的溯源档案' : '暂无可扫码的公开藏品，可手动输入编号查询' }}
          </p>
          <el-input
            v-model="code"
            placeholder="如 TC-0005"
            class="code-input"
            @keyup.enter="goScan"
          />
          <button class="btn-solid" style="width: 100%; margin-top: 10px" @click="goScan">查询溯源</button>
          <router-link to="/scan" class="link-quiet" style="display: inline-block; margin-top: 12px">
            打开扫码页 →
          </router-link>
        </div>
      </div>
    </section>

    <!-- 系统能力 -->
    <section class="section">
      <SectionHead title="从征集到注销" sub="六类业务单据，串联藏品的完整生命周期" />
      <div class="capabilities">
        <div v-for="(item, i) in capabilities" :key="item.title" class="capability">
          <div class="num">{{ String(i + 1).padStart(2, '0') }}</div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ArtFrame from '@/components/ArtFrame.vue'
import RelicCard from '@/components/RelicCard.vue'
import SectionHead from '@/components/SectionHead.vue'
import TraceTimeline from '@/components/TraceTimeline.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getHighlights, getPublicRelic } from '@/api/relic'
import { getPublicStats } from '@/api/stats'
import { formatNumber } from '@/utils/format'
import { uploadUrl } from '@/utils/asset'

/**
 * 前台首页
 * 覆盖文档 3.2 前台首页模块：精品展示、流转动态、快速溯源入口。
 */
const router = useRouter()

const highlights = ref([])
const stats = ref({ total: 0, categories: [], scanTotal: 0 })
const recentTraces = ref([])
const loading = ref(true)
const code = ref('')
const index = ref(0)
/** 快速溯源面板展示的真实二维码：取精选藏品的第一件 */
const qrRelic = ref({})
const qrImage = ref('')

const capabilities = [
  { title: '征集登记', desc: '录入来源、方式与规格，提交审核后进入编目环节。' },
  { title: '编目建档', desc: '生成全局唯一藏品编号与溯源二维码，形成正式档案。' },
  { title: '出入库与盘点', desc: '登记每一次物理位置变化，定期盘点核对账实。' },
  { title: '修复与借调', desc: '记录修复前后状况与借展去向，归还验收留痕。' }
]

const slides = computed(() => (highlights.value.length ? highlights.value.slice(0, 3) : [{ id: 'placeholder', cover: 'e' }]))
const current = computed(() => slides.value[index.value] || slides.value[0] || {})

let timer = null
onMounted(async () => {
  // 精选藏品与公开统计互不依赖，分别容错，避免其中一个失败导致首页整块空白
  const [listResult, statsResult] = await Promise.allSettled([getHighlights(), getPublicStats()])
  highlights.value = listResult.status === 'fulfilled' ? listResult.value || [] : []

  const base = statsResult.status === 'fulfilled' ? statsResult.value || {} : {}
  stats.value = {
    total: base.total ?? base.totalRelics ?? 0,
    categories: base.categories ?? new Array(base.totalCategories ?? 0).fill(null),
    scanTotal: base.scanTotal ?? 0
  }
  recentTraces.value = (base.recentTraces || []).slice(0, 5)
  loading.value = false

  if (highlights.value.length) loadQrCode(highlights.value[0])

  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (!reduce && slides.value.length > 1) {
    timer = setInterval(() => { index.value = (index.value + 1) % slides.value.length }, 6000)
  }
})

onBeforeUnmount(() => clearInterval(timer))

/**
 * 载入某件公开藏品的真实溯源二维码。
 *
 * <p>图片由后端在用 ZXing 生成并落盘（/uploads/qrcodes/{编号}.png），
 * 内容为「{站点}/#/scan?code=编号」，手机相机扫码可直接进入该藏品档案。
 * 接口不可用时按编号约定兜底拼路径，保证首页不至于只剩占位符。</p>
 */
async function loadQrCode(relic) {
  if (!relic?.id) return
  qrRelic.value = relic
  const fallback = relic.relicCode ? uploadUrl(`/uploads/qrcodes/${relic.relicCode}.png`) : ''
  try {
    const detail = await getPublicRelic(relic.id)
    qrImage.value = uploadUrl(detail?.qrcode?.imageUrl) || fallback
  } catch {
    qrImage.value = fallback
  }
}

/** 点击二维码进入该藏品的溯源档案 */
function openQrRelic() {
  if (qrRelic.value?.id) router.push({ name: 'relic-detail', params: { id: qrRelic.value.id } })
}

function goScan() {
  const value = code.value.trim()
  if (!value) {
    ElMessage.warning('请输入藏品编号，或直接扫描上方二维码')
    return
  }
  router.push({ name: 'scan', query: { code: value } })
}
</script>

<style scoped>
.skeleton {
  aspect-ratio: 4 / 5;
  background: linear-gradient(100deg, var(--paper-1) 20%, #fff 40%, var(--paper-1) 60%);
  background-size: 220% 100%;
  animation: shimmer 1.4s infinite linear;
  border: 1px solid var(--border);
}
@keyframes shimmer { from { background-position: 120% 0; } to { background-position: -20% 0; } }
.code-input :deep(.el-input__wrapper) { box-shadow: 0 0 0 1px var(--border-strong) inset; }
</style>
