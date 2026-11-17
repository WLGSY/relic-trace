<template>
  <div class="front" data-density="front">
    <header class="front-header">
      <div class="wrap front-nav">
        <router-link to="/" aria-label="返回首页">
          <AppBrand />
        </router-link>

        <nav class="nav-links">
          <router-link v-for="item in navs" :key="item.path" :to="item.path">{{ item.label }}</router-link>
        </nav>

        <div class="row" style="gap: 8px">
          <router-link class="btn-login" to="/login">
            <span>管理端登录</span>
            <span aria-hidden="true">→</span>
          </router-link>
          <button class="menu-btn" type="button" aria-label="打开导航" @click="drawer = true">
            <el-icon><Menu /></el-icon>
          </button>
        </div>
      </div>
    </header>

    <main>
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="footer">
      <div class="wrap">
        <div class="footer-top">
          <div>
            <div class="footer-brand">
              文物藏品管理与溯源系统
              <small>RELIC ARCHIVE &amp; PROVENANCE</small>
            </div>
            <p style="margin-top: 12px; max-width: 420px; line-height: 1.9">
              以藏品编号为线索，串联征集、编目、出入库、修复、借调与注销的完整经历，
              让每一件文物的流转都有据可查。
            </p>
          </div>
          <div class="links">
            <router-link to="/">首页</router-link>
            <router-link to="/search">藏品检索</router-link>
            <router-link to="/dashboard">数据看板</router-link>
            <router-link to="/scan">扫码溯源</router-link>
            <router-link to="/login">管理端</router-link>
          </div>
        </div>
      </div>
    </footer>

    <el-drawer v-model="drawer" direction="rtl" size="72%" :with-header="false">
      <div class="mobile-nav">
        <div class="panel-label">导航</div>
        <router-link v-for="item in navs" :key="item.path" :to="item.path" @click="drawer = false">
          {{ item.label }}
        </router-link>
        <router-link to="/login" @click="drawer = false">管理端登录</router-link>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import AppBrand from '@/components/AppBrand.vue'

/** 前台骨架：白色展墙式页头 + 内容区 + 墨黑页脚 */
const navs = [
  { label: '首页', path: '/' },
  { label: '藏品检索', path: '/search' },
  { label: '数据看板', path: '/dashboard' },
  { label: '扫码溯源', path: '/scan' }
]

const drawer = ref(false)

onMounted(() => {
  document.body.dataset.density = 'front'
})
onBeforeUnmount(() => {
  delete document.body.dataset.density
})
</script>

<style scoped>
.btn-login {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 34px;
  padding: 0 14px;
  border: 1px solid var(--border-strong);
  border-radius: var(--radius);
  font-size: var(--fs-micro);
  letter-spacing: 0.06em;
  color: var(--text-primary);
  transition: border-color var(--ease), background var(--ease);
}
.btn-login:hover { border-color: var(--ink-900); background: var(--bg-subtle); }
.icon-btn {
  display: inline-grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--border-strong);
  border-radius: var(--radius);
  background: transparent;
  color: var(--text-primary);
  cursor: pointer;
}
.menu-btn {
  display: none;
  place-items: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--border-strong);
  border-radius: var(--radius);
  background: transparent;
  color: var(--text-primary);
  cursor: pointer;
}
@media (max-width: 980px) {
  .menu-btn { display: inline-grid; }
}
.mobile-nav { display: flex; flex-direction: column; gap: 2px; }
.mobile-nav a {
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
  font-family: var(--font-display);
  font-size: 20px;
  color: var(--text-primary);
}
.mobile-nav .panel-label { margin-bottom: 12px; }
</style>
