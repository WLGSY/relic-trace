<template>
  <div class="stats">
    <div v-for="item in items" :key="item.label" class="stat">
      <div class="label">{{ item.label }}</div>
      <div class="num" :class="{ alert: item.alert }">
        {{ formatNumber(item.value) }}<small v-if="item.unit">{{ item.unit }}</small>
      </div>
      <div v-if="item.hint" class="trend" :class="item.trend">{{ item.hint }}</div>
    </div>
  </div>
</template>

<script setup>
import { formatNumber } from '@/utils/format'

/**
 * 指标条：以发丝线网格代替卡片，数字用衬线体呈现，
 * 更接近展签上的"数据说明"气质。
 */
defineProps({
  items: { type: Array, default: () => [] }
})
</script>
