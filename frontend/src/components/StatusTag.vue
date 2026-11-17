<template>
  <span class="tag" :class="tone">{{ text }}</span>
</template>

<script setup>
import { computed } from 'vue'

/**
 * 状态标签：由枚举常量表驱动，保证同一状态在全站的颜色一致。
 * 用法：<StatusTag :map="STOCK_STATUS" :value="relic.stockStatus" />
 */
const props = defineProps({
  map: { type: Object, required: true },
  value: { type: [Number, String, Boolean], default: null },
  fallback: { type: String, default: '—' }
})

const entry = computed(() => props.map?.[props.value] || null)
const text = computed(() => entry.value?.label || props.fallback)
const tone = computed(() => entry.value?.tone || 'ink')
</script>
