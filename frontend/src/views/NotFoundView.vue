<template>
  <div class="notfound">
    <div class="mono code">404</div>
    <h1 class="serif">页面不存在</h1>
    <p class="muted">你访问的地址可能已被移除，或链接输入有误。</p>
    <div class="row" style="justify-content: center; margin-top: 24px">
      <router-link class="btn-solid" to="/">返回首页</router-link>
      <router-link class="btn-line" to="/search">检索藏品</router-link>
    </div>
  </div>
</template>

<script setup>
/** 404 兜底页 */
</script>

<style scoped>
.notfound {
  display: grid;
  place-items: center;
  min-height: 72vh;
  text-align: center;
  padding: var(--sp-6);
}
.code { font-size: 12px; letter-spacing: 0.3em; color: var(--ink-300); }
h1 { margin: var(--sp-4) 0 var(--sp-2); font-size: 34px; font-weight: 500; color: var(--text-primary); }
p { font-size: var(--fs-caption); }
</style>
