<template>
  <div>
    <PageHeader title="注销管理" index="08" subtitle="对损毁、丢失或重复入藏的藏品履行注销审批与归档">
      <template #actions>
        <el-button v-if="userStore.roleType === 2" type="primary" @click="openCreate">发起注销申请</el-button>
        <span v-else class="muted">注销归档由系统管理员执行</span>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="注销单号 / 藏品名称"
        clearable
        style="width: 230px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="query.applyStatus" placeholder="全部状态" clearable style="width: 130px" @change="search">
        <el-option v-for="(item, key) in SIMPLE_APPLY_STATUS" :key="key" :label="item.label" :value="Number(key)" />
      </el-select>
      <el-button @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" row-key="id">
      <el-table-column prop="applyCode" label="注销单号" width="118">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.applyCode }}</span></template>
      </el-table-column>
      <el-table-column label="藏品" min-width="150">
        <template #default="{ row }">
          <div>{{ row.relicName }}</div>
          <div class="mono muted" style="font-size: 11px">{{ row.relicCode }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="类别" width="86" />
      <el-table-column prop="cancelReason" label="注销原因" min-width="220" show-overflow-tooltip />
      <el-table-column prop="cancelUserName" label="经办人" width="90" />
      <el-table-column prop="createTime" label="申请时间" width="150">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ formatDateTime(row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><StatusTag :map="SIMPLE_APPLY_STATUS" :value="row.applyStatus" /></template>
      </el-table-column>
      <el-table-column label="归档" width="150">
        <template #default="{ row }">
          <span v-if="row.archiveTime" class="muted">{{ row.archiveUserName }} · {{ formatDate(row.archiveTime) }}</span>
          <span v-else class="muted">未归档</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="right">
        <template #default="{ row }">
          <el-button
            v-if="row.applyStatus === 1 && !row.archiveTime && userStore.roleType === 1"
            link
            type="primary"
            @click="archive(row)"
          >
            执行归档
          </el-button>
          <span v-else-if="row.applyStatus === 1 && !row.archiveTime" class="muted">待管理员归档</span>
          <span v-else class="muted">—</span>
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

    <el-dialog v-model="dialog" title="发起注销申请" width="680px" append-to-body destroy-on-close>
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
          <el-form-item label="经办人">
            <el-input v-model="form.cancelUserName" />
          </el-form-item>
          <el-form-item label="注销原因" prop="cancelReason" class="full">
            <el-input
              v-model="form.cancelReason"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="说明损毁、丢失或重复入藏等具体情况，并附专家论证意见"
            />
          </el-form-item>
        </div>
      </el-form>

      <div class="warn-block">
        注销申请须经审核员审核通过，并由系统管理员执行归档；归档后藏品状态变为「已注销」，前台不再展示。
      </div>

      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">提交审核</el-button>
      </template>
    </el-dialog>

    <RelicPicker ref="picker" v-model="pickerVisible" title="选择待注销藏品" @select="onPick" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import RelicPicker from '@/components/RelicPicker.vue'
import { archiveCancel, createCancel, listCancels } from '@/api/cancel'
import { useTable } from '@/composables/useTable'
import { useUserStore } from '@/stores/user'
import { SIMPLE_APPLY_STATUS, STOCK_STATUS } from '@/utils/constants'
import { formatDate, formatDateTime } from '@/utils/format'

/**
 * 注销管理
 * 覆盖 1.2.4（3）与用例 010：
 * 录入员登记注销申请 → 审核员审核 → 系统管理员执行归档。
 */
const userStore = useUserStore()
const { loading, list, total, query, load, search, reset, onPageChange } = useTable(listCancels, {
  defaultQuery: { keyword: '', applyStatus: '' }
})

const dialog = ref(false)
const formRef = ref(null)
const pickerVisible = ref(false)
const picked = ref(null)
const saving = ref(false)

const form = reactive({ fkRelicId: '', cancelReason: '', cancelUserName: userStore.realName })
const rules = {
  fkRelicId: [{ required: true, message: '请选择藏品', trigger: 'change' }],
  cancelReason: [{ required: true, message: '请填写注销原因', trigger: 'blur' }]
}

function openCreate() {
  Object.assign(form, { fkRelicId: '', cancelReason: '', cancelUserName: userStore.realName })
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
    await createCancel({ ...form })
    ElMessage.success('注销申请已提交审核')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function archive(row) {
  await ElMessageBox.confirm(
    `归档后「${row.relicName}」将被标记为已注销，此操作不可撤销。是否确认？`,
    '执行注销归档',
    { type: 'warning', confirmButtonText: '确认归档' }
  )
  await archiveCancel(row.id)
  ElMessage.success('已完成注销归档')
  load()
}

onMounted(() => load())
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
.warn-block {
  margin-top: var(--sp-4);
  padding: var(--sp-3);
  background: var(--warning-soft);
  border-left: 2px solid var(--warning);
  font-size: var(--fs-micro);
  color: var(--warning);
  line-height: 1.7;
}
@media (max-width: 640px) {
  .form-grid { grid-template-columns: 1fr; }
}
</style>
