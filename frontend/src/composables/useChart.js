import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

/**
 * ECharts 轻量封装
 * ------------------------------------------------------------------
 * 统一处理：容器初始化、配置更新、容器尺寸变化、组件卸载释放。
 * 调用方只需维护一个 option 的 ref。
 */
export function useChart(optionRef) {
  const chartRef = ref(null)
  let instance = null
  let observer = null

  function render() {
    if (!chartRef.value || !optionRef.value) return
    if (!instance) instance = echarts.init(chartRef.value)
    instance.setOption(optionRef.value, true)
  }

  function resize() {
    instance?.resize()
  }

  onMounted(() => {
    render()
    window.addEventListener('resize', resize)
    // 侧栏折叠、Tab 切换等引起的容器变化
    if (typeof ResizeObserver !== 'undefined' && chartRef.value) {
      observer = new ResizeObserver(() => resize())
      observer.observe(chartRef.value)
    }
  })

  watch(optionRef, render, { deep: true })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resize)
    observer?.disconnect()
    instance?.dispose()
    instance = null
  })

  return { chartRef, render, resize }
}

/** 图表基础配置：与设计系统一致的字体、网格、轴线、提示框 */
export const CHART_COLORS = ['#14554c', '#14171a', '#8fb3ab', '#a34b2e', '#9a7a2c', '#b3b9be']

export function baseChart(overrides = {}) {
  return {
    color: CHART_COLORS,
    textStyle: {
      fontFamily: '"PingFang SC","Microsoft YaHei",sans-serif',
      color: '#6b737b',
      fontSize: 11
    },
    grid: { left: 8, right: 12, top: 28, bottom: 4, containLabel: true },
    tooltip: {
      backgroundColor: 'rgba(20,23,26,0.92)',
      borderWidth: 0,
      padding: [8, 12],
      textStyle: { color: '#fff', fontSize: 11 },
      extraCssText: 'border-radius:2px;'
    },
    ...overrides
  }
}

/** 坐标轴通用样式 */
export const axisStyle = {
  axisLine: { lineStyle: { color: '#e5e5e0' } },
  axisTick: { show: false },
  axisLabel: { color: '#8e959c', fontSize: 11 },
  splitLine: { lineStyle: { color: '#f0f0ec' } }
}
