<template>
  <div>
    <PageHeader title="修复管理" index="06" subtitle="登记修复申请、跟踪修复过程并归档修复前后状况">
      <template #actions>
        <el-button type="primary" @click="openCreate">新增修复申请</el-button>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="修复单号 / 藏品名称"
        clearable
        style="width: 220px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="query.applyStatus" placeholder="全部状态" clearable style="width: 130px" @change="search">
        <el-option v-for="(item, key) in APPLY_STATUS" :key="key" :label="item.label" :value="Number(key)" />
      </el-select>
      <el-button @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" row-key="id">
      <el-table-column prop="applyCode" label="修复单号" width="120">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.applyCode }}</span></template>
      </el-table-column>
      <el-table-column label="藏品" min-width="160">
        <template #default="{ row }">
          <div>{{ row.relicName }}</div>
          <div class="mono muted" style="font-size: 11px">{{ row.relicCode }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="repairItem" label="修复项目" min-width="170" show-overflow-tooltip />
      <el-table-column prop="repairUserName" label="修复人" width="90" />
      <el-table-column label="起止时间" width="190">
        <template #default="{ row }">
          <span class="mono" style="font-size: 11px">{{ row.startTime || '—' }} → {{ row.endTime || '进行中' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><StatusTag :map="APPLY_STATUS" :value="row.applyStatus" /></template>
      </el-table-column>
      <el-table-column prop="conditionAfter" label="修复后状况" min-width="150" show-overflow-tooltip>
        <template #default="{ row }"><span class="muted">{{ row.conditionAfter || '—' }}</span></template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openView(row)">详情</el-button>
          <el-button v-if="row.applyStatus === 2" link type="primary" @click="openProcess(row)">记录过程</el-button>
          <el-button v-if="row.applyStatus !== 2" link type="danger" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.pageNum"
      @current-change="onPageChange"
    />

    <!-- 新增修复申请 -->
    <el-dialog v-model="dialog" title="新增修复申请" width="700px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="选择藏品" prop="fkRelicId">
          <div v-if="picked" class="picked">
            <span class="mono" style="font-size: 12px">{{ picked.relicCode }}</span>
            <span>{{ picked.relicName }}</span>
            <StatusTag :map="STOCK_STATUS" :value="picked.stockStatus" />
            <el-button link type="danger" @click="clearPicked">重选</el-button>
          </div>
          <el-button v-else @click="pickerVisible = true">从档案选择藏品</el-button>
        </el-form-item>

        <div class="form-grid">
          <el-form-item label="修复项目" prop="repairItem">
            <el-input v-model="form.repairItem" placeholder="如 除尘、揭裱与局部补绢" />
          </el-form-item>
          <el-form-item label="修复人">
            <el-input v-model="form.repairUserName" placeholder="如 周修复" />
          </el-form-item>
          <el-form-item label="修复开始时间">
            <el-date-picker v-model="form.startTime" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="修复前影像">
            <el-upload action="#" :auto-upload="false" :limit="2">
              <el-button size="small">选择影像</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="修复前状况" class="full">
            <el-input v-model="form.conditionBefore" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="描述病害类型、程度与影响范围" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">提交审核</el-button>
      </template>
    </el-dialog>

    <!-- 记录修复过程 -->
    <el-dialog v-model="processVisible" title="记录修复过程" width="620px" append-to-body>
      <div v-if="current" class="process-head">
        <div>
          <div class="panel-label">修复单号</div>
          <div class="mono" style="margin-top: 4px">{{ current.applyCode }}</div>
        </div>
        <div>
          <div class="panel-label">藏品</div>
          <div style="margin-top: 4px">{{ current.relicName }}</div>
        </div>
        <div>
          <div class="panel-label">修复前状况</div>
          <div class="muted" style="margin-top: 4px">{{ current.conditionBefore || '—' }}</div>
        </div>
      </div>

      <el-form label-position="top" style="margin-top: 16px">
        <div class="form-grid">
          <el-form-item label="修复结束时间">
            <el-date-picker v-model="processForm.endTime" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="修复后完残状况">
            <el-select v-model="processForm.integrityStatus" style="width: 100%">
              <el-option v-for="item in integrityOptions" :key="item.value" :label="item.label" :value="item.label" />
            </el-select>
          </el-form-item>
          <el-form-item label="修复后状况" class="full">
            <el-input v-model="processForm.conditionAfter" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="描述修复措施、材料与最终效果" />
          </el-form-item>
          <el-form-item label="是否恢复在库" class="full">
            <el-switch v-model="processForm.backToStock" active-text="修复完成，恢复在库" inactive-text="仍需留修复室观察" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProcess">保存修复记录</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="viewVisible" title="修复详情" width="620px" append-to-body>
      <div v-if="current" class="stack">
        <div class="kv-list">
          <div class="kv-row"><span class="kv-key">修复单号</span><span class="kv-value mono">{{ current.applyCode }}</span></div>
          <div class="kv-row"><span class="kv-key">藏品</span><span class="kv-value">{{ current.relicName }}</span></div>
          <div class="kv-row"><span class="kv-key">修复项目</span><span class="kv-value">{{ current.repairItem }}</span></div>
          <div class="kv-row"><span class="kv-key">修复人</span><span class="kv-value">{{ current.repairUserName }}</span></div>
          <div class="kv-row"><span class="kv-key">开始时间</span><span class="kv-value">{{ current.startTime || '—' }}</span></div>
          <div class="kv-row"><span class="kv-key">结束时间</span><span class="kv-value">{{ current.endTime || '进行中' }}</span></div>
        </div>
        <div>
          <div class="panel-label">修复前状况</div>
          <p class="process-text">{{ current.conditionBefore || '—' }}</p>
        </div>
        <div>
          <div class="panel-label">修复后状况</div>
          <p class="process-text">{{ current.conditionAfter || '尚未填写（审核通过后由修复人补充）' }}</p>
        </div>
        <div>
          <div class="panel-label">审核意见</div>
          <p class="process-text">{{ current.auditOpinion || '—' }}</p>
        </div>
      </div>
    </el-dialog>

    <RelicPicker ref="picker" v-model="pickerVisible" title="选择待修复藏品" @select="onPick" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import RelicPicker from '@/components/RelicPicker.vue'
import { createRepair, deleteRepair, listRepairs, processRepair } from '@/api/repair'
import { useTable } from '@/composables/useTable'
import { useMetaStore } from '@/stores/meta'
import { APPLY_STATUS, STOCK_STATUS } from '@/utils/constants'
import { today } from '@/utils/format'

/**
 * 修复管理
 * 覆盖 1.2.4（3）与用例 008：
 * 登记修复申请 → 审核通过 → 记录修复过程（前后状况）→ 写入溯源时间线。
 */
const meta = useMetaStore()
const { loading, list, total, query, load, search, reset, onPageChange } = useTable(listRepairs, {
  defaultQuery: { keyword: '', applyStatus: '' }
})

const integrityOptions = ref([])
const pickerVisible = ref(false)
const picked = ref(null)
const current = ref(null)
const saving = ref(false)

const dialog = ref(false)
const formRef = ref(null)
const form = reactive({ fkRelicId: '', repairItem: '', repairUserName: '周修复', startTime: today(), conditionBefore: '' })
const rules = {
  fkRelicId: [{ required: true, message: '请选择藏品', trigger: 'change' }],
  repairItem: [{ required: true, message: '请填写修复项目', trigger: 'blur' }]
}

const processVisible = ref(false)
const processForm = reactive({ endTime: today(), conditionAfter: '', integrityStatus: '完好', backToStock: true })

const viewVisible = ref(false)

function openCreate() {
  Object.assign(form, { fkRelicId: '', repairItem: '', repairUserName: '周修复', startTime: today(), conditionBefore: '' })
  picked.value = null
  dialog.value = true
}

function onPick(relic) {
  picked.value = relic
  form.fkRelicId = relic.id
}

function clearPicked() {
  picked.value = null
  form.fkRelicId = ''
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createRepair({ ...form })
    ElMessage.success('修复申请已提交审核')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

function openProcess(row) {
  current.value = row
  Object.assign(processForm, {
    endTime: today(),
    conditionAfter: row.conditionAfter || '',
    integrityStatus: '完好',
    backToStock: true
  })
  processVisible.value = true
}

async function saveProcess() {
  saving.value = true
  try {
    await processRepair(current.value.id, { ...processForm })
    ElMessage.success('修复记录已保存')
    processVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function openView(row) {
  current.value = row
  viewVisible.value = true
}

async function removeRow(row) {
  await ElMessageBox.confirm(`确定删除修复申请「${row.applyCode}」吗？`, '删除确认', { type: 'warning' })
  await deleteRepair(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  await meta.ensure()
  integrityOptions.value = meta.dictOptions('integrity_status')
})
</script>

<style scoped>
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 var(--sp-5); }
.form-grid .full { grid-column: 1 / -1; }
.picked {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: 8px 12px;
  border: 1px solid var(--border-strong);
  border-radius: var(--radius);
  font-size: var(--fs-caption);
}
.process-head {
  display: grid;
  grid-template-columns: 1fr 1fr 1.4fr;
  gap: var(--sp-4);
  padding-bottom: var(--sp-4);
  border-bottom: 1px solid var(--border);
}
.process-text { margin-top: 6px; font-size: var(--fs-caption); color: var(--text-secondary); line-height: 1.8; }
@media (max-width: 640px) {
  .form-grid, .process-head { grid-template-columns: 1fr; }
}
</style>
