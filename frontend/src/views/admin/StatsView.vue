<template>
  <div>
    <PageHeader title="统计分析" index="10" subtitle="按分类、年代、级别与库房维度统计馆藏与流转情况">
      <template #actions>
        <el-select v-model="months" style="width: 120px" @change="loadTrend">
          <el-option label="近 6 个月" :value="6" />
          <el-option label="近 12 个月" :value="12" />
        </el-select>
        <el-button @click="exportReport">导出报表</el-button>
      </template>
    </PageHeader>

    <StatGrid :items="summary" />

    <section class="section grid-aside">
      <div class="chart-card">
        <div class="chart-head">
          <span class="chart-title">出入库与溯源趋势</span>
          <span class="chart-note">MONTHLY</span>
        </div>
        <EChart :option="trendOption" height="320px" />
      </div>
      <div class="chart-card">
        <div class="chart-head">
          <span class="chart-title">级别构成</span>
          <span class="chart-note">BY LEVEL</span>
        </div>
        <EChart :option="levelOption" height="320px" />
      </div>
    </section>

    <section class="section grid-2">
      <div class="chart-card">
        <div class="chart-head">
          <span class="chart-title">分类占比</span>
          <span class="chart-note">BY CATEGORY</span>
        </div>
        <EChart :option="categoryOption" height="280px" />
      </div>
      <div class="chart-card">
        <div class="chart-head">
          <span class="chart-title">库房存量</span>
          <span class="chart-note">BY WAREHOUSE</span>
        </div>
        <EChart :option="warehouseOption" height="280px" />
      </div>
    </section>

    <section class="section">
      <SectionHead title="分类明细" sub="统计结果与前台数据看板保持一致" />
      <el-table :data="categoryRows">
        <el-table-column prop="name" label="藏品分类" min-width="140" />
        <el-table-column prop="value" label="藏品数量" width="120" />
        <el-table-column label="占比" width="220">
          <template #default="{ row }">
            <div class="ratio">
              <span class="ratio-bar"><i :style="{ width: `${row.ratio}%` }" /></span>
              <span class="mono">{{ row.ratio }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="era" label="主要年代" min-width="140" />
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import SectionHead from '@/components/SectionHead.vue'
import StatGrid from '@/components/StatGrid.vue'
import EChart from '@/components/EChart.vue'
import { getCategoryStats, getEraStats, getLevelStats, getOverview, getStockTrend, getWarehouseStats } from '@/api/stats'
import { baseChart, axisStyle } from '@/composables/useChart'
import { exportCsv } from '@/utils/export'
import { formatNumber } from '@/utils/format'

/**
 * 统计分析
 * 覆盖 2.3.5 用例 018：按维度统计藏品数量、出入库与溯源数据并以图表展示，
 * 支持切换时间范围与导出报表。
 */
const overview = ref({})
const categories = ref([])
const eras = ref([])
const levels = ref([])
const warehouseStats = ref([])
const trend = ref({ labels: [], inbound: [], outbound: [], trace: [] })
const months = ref(6)

const summary = computed(() => [
  { label: '藏品总数 / Collection', value: overview.value.totalRelics || 0, unit: '件' },
  { label: '在库 / In Stock', value: overview.value.inStock || 0, unit: '件' },
  { label: '本月出入库 / Movements', value: overview.value.monthStock || 0, unit: '次' },
  { label: '累计扫码 / Scans', value: overview.value.scanTotal || 0, unit: '次' }
])

const categoryRows = computed(() => {
  const total = categories.value.reduce((sum, item) => sum + item.value, 0) || 1
  return categories.value.map((item) => ({
    ...item,
    ratio: Math.round((item.value / total) * 100),
    era: eraOfCategory(item.name)
  }))
})

/** 演示用：按分类给出主要年代说明 */
function eraOfCategory(name) {
  const map = { 陶瓷: '五代 — 宋', 青铜: '商周 — 汉', 书画: '明清', 玉器: '汉代', 织绣: '宋 — 清', 竹木: '明清' }
  return map[name] || '—'
}

const trendOption = computed(() => ({
  ...baseChart(),
  tooltip: { ...baseChart().tooltip, trigger: 'axis' },
  legend: { top: 0, right: 0, icon: 'rect', itemWidth: 10, itemHeight: 2, textStyle: { color: '#6b737b', fontSize: 11 } },
  grid: { left: 4, right: 10, top: 34, bottom: 4, containLabel: true },
  xAxis: { type: 'category', data: trend.value.labels, axisLine: axisStyle.axisLine, axisTick: { show: false }, axisLabel: axisStyle.axisLabel },
  yAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false }, axisLabel: axisStyle.axisLabel, splitLine: axisStyle.splitLine },
  series: [
    { name: '入库', type: 'bar', barWidth: 10, itemStyle: { color: '#14171a' }, data: trend.value.inbound },
    { name: '出库', type: 'bar', barWidth: 10, itemStyle: { color: '#8fb3ab' }, data: trend.value.outbound },
    {
      name: '溯源记录',
      type: 'line',
      symbol: 'circle',
      symbolSize: 4,
      lineStyle: { width: 1.4, color: '#a34b2e' },
      itemStyle: { color: '#a34b2e' },
      data: trend.value.trace
    }
  ]
}))

const levelOption = computed(() => ({
  ...baseChart(),
  tooltip: { ...baseChart().tooltip, trigger: 'item', formatter: '{b}<br/>{c} 件（{d}%）' },
  series: [
    {
      type: 'pie',
      radius: ['46%', '68%'],
      center: ['50%', '50%'],
      itemStyle: { borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{c}', color: '#6b737b', fontSize: 11, lineHeight: 16 },
      labelLine: { length: 8, length2: 8, lineStyle: { color: '#d2d2cb' } },
      data: levels.value.filter((item) => item.value > 0).map((item) => ({ name: item.name, value: item.value }))
    }
  ]
}))

const categoryOption = computed(() => ({
  ...baseChart(),
  tooltip: { ...baseChart().tooltip, trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 4, right: 8, top: 20, bottom: 4, containLabel: true },
  xAxis: { type: 'category', data: categories.value.map((i) => i.name), axisLine: axisStyle.axisLine, axisTick: { show: false }, axisLabel: axisStyle.axisLabel },
  yAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false }, axisLabel: axisStyle.axisLabel, splitLine: axisStyle.splitLine },
  series: [{ type: 'bar', barWidth: 18, itemStyle: { color: '#14554c' }, data: categories.value.map((i) => i.value) }]
}))

const warehouseOption = computed(() => ({
  ...baseChart(),
  tooltip: { ...baseChart().tooltip, trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 4, right: 16, top: 20, bottom: 4, containLabel: true },
  xAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false }, axisLabel: axisStyle.axisLabel, splitLine: axisStyle.splitLine },
  yAxis: { type: 'category', data: warehouseStats.value.map((i) => i.name), axisLine: { show: false }, axisTick: { show: false }, axisLabel: axisStyle.axisLabel },
  series: [{ type: 'bar', barWidth: 12, itemStyle: { color: '#14171a' }, data: warehouseStats.value.map((i) => i.value) }]
}))

async function loadTrend() {
  try {
    trend.value = await getStockTrend(months.value)
  } catch {
    // 单个接口失败不影响其余图表；错误提示由 axios 拦截器统一给出
    trend.value = { labels: [], inbound: [], outbound: [], trace: [] }
  }
}

function exportReport() {
  exportCsv(
    `馆藏统计-${new Date().toISOString().slice(0, 10)}`,
    ['统计维度', '名称', '数量'],
    [
      ...categories.value.map((i) => ['分类', i.name, i.value]),
      ...eras.value.map((i) => ['年代', i.name, i.value]),
      ...levels.value.map((i) => ['级别', i.name, i.value]),
      ...warehouseStats.value.map((i) => ['库房', i.name, i.value])
    ]
  )
  ElMessage.success('统计报表已导出')
}

onMounted(async () => {
  // 各统计维度相互独立：全部 settle 后再赋值，避免某个接口异常导致整页空白
  const [overviewData, categoryData, eraData, levelData, warehouseData] = await Promise.allSettled([
    getOverview(), getCategoryStats(), getEraStats(), getLevelStats(), getWarehouseStats()
  ])
  overview.value = overviewData.status === 'fulfilled' ? overviewData.value || {} : {}
  categories.value = categoryData.status === 'fulfilled' ? categoryData.value || [] : []
  eras.value = eraData.status === 'fulfilled' ? eraData.value || [] : []
  levels.value = levelData.status === 'fulfilled' ? levelData.value || [] : []
  warehouseStats.value = warehouseData.status === 'fulfilled' ? warehouseData.value || [] : []
  await loadTrend()
})
</script>

<style scoped>
.ratio { display: flex; align-items: center; gap: 10px; }
.ratio-bar { flex: 1; height: 4px; background: var(--paper-1); display: block; }
.ratio-bar i { display: block; height: 100%; background: var(--ink-900); }
</style>
