<template>
  <div>
    <PageHeader title="系统工作台" index="00" :subtitle="`${greeting}，${userStore.realName}。以下是今天需要关注的馆藏事务。`">
      <template #actions>
        <el-button v-if="userStore.roleType === 2" type="primary" @click="router.push('/admin/collect')">
          新增征集
        </el-button>
        <el-button v-if="userStore.roleType === 3" type="primary" @click="router.push('/admin/audit')">
          进入审核
        </el-button>
        <el-button v-if="userStore.roleType === 1" @click="router.push('/admin/stats')">
          查看统计
        </el-button>
      </template>
    </PageHeader>

    <div class="role-banner">
      <span class="seal">溯</span>
      当前以 <b>{{ userStore.roleName }}</b> 身份登录，左侧菜单已按该角色的权限范围渲染。
    </div>

    <StatGrid :items="summary" />

    <div class="grid-2 section">
      <div class="card card-pad">
        <div class="card-head">
          <div>
            <div class="card-title">待办事项</div>
            <div class="card-sub">按当前角色汇总需要处理的业务</div>
          </div>
          <span class="panel-label mono">TO-DO</span>
        </div>
        <div>
          <div
            v-for="todo in todos"
            :key="todo.label"
            class="list-row clickable"
            @click="todo.path && router.push(todo.path)"
          >
            <div>
              <div class="list-row-main">{{ todo.label }}</div>
              <div class="list-row-meta">{{ todo.desc }}</div>
            </div>
            <div class="row" style="gap: 12px">
              <span class="todo-count">{{ todo.count }}</span>
              <span class="muted">{{ todo.unit }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="card card-pad">
        <div class="card-head">
          <div>
            <div class="card-title">最近操作</div>
            <div class="card-sub">系统最近产生的操作记录</div>
          </div>
          <router-link class="link-quiet" to="/admin/log">全部日志 →</router-link>
        </div>
        <div class="timeline">
          <div v-for="log in logs" :key="log.id" class="tl-item">
            <span class="dot" />
            <div class="t-time">{{ formatDateTime(log.operateTime) }}</div>
            <div class="t-type">{{ log.module }} · {{ log.operateType }}</div>
            <div class="t-content">{{ log.operateContent }}</div>
            <div class="t-op">{{ log.operateUserName }}</div>
          </div>
          <div v-if="!logs.length" class="muted">暂无操作记录</div>
        </div>
      </div>
    </div>

    <div class="section">
      <SectionHead title="馆藏概览" sub="按库存状态与业务进度分布的实时数据" />
      <div class="status-grid">
        <div v-for="item in statusCards" :key="item.label" class="status-cell">
          <div class="panel-label">{{ item.en }}</div>
          <div class="status-value">{{ formatNumber(item.value) }}<span>件</span></div>
          <div class="muted">{{ item.label }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import StatGrid from '@/components/StatGrid.vue'
import SectionHead from '@/components/SectionHead.vue'
import { getOverview } from '@/api/stats'
import { getPendingCount } from '@/api/audit'
import { listOperateLogs } from '@/api/system'
import { useUserStore } from '@/stores/user'
import { formatDateTime, formatNumber } from '@/utils/format'

/**
 * 系统工作台
 * 按角色展示指标、待办与最近操作，是登录后的默认落地页。
 */
const router = useRouter()
const userStore = useUserStore()

const overview = ref({})
const pending = ref({ 1: 0, 2: 0, 3: 0, 4: 0, 5: 0, total: 0 })
const logs = ref([])

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 11) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const summary = computed(() => [
  { label: '藏品总数 / Collection', value: overview.value.totalRelics || 0, unit: '件', hint: `本月新增 ${overview.value.monthNew || 0} 件` },
  { label: '在库藏品 / In Stock', value: overview.value.inStock || 0, unit: '件', hint: `借出 ${overview.value.loaned || 0} · 修复 ${overview.value.repairing || 0}` },
  { label: '待审核单据 / Pending', value: overview.value.pendingAudits || 0, unit: '单', alert: (overview.value.pendingAudits || 0) > 0, hint: '需及时处理' },
  { label: '本月出入库 / This Month', value: overview.value.monthStock || 0, unit: '次', hint: `本月溯源 ${overview.value.monthTrace || 0} 条` }
])

const todos = computed(() => {
  const list = [
    {
      label: '待审核业务单据',
      desc: '征集 / 出入库 / 修复 / 借调 / 注销',
      count: pending.value.total || 0,
      unit: '单',
      path: userStore.roleType === 3 ? '/admin/audit' : ''
    },
    {
      label: '借出中的藏品',
      desc: '关注预计归还时间，逾期需催还',
      count: overview.value.loaned || 0,
      unit: '件',
      path: userStore.roleType === 2 ? '/admin/loan' : ''
    },
    {
      label: '修复中的藏品',
      desc: '跟踪修复进度并补充修复后状况',
      count: overview.value.repairing || 0,
      unit: '件',
      path: userStore.roleType === 2 ? '/admin/repair' : ''
    },
    {
      label: '公开藏品',
      desc: '可被访客检索与扫码的藏品数量',
      count: overview.value.publicCount || 0,
      unit: '件',
      path: ''
    }
  ]
  return list
})

const statusCards = computed(() => [
  { label: '在库', en: 'IN STOCK', value: overview.value.inStock || 0 },
  { label: '出库', en: 'OUT', value: overview.value.outbound || 0 },
  { label: '借出', en: 'ON LOAN', value: overview.value.loaned || 0 },
  { label: '修复中', en: 'IN REPAIR', value: overview.value.repairing || 0 },
  { label: '已注销', en: 'CANCELLED', value: overview.value.cancelled || 0 },
  { label: '累计扫码', en: 'SCANS', value: overview.value.scanTotal || 0 }
])

onMounted(async () => {
  const [overviewData, pendingData, logData] = await Promise.all([
    getOverview(),
    getPendingCount(),
    listOperateLogs({ pageNum: 1, pageSize: 5 })
  ])
  overview.value = overviewData || {}
  pending.value = pendingData || pending.value
  logs.value = logData?.list || []
})
</script>

<style scoped>
.todo-count { font-family: var(--font-display); font-size: 20px; color: var(--text-primary); }
.status-grid { display: grid; grid-template-columns: repeat(6, 1fr); border-top: 1px solid var(--border); border-bottom: 1px solid var(--border); }
.status-cell { padding: var(--sp-4); border-right: 1px solid var(--border); }
.status-cell:last-child { border-right: 0; }
.status-value { font-family: var(--font-display); font-size: 28px; line-height: 1.2; margin: var(--sp-2) 0 2px; color: var(--text-primary); }
.status-value span { font-family: var(--font-sans); font-size: var(--fs-micro); color: var(--text-muted); margin-left: 4px; }
@media (max-width: 1180px) {
  .status-grid { grid-template-columns: repeat(3, 1fr); }
  .status-cell:nth-child(3n) { border-right: 0; }
  .status-cell:nth-child(n + 4) { border-top: 1px solid var(--border); }
}
@media (max-width: 640px) {
  .status-grid { grid-template-columns: repeat(2, 1fr); }
  .status-cell:nth-child(3n) { border-right: 1px solid var(--border); }
  .status-cell:nth-child(2n) { border-right: 0; }
  .status-cell:nth-child(n + 3) { border-top: 1px solid var(--border); }
}
</style>
