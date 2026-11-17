<template>
  <div>
    <PageHeader title="展览借调" index="07" subtitle="借调申请、出库交接与归还接收的全流程记录">
      <template #actions>
        <el-button type="primary" @click="openCreate">新增借调申请</el-button>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="借调单号 / 藏品名称 / 借调单位"
        clearable
        style="width: 260px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="query.loanStatus" placeholder="全部状态" clearable style="width: 130px" @change="search">
        <el-option v-for="(item, key) in LOAN_STATUS" :key="key" :label="item.label" :value="Number(key)" />
      </el-select>
      <el-button @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <span class="toolbar-meta">共 {{ total }} 条借调记录</span>
    </div>

    <el-table v-loading="loading" :data="list" row-key="id">
      <el-table-column prop="loanCode" label="借调单号" width="118">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.loanCode }}</span></template>
      </el-table-column>
      <el-table-column label="藏品" min-width="150">
        <template #default="{ row }">
          <div>{{ row.relicName }}</div>
          <div class="mono muted" style="font-size: 11px">{{ row.relicCode }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="loanUnit" label="借调单位" min-width="150" show-overflow-tooltip />
      <el-table-column prop="loanUserName" label="经办人" width="90" />
      <el-table-column prop="loanTime" label="借出时间" width="110" />
      <el-table-column prop="planReturnTime" label="预计归还" width="110">
        <template #default="{ row }">
          <span :class="{ overdue: row.overdueDays > 0 }">{{ row.planReturnTime }}</span>
          <span v-if="row.overdueDays > 0" class="overdue-tag">逾期 {{ row.overdueDays }} 天</span>
        </template>
      </el-table-column>
      <el-table-column prop="actualReturnTime" label="实际归还" width="110">
        <template #default="{ row }"><span class="muted">{{ row.actualReturnTime || '—' }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><StatusTag :map="LOAN_STATUS" :value="row.loanStatus" /></template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="right">
        <template #default="{ row }">
          <el-button v-if="row.loanStatus === 1" link type="primary" @click="handover(row)">出库交接</el-button>
          <el-button v-if="row.loanStatus === 2" link type="primary" @click="openReturn(row)">归还接收</el-button>
          <span v-if="[3, 4].includes(row.loanStatus)" class="muted">已结束</span>
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

    <!-- 新增借调 -->
    <el-dialog v-model="dialog" title="新增借调申请" width="700px" append-to-body destroy-on-close>
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
          <el-form-item label="借调单位" prop="loanUnit">
            <el-input v-model="form.loanUnit" placeholder="如 长沙市博物馆" />
          </el-form-item>
          <el-form-item label="经办人">
            <el-input v-model="form.loanUserName" />
          </el-form-item>
          <el-form-item label="借出时间" prop="loanTime">
            <el-date-picker v-model="form.loanTime" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="预计归还时间" prop="planReturnTime">
            <el-date-picker v-model="form.planReturnTime" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="借调协议" class="full">
            <el-upload action="#" :auto-upload="false" :limit="1">
              <el-button size="small">上传协议文件</el-button>
              <template #tip><div class="muted" style="margin-top: 4px">审核通过并办理交接后，系统自动归档协议路径</div></template>
            </el-upload>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">提交审核</el-button>
      </template>
    </el-dialog>

    <!-- 归还接收 -->
    <el-dialog v-model="returnVisible" title="归还接收" width="520px" append-to-body>
      <div v-if="current" class="return-head">
        <div class="panel-label">借调单号</div>
        <div class="mono">{{ current.loanCode }}</div>
        <div class="muted" style="margin-top: 6px">{{ current.relicName }} · {{ current.loanUnit }}</div>
      </div>
      <el-form label-position="top" style="margin-top: 16px">
        <el-form-item label="实际归还时间">
          <el-date-picker v-model="returnForm.actualReturnTime" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="验收情况">
          <el-input v-model="returnForm.inspection" type="textarea" :rows="3" placeholder="如 无新增损伤，包装完好" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveReturn">确认接收</el-button>
      </template>
    </el-dialog>

    <RelicPicker ref="picker" v-model="pickerVisible" title="选择借调藏品" @select="onPick" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import RelicPicker from '@/components/RelicPicker.vue'
import { createLoan, handoverLoan, listLoans, returnLoan } from '@/api/loan'
import { useTable } from '@/composables/useTable'
import { LOAN_STATUS, STOCK_STATUS } from '@/utils/constants'
import { today } from '@/utils/format'

/**
 * 展览借调管理
 * 覆盖 1.2.4（2）与用例 009：
 * 申请 → 审核 → 出库交接（藏品转为借出）→ 归还接收（恢复在库），全程写入溯源。
 */
const { loading, list, total, query, load, search, reset, onPageChange } = useTable(listLoans, {
  defaultQuery: { keyword: '', loanStatus: '' }
})

const pickerVisible = ref(false)
const picked = ref(null)
const saving = ref(false)
const current = ref(null)

const dialog = ref(false)
const formRef = ref(null)
const form = reactive({ fkRelicId: '', loanUnit: '', loanUserName: '', loanTime: today(), planReturnTime: today() })
const rules = {
  fkRelicId: [{ required: true, message: '请选择藏品', trigger: 'change' }],
  loanUnit: [{ required: true, message: '请填写借调单位', trigger: 'blur' }],
  planReturnTime: [{ required: true, message: '请选择预计归还时间', trigger: 'change' }]
}

const returnVisible = ref(false)
const returnForm = reactive({ actualReturnTime: today(), inspection: '无新增损伤，包装完好' })

function openCreate() {
  Object.assign(form, { fkRelicId: '', loanUnit: '', loanUserName: '', loanTime: today(), planReturnTime: today() })
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
    await createLoan({ ...form })
    ElMessage.success('借调申请已提交审核')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function handover(row) {
  await ElMessageBox.confirm(
    `办理出库交接后，「${row.relicName}」状态将变为借出，并写入溯源时间线。是否继续？`,
    '出库交接',
    { confirmButtonText: '办理交接' }
  )
  await handoverLoan(row.id)
  ElMessage.success('出库交接完成')
  load()
}

function openReturn(row) {
  current.value = row
  Object.assign(returnForm, { actualReturnTime: today(), inspection: '无新增损伤，包装完好' })
  returnVisible.value = true
}

async function saveReturn() {
  saving.value = true
  try {
    await returnLoan(current.value.id, { ...returnForm })
    ElMessage.success('归还接收完成，藏品已恢复在库')
    returnVisible.value = false
    load()
  } finally {
    saving.value = false
  }
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
.overdue { color: var(--danger); }
.overdue-tag {
  display: inline-block;
  margin-left: 6px;
  padding: 1px 5px;
  font-size: 10px;
  color: var(--danger);
  background: var(--danger-soft);
  border-radius: var(--radius);
}
.return-head { padding-bottom: var(--sp-4); border-bottom: 1px solid var(--border); }
@media (max-width: 640px) {
  .form-grid { grid-template-columns: 1fr; }
}
</style>
