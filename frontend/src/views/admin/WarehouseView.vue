<template>
  <div>
    <PageHeader title="库房管理" index="05" subtitle="维护库房与仓位信息，执行库存盘点并处理账实差异" />

    <el-tabs v-model="tab">
      <!-- 库房信息 -->
      <el-tab-pane label="库房信息" name="warehouse">
        <div class="toolbar" style="border-bottom: 0; padding-top: 0">
          <span class="toolbar-meta" style="margin-left: 0">共 {{ warehouses.length }} 座库房</span>
          <div class="toolbar-actions">
            <el-button type="primary" @click="openWarehouse()">新增库房</el-button>
          </div>
        </div>

        <div class="warehouse-grid">
          <div v-for="item in warehouses" :key="item.id" class="card card-pad warehouse-card">
            <div class="row-between">
              <span class="mono warehouse-code">{{ item.warehouseCode }}</span>
              <span class="tag" :class="item.isEnabled === 1 ? 'jade' : 'ink'">{{ item.isEnabled === 1 ? '启用' : '停用' }}</span>
            </div>
            <h3 class="warehouse-name">{{ item.warehouseName }}</h3>
            <div class="muted">{{ item.location || '位置未填写' }}</div>
            <div class="warehouse-stats">
              <div><span class="panel-label">仓位</span><b>{{ item.positionCount }}</b></div>
              <div><span class="panel-label">在库藏品</span><b>{{ item.relicCount }}</b></div>
              <div><span class="panel-label">保管员</span><b style="font-size: 13px">{{ item.keeperName || '—' }}</b></div>
            </div>
            <div class="row" style="gap: 14px; margin-top: 12px">
              <el-button link type="primary" @click="openWarehouse(item)">编辑</el-button>
              <el-button link type="danger" @click="removeWarehouse(item)">删除</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 仓位管理 -->
      <el-tab-pane label="仓位管理" name="position">
        <div class="toolbar">
          <el-select v-model="positionWarehouse" placeholder="全部库房" clearable style="width: 180px" @change="loadPositions">
            <el-option v-for="item in warehouses" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
          <el-button @click="loadPositions">刷新</el-button>
          <div class="toolbar-actions">
            <el-button type="primary" @click="openPosition()">新增仓位</el-button>
          </div>
        </div>

        <el-table v-loading="positionLoading" :data="positions">
          <el-table-column prop="positionCode" label="仓位编号" width="130">
            <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.positionCode }}</span></template>
          </el-table-column>
          <el-table-column prop="positionName" label="仓位名称" min-width="150" />
          <el-table-column prop="warehouseName" label="所属库房" width="140" />
          <el-table-column label="存放藏品" width="110">
            <template #default="{ row }">{{ row.relicCount }} 件</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <span class="tag" :class="row.isEnabled === 1 ? 'jade' : 'ink'">{{ row.isEnabled === 1 ? '启用' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openPosition(row)">编辑</el-button>
              <el-button link type="danger" @click="removePosition(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 库存盘点 -->
      <el-tab-pane label="库存盘点" name="check">
        <div class="toolbar">
          <el-select v-model="checkWarehouse" placeholder="全部库房" clearable style="width: 180px" @change="loadChecks">
            <el-option v-for="item in warehouses" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
          <el-button @click="loadChecks">查询</el-button>
          <div class="toolbar-actions">
            <el-button type="primary" @click="openCheck">新建盘点</el-button>
          </div>
        </div>

        <el-table v-loading="checkLoading" :data="checks">
          <el-table-column prop="checkCode" label="盘点单号" width="130">
            <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.checkCode }}</span></template>
          </el-table-column>
          <el-table-column prop="warehouseName" label="盘点库房" width="140" />
          <el-table-column prop="checkUserName" label="盘点人" width="100" />
          <el-table-column prop="checkTime" label="盘点时间" width="110" />
          <el-table-column label="应盘 / 实盘" width="120">
            <template #default="{ row }">{{ row.totalCount }} / {{ row.realCount }}</template>
          </el-table-column>
          <el-table-column label="差异" width="90">
            <template #default="{ row }">
              <span :class="row.abnormalCount > 0 ? 'diff-alert' : ''">{{ row.abnormalCount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="96">
            <template #default="{ row }"><StatusTag :map="CHECK_STATUS" :value="row.checkStatus" /></template>
          </el-table-column>
          <el-table-column prop="checkResult" label="盘点结果" min-width="200" show-overflow-tooltip />
          <el-table-column label="操作" width="110" align="right">
            <template #default="{ row }">
              <el-button v-if="row.checkStatus === 1" link type="primary" @click="openFinish(row)">完成盘点</el-button>
              <span v-else class="muted">已完成</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 库房表单 -->
    <el-dialog v-model="warehouseDialog" :title="warehouseForm.id ? '编辑库房' : '新增库房'" width="560px" append-to-body>
      <el-form ref="warehouseRef" :model="warehouseForm" :rules="warehouseRules" label-position="top">
        <div class="form-grid">
          <el-form-item label="库房编号" prop="warehouseCode"><el-input v-model="warehouseForm.warehouseCode" placeholder="如 W-04" /></el-form-item>
          <el-form-item label="库房名称" prop="warehouseName"><el-input v-model="warehouseForm.warehouseName" placeholder="如 三号库房" /></el-form-item>
          <el-form-item label="库房位置"><el-input v-model="warehouseForm.location" /></el-form-item>
          <el-form-item label="保管员"><el-input v-model="warehouseForm.keeperName" /></el-form-item>
          <el-form-item label="是否启用">
            <el-radio-group v-model="warehouseForm.isEnabled">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注"><el-input v-model="warehouseForm.memo" /></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="warehouseDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveWarehouse">保存</el-button>
      </template>
    </el-dialog>

    <!-- 仓位表单 -->
    <el-dialog v-model="positionDialog" :title="positionForm.id ? '编辑仓位' : '新增仓位'" width="520px" append-to-body>
      <el-form ref="positionRef" :model="positionForm" :rules="positionRules" label-position="top">
        <div class="form-grid">
          <el-form-item label="所属库房" prop="fkWarehouseId">
            <el-select v-model="positionForm.fkWarehouseId" style="width: 100%">
              <el-option v-for="item in warehouses" :key="item.id" :label="item.warehouseName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="仓位编号" prop="positionCode"><el-input v-model="positionForm.positionCode" placeholder="如 A-06" /></el-form-item>
          <el-form-item label="仓位名称"><el-input v-model="positionForm.positionName" placeholder="如 A 区 06 号柜" /></el-form-item>
          <el-form-item label="是否启用">
            <el-radio-group v-model="positionForm.isEnabled">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="positionDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePosition">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新建盘点 -->
    <el-dialog v-model="checkDialog" title="新建盘点任务" width="520px" append-to-body>
      <el-form ref="checkRef" :model="checkForm" :rules="checkRules" label-position="top">
        <el-form-item label="盘点库房" prop="fkWarehouseId">
          <el-select v-model="checkForm.fkWarehouseId" style="width: 100%">
            <el-option v-for="item in warehouses" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="盘点人"><el-input v-model="checkForm.checkUserName" /></el-form-item>
          <el-form-item label="实盘数量"><el-input-number v-model="checkForm.realCount" :min="0" :controls="false" style="width: 100%" /></el-form-item>
          <el-form-item label="盘点结果" class="full">
            <el-input v-model="checkForm.checkResult" type="textarea" :rows="3" placeholder="记录账实相符情况或差异原因" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="checkDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCheck">生成盘点单</el-button>
      </template>
    </el-dialog>

    <!-- 完成盘点 -->
    <el-dialog v-model="finishDialog" title="完成盘点" width="480px" append-to-body>
      <el-form label-position="top">
        <el-form-item label="实盘数量">
          <el-input-number v-model="finishForm.realCount" :min="0" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="盘点结果">
          <el-input v-model="finishForm.checkResult" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveFinish">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import {
  createPosition, createStockCheck, createWarehouse, deletePosition, deleteWarehouse,
  finishStockCheck, listPositions, listStockChecks, listWarehouses, updatePosition, updateWarehouse
} from '@/api/warehouse'
import { useMetaStore } from '@/stores/meta'
import { CHECK_STATUS } from '@/utils/constants'
import { today } from '@/utils/format'

/**
 * 库房管理
 * 覆盖 1.2.3（3）（4）与用例 007：
 * 库房信息维护、仓位划分、库存盘点与差异处理。
 * 业务约束：仓位被藏品占用时不允许删除（后端校验 + 前端提示）。
 */
const meta = useMetaStore()
const tab = ref('warehouse')

const warehouses = ref([])
const positions = ref([])
const checks = ref([])
const positionLoading = ref(false)
const checkLoading = ref(false)
const saving = ref(false)

const positionWarehouse = ref('')
const checkWarehouse = ref('')

/* ---------------- 库房 ---------------- */
const warehouseDialog = ref(false)
const warehouseRef = ref(null)
const warehouseForm = reactive({ id: '', warehouseCode: '', warehouseName: '', location: '', keeperName: '', isEnabled: 1, memo: '' })
const warehouseRules = {
  warehouseCode: [{ required: true, message: '请填写库房编号', trigger: 'blur' }],
  warehouseName: [{ required: true, message: '请填写库房名称', trigger: 'blur' }]
}

function openWarehouse(row) {
  Object.assign(warehouseForm, {
    id: '', warehouseCode: '', warehouseName: '', location: '', keeperName: '', isEnabled: 1, memo: ''
  }, row || {})
  warehouseDialog.value = true
}

async function loadWarehouses() {
  warehouses.value = await listWarehouses()
}

async function saveWarehouse() {
  const valid = await warehouseRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (warehouseForm.id) await updateWarehouse(warehouseForm.id, { ...warehouseForm })
    else await createWarehouse({ ...warehouseForm })
    ElMessage.success('保存成功')
    warehouseDialog.value = false
    await loadWarehouses()
    await meta.ensure(true)
  } finally {
    saving.value = false
  }
}

async function removeWarehouse(row) {
  await ElMessageBox.confirm(`确定删除库房「${row.warehouseName}」吗？库房内仍有藏品时不允许删除。`, '删除确认', { type: 'warning' })
  await deleteWarehouse(row.id)
  ElMessage.success('已删除')
  await loadWarehouses()
}

/* ---------------- 仓位 ---------------- */
const positionDialog = ref(false)
const positionRef = ref(null)
const positionForm = reactive({ id: '', positionCode: '', positionName: '', fkWarehouseId: '', isEnabled: 1 })
const positionRules = {
  positionCode: [{ required: true, message: '请填写仓位编号', trigger: 'blur' }],
  fkWarehouseId: [{ required: true, message: '请选择所属库房', trigger: 'change' }]
}

function openPosition(row) {
  Object.assign(positionForm, {
    id: '', positionCode: '', positionName: '', fkWarehouseId: positionWarehouse.value || '', isEnabled: 1
  }, row || {})
  positionDialog.value = true
}

async function loadPositions() {
  positionLoading.value = true
  try {
    positions.value = await listPositions({ warehouseId: positionWarehouse.value })
  } finally {
    positionLoading.value = false
  }
}

async function savePosition() {
  const valid = await positionRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (positionForm.id) await updatePosition(positionForm.id, { ...positionForm })
    else await createPosition({ ...positionForm })
    ElMessage.success('保存成功')
    positionDialog.value = false
    await Promise.all([loadPositions(), loadWarehouses()])
  } finally {
    saving.value = false
  }
}

async function removePosition(row) {
  await ElMessageBox.confirm(`确定删除仓位「${row.positionCode}」吗？`, '删除确认', { type: 'warning' })
  await deletePosition(row.id)
  ElMessage.success('已删除')
  await Promise.all([loadPositions(), loadWarehouses()])
}

/* ---------------- 盘点 ---------------- */
const checkDialog = ref(false)
const checkRef = ref(null)
const checkForm = reactive({ fkWarehouseId: '', checkUserName: '', realCount: 0, checkResult: '' })
const checkRules = {
  fkWarehouseId: [{ required: true, message: '请选择盘点库房', trigger: 'change' }]
}

const finishDialog = ref(false)
const finishForm = reactive({ id: '', realCount: 0, checkResult: '' })

function openCheck() {
  Object.assign(checkForm, { fkWarehouseId: '', checkUserName: '', realCount: 0, checkResult: '' })
  checkDialog.value = true
}

async function loadChecks() {
  checkLoading.value = true
  try {
    const result = await listStockChecks({ warehouseId: checkWarehouse.value, pageNum: 1, pageSize: 50 })
    checks.value = result.list || []
  } finally {
    checkLoading.value = false
  }
}

async function saveCheck() {
  const valid = await checkRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await createStockCheck({ ...checkForm, checkTime: today(), checkStatus: 2 })
    ElMessage.success('盘点单已生成')
    checkDialog.value = false
    loadChecks()
  } finally {
    saving.value = false
  }
}

function openFinish(row) {
  Object.assign(finishForm, { id: row.id, realCount: row.realCount, checkResult: row.checkResult })
  finishDialog.value = true
}

async function saveFinish() {
  saving.value = true
  try {
    await finishStockCheck(finishForm.id, { realCount: finishForm.realCount, checkResult: finishForm.checkResult })
    ElMessage.success('盘点已完成')
    finishDialog.value = false
    loadChecks()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await meta.ensure()
  await Promise.all([loadWarehouses(), loadPositions(), loadChecks()])
})
</script>

<style scoped>
.warehouse-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--sp-4); }
.warehouse-card { display: flex; flex-direction: column; }
.warehouse-code { font-size: var(--fs-micro); color: var(--text-muted); letter-spacing: 0.1em; }
.warehouse-name { margin: var(--sp-2) 0 4px; font-family: var(--font-display); font-size: 20px; font-weight: 500; color: var(--text-primary); }
.warehouse-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--sp-2);
  margin-top: var(--sp-4);
  padding-top: var(--sp-3);
  border-top: 1px solid var(--border);
}
.warehouse-stats b { display: block; margin-top: 4px; font-family: var(--font-display); font-size: 20px; font-weight: 500; color: var(--text-primary); }
.diff-alert { color: var(--danger); font-weight: 600; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 var(--sp-5); }
.form-grid .full { grid-column: 1 / -1; }
@media (max-width: 1100px) {
  .warehouse-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .warehouse-grid, .form-grid { grid-template-columns: 1fr; }
}
</style>
