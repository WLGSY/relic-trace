<template>
  <div class="section-head">
    <div>
      <h2 class="section-title">{{ title }}</h2>
      <p v-if="sub" class="section-sub">{{ sub }}</p>
    </div>
    <slot>
      <router-link v-if="link" :to="link" class="section-link">{{ linkText }}</router-link>
    </slot>
  </div>
</template>

<script setup>
/** 区块标题：衬线小标题 + 副说明 + 可选"查看全部" */
defineProps({
  title: { type: String, required: true },
  sub: { type: String, default: '' },
  link: { type: [String, Object], default: '' },
  linkText: { type: String, default: '查看全部 →' }
})
</script>
