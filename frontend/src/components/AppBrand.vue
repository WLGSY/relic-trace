<template>
  <div class="brand">
    <span class="brand-mark">溯</span>
    <div v-if="!compact" class="brand-name">
      {{ title }}
      <small>{{ subtitle }}</small>
    </div>
  </div>
</template>

<script setup>
/** 品牌标识：方框印章 + 中文名 + 等宽英文副标题 */
defineProps({
  title: { type: String, default: '文物藏品管理与溯源系统' },
  subtitle: { type: String, default: 'Relic Archive & Provenance' },
  compact: { type: Boolean, default: false }
})
</script>
