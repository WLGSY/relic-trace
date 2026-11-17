<template>
  <div>
    <PageHeader title="操作日志" index="14" subtitle="记录征集、编目、出入库、修复、借调、审核与系统配置的全量操作痕迹">
      <template #actions>
        <el-button @click="exportList">导出当前结果</el-button>
      </template>
    </PageHeader>

    <div class="log-stats">
      <div class="log-stat">
        <div class="panel-label">匹配记录</div>
        <div class="log-stat-value mono">{{ total }}</div>
      </div>
      <div class="log-stat">
        <div class="panel-label">涉及模块</div>
        <div class="log-stat-value mono">{{ modules.length }}</div>
      </div>
      <div class="log-stat">
        <div class="panel-label">当前筛选</div>
        <div class="log-stat-text">{{ filterSummary }}</div>
      </div>
    </div>

    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="操作内容 / 操作人"
        clearable
        style="width: 220px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="query.module" placeholder="全部模块" clearable style="width: 148px" @change="search">
        <el-option v-for="item in modules" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="query.operateType" placeholder="全部类型" clearable style="width: 122px" @change="search">
        <el-option v-for="item in OPERATE_TYPES" :key="item" :label="item" :value="item" />
      </el-select>
      <el-input
        v-model="query.operateUserName"
        placeholder="操作人"
        clearable
        style="width: 130px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        value-format="YYYY-MM-DD"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 248px"
        @change="onDateChange"
      />
      <el-button @click="search">查询</el-button>
      <el-button @click="resetAll">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" row-key="id" class="log-table">
      <el-table-column label="操作时间" width="164">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ formatDateTime(row.operateTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作人" width="112">
        <template #default="{ row }">{{ row.operateUserName || '—' }}</template>
      </el-table-column>
      <el-table-column label="所属模块" width="132">
        <template #default="{ row }"><span class="tag">{{ row.module }}</span></template>
      </el-table-column>
      <el-table-column label="操作类型" width="100">
        <template #default="{ row }">
          <span class="tag" :class="toneOfType(row.operateType)">{{ row.operateType }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="operateContent" label="操作内容" min-width="320" show-overflow-tooltip />
    </el-table>

    <el-pagination
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.pageNum"
      @current-change="onPageChange"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { listLogModules, listOperateLogs } from '@/api/system'
import { useTable } from '@/composables/useTable'
import { formatDateTime } from '@/utils/format'
import { exportCsv } from '@/utils/export'

/**
 * 操作日志
 * 对应文档 1.2.3 系统管理模块：按模块、类型、操作人、时间区间检索审计痕迹。
 * 日志仅提供查询与导出，不提供修改或删除入口，保证审计可追溯。
 */
/** 操作类型筛选项：与后端 OperateLogDict 的操作类型一一对应 */
const OPERATE_TYPES = ['新增', '修改', '删除', '审核', '提交', '上传', '重建', '处理', '登录', '退出', '导出']

/** 操作类型 → 标签色调（新增/提交=石青、删除=陶土、审核=赭石） */
const TYPE_TONE = {
  新增: 'jade',
  修改: 'ink',
  删除: 'cinnabar',
  审核: 'bronze',
  提交: 'jade',
  上传: 'jade',
  重建: 'ink',
  处理: 'ink',
  登录: 'ink',
  退出: 'ink',
  导出: 'ink'
}
const toneOfType = (type) => TYPE_TONE[type] || 'ink'

const modules = ref([])
const dateRange = ref([])

const { loading, list, total, query, search, reset, onPageChange } = useTable(listOperateLogs, {
  defaultQuery: { keyword: '', module: '', operateType: '', operateUserName: '', beginTime: '', endTime: '' }
})

/** 顶部概览：把当前筛选条件翻译成一句话，避免用户忘记已叠加的条件 */
const filterSummary = computed(() => {
  const parts = []
  if (query.module) parts.push(query.module)
  if (query.operateType) parts.push(query.operateType)
  if (query.operateUserName) parts.push(`操作人 ${query.operateUserName}`)
  if (query.keyword) parts.push(`关键词「${query.keyword}」`)
  if (query.beginTime) parts.push(`${query.beginTime} ~ ${query.endTime}`)
  return parts.length ? parts.join(' · ') : '全部日志'
})

function onDateChange(range) {
  query.beginTime = range?.[0] || ''
  query.endTime = range?.[1] || ''
  search()
}

function resetAll() {
  dateRange.value = []
  reset()
}

function exportList() {
  exportCsv(
    `操作日志-${new Date().toISOString().slice(0, 10)}`,
    ['操作时间', '操作人', '所属模块', '操作类型', '操作内容'],
    list.value.map((row) => [formatDateTime(row.operateTime), row.operateUserName, row.module, row.operateType, row.operateContent])
  )
  ElMessage.success('已导出当前页数据')
}

onMounted(async () => {
  try {
    modules.value = (await listLogModules()) || []
  } catch {
    modules.value = []
  }
})
</script>

<style scoped>
.log-stats {
  display: grid;
  grid-template-columns: 160px 160px minmax(0, 1fr);
  border-top: 1px solid var(--ink-900);
  border-bottom: 1px solid var(--border);
  margin-bottom: var(--sp-5);
}
.log-stat { padding: var(--sp-4) var(--sp-4) var(--sp-4) 0; }
.log-stat + .log-stat { padding-left: var(--sp-5); border-left: 1px solid var(--border); }
.log-stat-value { margin-top: 6px; font-size: 26px; line-height: 1.1; color: var(--ink-900); font-feature-settings: 'tnum'; }
.log-stat-text { margin-top: 8px; font-size: 13px; line-height: 1.6; color: var(--text-secondary); }
.log-table :deep(td:first-child) { border-left: 2px solid transparent; }
.log-table :deep(tr:hover td:first-child) { border-left-color: var(--jade-800); }
@media (max-width: 760px) {
  .log-stats { grid-template-columns: 1fr 1fr; }
  .log-stat:last-child { grid-column: 1 / -1; padding-left: 0; border-left: 0; border-top: 1px solid var(--border); }
}
</style>
