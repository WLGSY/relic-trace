<template>
  <div ref="chartRef" class="chart" :style="{ height }" />
</template>

<script setup>
import { computed } from 'vue'
import { useChart } from '@/composables/useChart'

/**
 * 图表容器
 * 把 ECharts 实例的生命周期收敛到一个组件里，页面只需传 option。
 */
const props = defineProps({
  option: { type: Object, default: () => ({}) },
  height: { type: String, default: '260px' }
})

const optionRef = computed(() => props.option)
const { chartRef } = useChart(optionRef)
</script>

<style scoped>
.chart { width: 100%; }
</style>
