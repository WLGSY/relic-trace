<template>
  <div>
    <PageHeader title="出入库管理" index="04" subtitle="登记入库、出库与库房转移，审核通过后自动写入溯源时间线">
      <template #actions>
        <el-button type="primary" @click="openCreate">登记出入库</el-button>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="单据编号 / 藏品名称 / 藏品编号"
        clearable
        style="width: 240px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="query.recordType" placeholder="全部类型" clearable style="width: 118px" @change="search">
        <el-option v-for="(item, key) in RECORD_TYPE" :key="key" :label="item.label" :value="Number(key)" />
      </el-select>
      <el-select v-model="query.applyStatus" placeholder="全部状态" clearable style="width: 126px" @change="search">
        <el-option v-for="(item, key) in SIMPLE_APPLY_STATUS" :key="key" :label="item.label" :value="Number(key)" />
      </el-select>
      <el-input v-model="query.operatorName" placeholder="经手人" clearable style="width: 120px" @keyup.enter="search" />
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        value-format="YYYY-MM-DD"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 240px"
        @change="onDateChange"
      />
      <el-button @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" row-key="id">
      <el-table-column prop="recordCode" label="单据编号" width="122">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.recordCode }}</span></template>
      </el-table-column>
      <el-table-column label="类型" width="82">
        <template #default="{ row }"><StatusTag :map="RECORD_TYPE" :value="row.recordType" /></template>
      </el-table-column>
      <el-table-column label="藏品" min-width="160">
        <template #default="{ row }">
          <div>{{ row.relicName }}</div>
          <div class="mono muted" style="font-size: 11px">{{ row.relicCode }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="sourceTarget" label="来源 / 去向" min-width="150" show-overflow-tooltip />
      <el-table-column label="库房变化" width="170">
        <template #default="{ row }">
          <span class="muted">{{ row.fromWarehouseName }} → {{ row.toWarehouseName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="operatorName" label="经手人" width="90" />
      <el-table-column prop="operateTime" label="经办时间" width="150">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ formatDateTime(row.operateTime) }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><StatusTag :map="SIMPLE_APPLY_STATUS" :value="row.applyStatus" /></template>
      </el-table-column>
      <el-table-column prop="auditOpinion" label="审核意见" min-width="140" show-overflow-tooltip>
        <template #default="{ row }"><span class="muted">{{ row.auditOpinion || '—' }}</span></template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="right" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="printRecord(row)">打印单据</el-button>
          <el-button v-if="row.applyStatus === 0" link type="danger" @click="removeRow(row)">删除</el-button>
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

    <!-- 登记弹窗 -->
    <el-dialog v-model="dialog" title="登记出入库" width="720px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="业务类型" prop="recordType">
          <el-radio-group v-model="form.recordType" @change="onTypeChange">
            <el-radio-button :value="1">入库</el-radio-button>
            <el-radio-button :value="2">出库</el-radio-button>
            <el-radio-button :value="3">移库</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="藏品" prop="fkRelicId">
          <div class="relic-field">
            <div v-if="picked" class="picked">
              <span class="mono" style="font-size: 12px">{{ picked.relicCode }}</span>
              <span>{{ picked.relicName }}</span>
              <StatusTag :map="STOCK_STATUS" :value="picked.stockStatus" />
              <el-button link type="danger" @click="clearPicked">重选</el-button>
            </div>
            <div v-else class="row" style="gap: 8px; width: 100%">
              <el-button @click="pickerVisible = true">从档案选择</el-button>
              <el-input v-model="scanCode" placeholder="或扫码 / 输入藏品编号定位" style="max-width: 260px" @keyup.enter="locate">
                <template #append><el-button @click="locate">定位</el-button></template>
              </el-input>
            </div>
          </div>
        </el-form-item>

        <div class="form-grid">
          <el-form-item :label="form.recordType === 1 ? '来源' : '去向'" prop="sourceTarget">
            <el-input v-model="form.sourceTarget" :placeholder="form.recordType === 1 ? '如 湖南博物院调拨' : '如 长沙市博物馆'" />
          </el-form-item>
          <el-form-item label="经办人">
            <el-input v-model="form.operatorName" />
          </el-form-item>
          <el-form-item v-if="form.recordType !== 1" label="原库房" prop="fkFromWarehouseId">
            <el-select v-model="form.fkFromWarehouseId" clearable style="width: 100%">
              <el-option v-for="item in meta.warehouseOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="form.recordType !== 2" label="目标库房" prop="fkToWarehouseId">
            <el-select v-model="form.fkToWarehouseId" clearable style="width: 100%">
              <el-option v-for="item in meta.warehouseOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="经办时间">
            <el-date-picker v-model="form.operateTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
          </el-form-item>
          <el-form-item label="事由说明" class="full">
            <el-input v-model="form.operateReason" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="如 参加「宋瓷雅韵」巡展" />
          </el-form-item>
        </div>
      </el-form>

      <div class="hint-block">
        提交后单据进入「待审核」状态，审核通过后藏品状态与库房位置才会更新，并写入藏品溯源时间线。
      </div>

      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">提交登记</el-button>
      </template>
    </el-dialog>

    <RelicPicker
      ref="pickerRef"
      v-model="pickerVisible"
      title="选择藏品"
      :query="{ }"
      @select="onPick"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import RelicPicker from '@/components/RelicPicker.vue'
import { createStockRecord, deleteStockRecord, listStockRecords } from '@/api/stock'
import { getRelicByCode } from '@/api/relic'
import { useTable } from '@/composables/useTable'
import { useMetaStore } from '@/stores/meta'
import { useUserStore } from '@/stores/user'
import { RECORD_TYPE, SIMPLE_APPLY_STATUS, STOCK_STATUS } from '@/utils/constants'
import { formatDateTime, now } from '@/utils/format'
import { printHtml } from '@/utils/export'

/**
 * 出入库管理
 * 覆盖 1.2.3（1）（2）（5）与用例 006：
 * 入库 / 出库 / 移库登记、扫码定位、单据打印、按条件查询流转记录。
 */
const meta = useMetaStore()
const userStore = useUserStore()

const { loading, list, total, query, load, search, reset, onPageChange } = useTable(listStockRecords, {
  defaultQuery: { keyword: '', recordType: '', applyStatus: '', operatorName: '', beginTime: '', endTime: '' }
})

const dateRange = ref([])

function onDateChange(value) {
  query.beginTime = value?.[0] || ''
  query.endTime = value?.[1] || ''
  search()
}

/* ---------------- 登记 ---------------- */
const dialog = ref(false)
const saving = ref(false)
const formRef = ref(null)
const pickerVisible = ref(false)
const picked = ref(null)
const scanCode = ref('')

const emptyForm = () => ({
  recordType: 2,
  fkRelicId: '',
  sourceTarget: '',
  fkFromWarehouseId: '',
  fkToWarehouseId: '',
  operatorName: userStore.realName,
  operateTime: now(),
  operateReason: ''
})

const form = reactive(emptyForm())

const rules = {
  recordType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  fkRelicId: [{ required: true, message: '请选择藏品', trigger: 'change' }],
  sourceTarget: [{ required: true, message: '请填写来源或去向', trigger: 'blur' }]
}

function openCreate() {
  Object.assign(form, emptyForm())
  picked.value = null
  scanCode.value = ''
  dialog.value = true
}

function onTypeChange() {
  // 切换类型时清空库房字段，避免残留数据造成歧义
  form.fkFromWarehouseId = ''
  form.fkToWarehouseId = ''
}

function onPick(relic) {
  picked.value = relic
  form.fkRelicId = relic.id
  form.fkFromWarehouseId = relic.fkWarehouseId || ''
}

function clearPicked() {
  picked.value = null
  form.fkRelicId = ''
}

async function locate() {
  const code = scanCode.value.trim()
  if (!code) return ElMessage.warning('请输入藏品编号')
  try {
    const relic = await getRelicByCode(code)
    onPick(relic)
    ElMessage.success(`已定位：${relic.relicName}`)
  } catch {
    // 错误提示由拦截器给出
  }
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createStockRecord({ ...form })
    ElMessage.success('登记成功，等待审核')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function removeRow(row) {
  await ElMessageBox.confirm(`确定删除单据「${row.recordCode}」吗？`, '删除确认', { type: 'warning' })
  await deleteStockRecord(row.id)
  ElMessage.success('已删除')
  load()
}

function printRecord(row) {
  printHtml(
    `出入库单 ${row.recordCode}`,
    `<h1>文物藏品出入库单</h1>
     <div class="sub">${row.recordCode} · 打印时间 ${now()}</div>
     <table>
       <tr><th>业务类型</th><td>${RECORD_TYPE[row.recordType]?.label}</td></tr>
       <tr><th>藏品名称</th><td>${row.relicName}</td></tr>
       <tr><th>藏品编号</th><td>${row.relicCode}</td></tr>
       <tr><th>${row.recordType === 1 ? '来源' : '去向'}</th><td>${row.sourceTarget || '—'}</td></tr>
       <tr><th>原库房</th><td>${row.fromWarehouseName}</td></tr>
       <tr><th>目标库房</th><td>${row.toWarehouseName}</td></tr>
       <tr><th>经办人</th><td>${row.operatorName}</td></tr>
       <tr><th>经办时间</th><td>${formatDateTime(row.operateTime)}</td></tr>
       <tr><th>事由</th><td>${row.operateReason || '—'}</td></tr>
       <tr><th>审核状态</th><td>${SIMPLE_APPLY_STATUS[row.applyStatus]?.label}</td></tr>
     </table>
     <div class="foot"><span>库房保管员签字：____________</span><span>经办人签字：____________</span></div>`
  )
}

onMounted(() => meta.ensure())
</script>

<style scoped>
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 var(--sp-5); }
.form-grid .full { grid-column: 1 / -1; }
.relic-field { width: 100%; }
.picked {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: 8px 12px;
  border: 1px solid var(--border-strong);
  border-radius: var(--radius);
  font-size: var(--fs-caption);
}
.hint-block {
  margin-top: var(--sp-4);
  padding: var(--sp-3);
  background: var(--bg-subtle);
  border-left: 2px solid var(--accent);
  font-size: var(--fs-micro);
  color: var(--text-secondary);
  line-height: 1.7;
}
@media (max-width: 640px) {
  .form-grid { grid-template-columns: 1fr; }
}
</style>
