<template>
  <div class="empty-state">
    <div class="big">{{ big }}</div>
    <div class="title">{{ title }}</div>
    <div v-if="msg" class="msg">{{ msg }}</div>
    <slot />
  </div>
</template>

<script setup>
/** 空状态：以展签留白的方式表达"此处暂无内容" */
defineProps({
  big: { type: String, default: '—' },
  title: { type: String, default: '暂无数据' },
  msg: { type: String, default: '' }
})
</script>
