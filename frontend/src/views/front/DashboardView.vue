<template>
  <div class="wrap">
    <PageHeader title="数据看板" index="02" subtitle="馆藏结构、库房分布与流转趋势的公开统计">
      <template #actions>
        <span class="mono muted">更新于 {{ updatedAt }}</span>
      </template>
    </PageHeader>

    <StatGrid :items="summary" />

    <section class="section grid-2">
      <div class="chart-card">
        <div class="chart-head">
          <span class="chart-title">藏品分类构成</span>
          <span class="chart-note">BY CATEGORY</span>
        </div>
        <EChart :option="categoryOption" height="280px" />
      </div>
      <div class="chart-card">
        <div class="chart-head">
          <span class="chart-title">年代分布</span>
          <span class="chart-note">BY ERA</span>
        </div>
        <EChart :option="eraOption" height="280px" />
      </div>
    </section>

    <section class="section grid-aside">
      <div class="chart-card">
        <div class="chart-head">
          <span class="chart-title">近半年流转趋势</span>
          <span class="chart-note">STOCK MOVEMENT</span>
        </div>
        <EChart :option="trendOption" height="300px" />
      </div>
      <div class="chart-card">
        <div class="chart-head">
          <span class="chart-title">库房分布</span>
          <span class="chart-note">BY WAREHOUSE</span>
        </div>
        <EChart :option="warehouseOption" height="300px" />
      </div>
    </section>

    <section class="section">
      <SectionHead title="最新溯源记录" sub="公开藏品的近期流转节点" link="/search" link-text="浏览藏品 →" />
      <div class="card card-pad">
        <TraceTimeline :traces="recentTraces" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import SectionHead from '@/components/SectionHead.vue'
import StatGrid from '@/components/StatGrid.vue'
import EChart from '@/components/EChart.vue'
import TraceTimeline from '@/components/TraceTimeline.vue'
import { getPublicStats, getStockTrend } from '@/api/stats'
import { baseChart, axisStyle } from '@/composables/useChart'
import { formatDate } from '@/utils/format'

/**
 * 前台数据看板
 * 覆盖 2.3.5 用例 019：藏品总量、分类统计、年代与库房分布、流转动态。
 * 图表配色来自设计 token：墨黑为主，石青作对比，陶土仅用于负向趋势。
 */
const stats = ref({ total: 0, categories: [], eras: [], warehouses: [], recentTraces: [], scanTotal: 0 })
const trend = ref({ labels: [], inbound: [], outbound: [], trace: [] })
const loading = ref(true)

const updatedAt = computed(() => formatDate(new Date()))

/** 只保留有藏品的分类 / 库房，避免图例出现一堆 0 */
const categoryData = computed(() => (stats.value.categories || []).filter((item) => item.value > 0))
const eraData = computed(() => (stats.value.eras || []).filter((item) => item.value > 0))
const warehouseData = computed(() => (stats.value.warehouses || []).filter((item) => item.value > 0))

const summary = computed(() => [
  { label: '公开藏品 / Objects', value: stats.value.total, unit: '件' },
  { label: '藏品分类 / Categories', value: categoryData.value.length, unit: '类' },
  { label: '覆盖库房 / Warehouses', value: warehouseData.value.length, unit: '座' },
  { label: '累计扫码 / Scans', value: stats.value.scanTotal || 0, unit: '次' }
])

const recentTraces = computed(() => stats.value.recentTraces || [])

const categoryOption = computed(() => ({
  ...baseChart(),
  tooltip: { ...baseChart().tooltip, trigger: 'item', formatter: '{b}<br/>{c} 件（{d}%）' },
  legend: {
    bottom: 0,
    icon: 'circle',
    itemWidth: 6,
    itemHeight: 6,
    itemGap: 14,
    textStyle: { color: '#6b737b', fontSize: 11 }
  },
  series: [
    {
      type: 'pie',
      radius: ['52%', '72%'],
      center: ['50%', '43%'],
      avoidLabelOverlap: true,
      itemStyle: { borderColor: '#ffffff', borderWidth: 2 },
      label: { show: false },
      data: categoryData.value.map((item) => ({ name: item.name, value: item.value }))
    }
  ]
}))

const eraOption = computed(() => ({
  ...baseChart(),
  tooltip: { ...baseChart().tooltip, trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 4, right: 8, top: 20, bottom: 4, containLabel: true },
  xAxis: {
    type: 'category',
    data: eraData.value.map((item) => item.name),
    axisLine: axisStyle.axisLine,
    axisTick: { show: false },
    axisLabel: { ...axisStyle.axisLabel, interval: 0, rotate: 30 }
  },
  yAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false }, axisLabel: axisStyle.axisLabel, splitLine: axisStyle.splitLine },
  series: [
    {
      type: 'bar',
      barWidth: 14,
      itemStyle: { color: '#14171a' },
      data: eraData.value.map((item) => item.value)
    }
  ]
}))

const warehouseOption = computed(() => ({
  ...baseChart(),
  tooltip: { ...baseChart().tooltip, trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 4, right: 16, top: 16, bottom: 4, containLabel: true },
  xAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false }, axisLabel: axisStyle.axisLabel, splitLine: axisStyle.splitLine },
  yAxis: {
    type: 'category',
    data: warehouseData.value.map((item) => item.name),
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: axisStyle.axisLabel
  },
  series: [
    {
      type: 'bar',
      barWidth: 12,
      itemStyle: { color: '#14554c' },
      data: warehouseData.value.map((item) => item.value)
    }
  ]
}))

const trendOption = computed(() => ({
  ...baseChart(),
  tooltip: { ...baseChart().tooltip, trigger: 'axis' },
  legend: {
    top: 0,
    right: 0,
    icon: 'rect',
    itemWidth: 10,
    itemHeight: 2,
    textStyle: { color: '#6b737b', fontSize: 11 }
  },
  grid: { left: 4, right: 8, top: 32, bottom: 4, containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trend.value.labels,
    axisLine: axisStyle.axisLine,
    axisTick: { show: false },
    axisLabel: axisStyle.axisLabel
  },
  yAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false }, axisLabel: axisStyle.axisLabel, splitLine: axisStyle.splitLine },
  series: [
    {
      name: '入库',
      type: 'line',
      symbol: 'circle',
      symbolSize: 4,
      lineStyle: { width: 1.4, color: '#14554c' },
      itemStyle: { color: '#14554c' },
      areaStyle: { color: 'rgba(20,85,76,0.06)' },
      data: trend.value.inbound
    },
    {
      name: '出库',
      type: 'line',
      symbol: 'circle',
      symbolSize: 4,
      lineStyle: { width: 1.4, color: '#a34b2e' },
      itemStyle: { color: '#a34b2e' },
      data: trend.value.outbound
    },
    {
      // 溯源记录：藏品每一次交接留痕的条数，是「流转」最完整的口径
      name: '溯源记录',
      type: 'line',
      symbol: 'circle',
      symbolSize: 4,
      lineStyle: { width: 1.2, color: '#14171a', type: 'dashed' },
      itemStyle: { color: '#14171a' },
      data: trend.value.trace || []
    }
  ]
}))

onMounted(async () => {
  // 两个请求互不依赖：任一个失败时另一个仍要正常展示，
  // 否则一次 401/超时会让整块看板全为 0（Promise.all 的失败会吞掉全部结果）。
  const [publicStats, trendData] = await Promise.allSettled([getPublicStats(), getStockTrend(6)])
  if (publicStats.status === 'fulfilled' && publicStats.value) stats.value = publicStats.value
  if (trendData.status === 'fulfilled' && trendData.value) trend.value = trendData.value
  loading.value = false
})
</script>
