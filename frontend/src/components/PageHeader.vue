<template>
  <div class="page-head">
    <div class="page-head-main">
      <div class="page-head-title">
        <span v-if="index" class="page-head-index">{{ index }}</span>
        <span>{{ title }}</span>
      </div>
      <div v-if="subtitle || $slots.subtitle" class="page-head-sub">
        <slot name="subtitle">{{ subtitle }}</slot>
      </div>
    </div>
    <div v-if="$slots.actions" class="page-head-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
/** 页面标题：衬线标题 + 等宽编号 + 右侧操作区 */
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  index: { type: String, default: '' }
})
</script>
