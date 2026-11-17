<template>
  <div>
    <PageHeader title="审核工作台" index="09" subtitle="集中处理征集、出入库、修复、借调与注销五类待审业务">
      <template #actions>
        <el-button :disabled="!selection.length" @click="batchPass">批量通过（{{ selection.length }}）</el-button>
      </template>
    </PageHeader>

    <div class="pending-summary">
      <div v-for="item in summaryCards" :key="item.type" class="summary-cell" :class="{ active: activeType === String(item.type) }" @click="switchType(item.type)">
        <div class="panel-label">{{ item.en }}</div>
        <div class="summary-value">{{ item.count }}</div>
        <div class="muted">{{ item.label }}</div>
      </div>
    </div>

    <section class="section" style="margin-top: 32px">
      <SectionHead title="待审核业务" :sub="`当前共 ${pendingTotal} 条待处理`" />

      <el-table v-loading="loading" :data="pending" row-key="`${bizType}-${bizId}`" @selection-change="(rows) => (selection = rows)">
        <el-table-column type="selection" width="42" />
        <el-table-column label="业务类型" width="110">
          <template #default="{ row }">
            <span class="tag ink">{{ AUDIT_BIZ_TYPE[row.bizType]?.label }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="bizCode" label="单据编号" width="120">
          <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.bizCode }}</span></template>
        </el-table-column>
        <el-table-column label="审核对象" min-width="200">
          <template #default="{ row }">
            <div class="audit-title">{{ row.title }}</div>
            <div class="muted">{{ row.subtitle }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="submitUser" label="提交人" width="90" />
        <el-table-column prop="submitTime" label="提交时间" width="150">
          <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ formatDateTime(row.submitTime) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAudit(row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>

      <EmptyState v-if="!loading && !pending.length" big="✓" title="待审业务已清空" msg="当前没有需要处理的单据" />
    </section>

    <section class="section">
      <SectionHead title="审核记录" sub="历史审核结论，只读留存" />
      <el-table :data="records" size="small">
        <el-table-column prop="bizCode" label="单据编号" width="120">
          <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.bizCode }}</span></template>
        </el-table-column>
        <el-table-column prop="bizTypeName" label="业务类型" width="110" />
        <el-table-column prop="auditUserName" label="审核人" width="90" />
        <el-table-column prop="auditTime" label="审核时间" width="150">
          <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ formatDateTime(row.auditTime) }}</span></template>
        </el-table-column>
        <el-table-column label="结果" width="90">
          <template #default="{ row }"><StatusTag :map="AUDIT_RESULT" :value="row.auditResult" /></template>
        </el-table-column>
        <el-table-column prop="auditOpinion" label="审核意见" min-width="220" show-overflow-tooltip />
      </el-table>
      <el-pagination
        layout="total, prev, pager, next"
        :total="recordTotal"
        :page-size="recordQuery.pageSize"
        :current-page="recordQuery.pageNum"
        @current-change="onRecordPage"
      />
    </section>

    <AuditDialog v-model="auditVisible" :item="current" @done="reload" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import SectionHead from '@/components/SectionHead.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import AuditDialog from '@/components/AuditDialog.vue'
import { batchAudit, getPendingCount, listAuditRecords, listPending } from '@/api/audit'
import { AUDIT_BIZ_TYPE, AUDIT_RESULT } from '@/utils/constants'
import { formatDateTime } from '@/utils/format'

/**
 * 审核工作台
 * 覆盖 2.3.2 / 2.3.3 中的审核用例（005、011、012、013、014）：
 * 分类查看待审业务、单条审核（通过/驳回，驳回必填意见）、批量通过。
 */
const activeType = ref('all')
const pending = ref([])
const loading = ref(false)
const selection = ref([])
const auditVisible = ref(false)
const current = ref(null)
const counts = ref({ 1: 0, 2: 0, 3: 0, 4: 0, 5: 0, total: 0 })

const records = ref([])
const recordTotal = ref(0)
const recordQuery = reactive({ pageNum: 1, pageSize: 5 })

const summaryCards = computed(() => [
  { type: 1, label: '征集审核', en: 'COLLECT', count: counts.value[1] || 0 },
  { type: 2, label: '出入库审核', en: 'STOCK', count: counts.value[2] || 0 },
  { type: 3, label: '修复审核', en: 'REPAIR', count: counts.value[3] || 0 },
  { type: 4, label: '借调审核', en: 'LOAN', count: counts.value[4] || 0 },
  { type: 5, label: '注销审核', en: 'CANCEL', count: counts.value[5] || 0 }
])

const pendingTotal = computed(() => counts.value.total || 0)

async function loadPending() {
  loading.value = true
  try {
    const params = { pageNum: 1, pageSize: 100 }
    if (activeType.value !== 'all') params.bizType = Number(activeType.value)
    const result = await listPending(params)
    pending.value = result.list || []
  } finally {
    loading.value = false
  }
}

async function loadCounts() {
  counts.value = await getPendingCount()
}

async function loadRecords() {
  const result = await listAuditRecords({ ...recordQuery })
  records.value = result.list || []
  recordTotal.value = result.total || 0
}

function switchType(type) {
  activeType.value = activeType.value === String(type) ? 'all' : String(type)
  loadPending()
}

function openAudit(row) {
  current.value = row
  auditVisible.value = true
}

async function reload() {
  await Promise.all([loadPending(), loadCounts(), loadRecords()])
}

async function batchPass() {
  const items = selection.value.map((row) => ({ bizType: row.bizType, bizId: row.bizId }))
  await ElMessageBox.confirm(
    `将对选中的 ${items.length} 条业务执行"通过"，请确认已核验完毕。`,
    '批量通过',
    { confirmButtonText: '确认通过' }
  )
  await batchAudit(items, '批量通过，材料齐全。')
  ElMessage.success(`已通过 ${items.length} 条`)
  await reload()
}

async function onRecordPage(page) {
  recordQuery.pageNum = page
  await loadRecords()
}

onMounted(() => reload())
</script>

<style scoped>
.pending-summary {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
}
.summary-cell {
  padding: var(--sp-4);
  border-right: 1px solid var(--border);
  cursor: pointer;
  transition: background var(--ease);
}
.summary-cell:last-child { border-right: 0; }
.summary-cell:hover { background: var(--bg-subtle); }
.summary-cell.active { background: var(--bg-subtle); box-shadow: inset 0 -2px 0 var(--ink-900); }
.summary-value { font-family: var(--font-display); font-size: 30px; line-height: 1.2; margin: var(--sp-2) 0 2px; color: var(--text-primary); }
.audit-title { font-size: var(--fs-caption); color: var(--text-primary); margin-bottom: 2px; }
@media (max-width: 900px) {
  .pending-summary { grid-template-columns: repeat(2, 1fr); }
  .summary-cell:nth-child(2n) { border-right: 0; }
  .summary-cell:nth-child(n + 3) { border-top: 1px solid var(--border); }
}
</style>
