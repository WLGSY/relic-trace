<template>
  <div v-if="traces.length" class="timeline">
    <div
      v-for="(trace, index) in traces"
      :key="trace.id || index"
      class="tl-item"
      :class="toneOf(TRACE_TYPE, trace.traceType)"
    >
      <span class="dot" />
      <div class="t-time">{{ formatDateTime(trace.traceTime) }}</div>
      <div class="t-type">
        {{ TRACE_TYPE[trace.traceType]?.label || '流转' }}
        <span v-if="!hideRelic" class="muted">· {{ trace.relicName }}</span>
      </div>
      <div class="t-content">{{ trace.traceContent }}</div>
      <div class="t-op">操作人 {{ trace.operatorName }}<span v-if="!hideRelic"> · NO. {{ trace.relicCode }}</span></div>
    </div>
  </div>
  <EmptyState v-else big="○" title="暂无溯源记录" msg="该藏品尚未产生流转记录" />
</template>

<script setup>
import EmptyState from './EmptyState.vue'
import { TRACE_TYPE } from '@/utils/constants'
import { formatDateTime, toneOf } from '@/utils/format'

/**
 * 溯源时间线
 * 每条记录对应 relic_trace_record：时间 / 类型 / 内容 / 操作人
 */
defineProps({
  traces: { type: Array, default: () => [] },
  hideRelic: { type: Boolean, default: false }
})
</script>
