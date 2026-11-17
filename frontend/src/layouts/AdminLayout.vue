<template>
  <div class="admin-shell">
    <!-- 移动端遮罩 -->
    <div v-if="sidebarOpen" class="sidebar-mask" @click="sidebarOpen = false" />

    <aside class="sidebar" :class="{ open: sidebarOpen }">
      <div class="s-logo">
        <AppBrand />
        <div class="who">当前身份 / Identity</div>
        <span class="role-badge" :class="userStore.roleBadge">{{ userStore.roleName }}</span>
      </div>

      <nav class="s-menu">
        <div v-for="group in userStore.menus" :key="group.code" class="menu-group">
          <div class="g-title">{{ group.group }}</div>
          <router-link
            v-for="(item, index) in group.items"
            :key="item.name"
            :to="item.path"
            class="menu-item"
            :class="{ active: route.path === item.path }"
            @click="sidebarOpen = false"
          >
            <span class="idx">{{ String(index + 1).padStart(2, '0') }}</span>
            <span>{{ item.label }}</span>
          </router-link>
        </div>
      </nav>

      <div class="s-foot">
        <router-link to="/" class="foot-link">← 返回前台</router-link>
        <button type="button" class="foot-link" @click="handleLogout">退出登录</button>
      </div>
    </aside>

    <div class="admin-main">
      <header class="admin-topbar">
        <div class="crumbs">
          <button class="menu-btn" type="button" aria-label="切换菜单" @click="sidebarOpen = !sidebarOpen">
            <el-icon><Menu /></el-icon>
          </button>
          <span>管理端</span>
          <span>/</span>
          <b>{{ route.meta.title || '工作台' }}</b>
        </div>
        <div class="topbar-actions">
          <span class="today mono">{{ todayText }}</span>
          <div class="user-chip">
            <span class="avatar">{{ initial }}</span>
            <span>{{ userStore.realName }}</span>
          </div>
        </div>
      </header>

      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppBrand from '@/components/AppBrand.vue'
import { useUserStore } from '@/stores/user'
import { useMetaStore } from '@/stores/meta'
import { formatDate } from '@/utils/format'

/**
 * 后台骨架
 * 左侧为白底索引式菜单（按角色渲染），右侧为内容区。
 * 移动端侧栏以抽屉方式滑出，避免挤压表格。
 */
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const metaStore = useMetaStore()
const sidebarOpen = ref(false)

const initial = computed(() => (userStore.realName || '·').charAt(0))
const todayText = formatDate(new Date())

onMounted(async () => {
  document.body.dataset.density = 'admin'
  await metaStore.ensure()
  if (route.query.denied) {
    ElMessage.warning('当前账号无权访问该页面，已返回工作台')
  }
})

onBeforeUnmount(() => {
  delete document.body.dataset.density
})

async function handleLogout() {
  await userStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.sidebar { display: flex; flex-direction: column; }
.s-menu { flex: 1; overflow-y: auto; padding-bottom: var(--sp-4); }
.s-foot {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: var(--sp-4) var(--sp-5);
  border-top: 1px solid var(--border);
  background: var(--bg-surface);
}
.foot-link {
  padding: 0;
  border: 0;
  background: none;
  text-align: left;
  font-size: var(--fs-micro);
  letter-spacing: 0.06em;
  color: var(--text-muted);
  cursor: pointer;
}
.foot-link:hover { color: var(--text-primary); }
.menu-btn {
  display: none;
  place-items: center;
  width: 30px;
  height: 30px;
  margin-right: 4px;
  border: 1px solid var(--border-strong);
  border-radius: var(--radius);
  background: transparent;
  color: var(--text-primary);
  cursor: pointer;
}
.sidebar-mask {
  position: fixed;
  inset: 0;
  z-index: 80;
  background: rgba(20, 23, 26, 0.36);
}
.today { font-size: var(--fs-micro); color: var(--text-muted); letter-spacing: 0.06em; }
@media (max-width: 980px) {
  .menu-btn { display: inline-grid; }
}
@media (max-width: 640px) {
  .today { display: none; }
}
</style>
