<template>
  <div>
    <PageHeader title="数据字典" index="12" subtitle="统一维护下拉选项与枚举文案，前台检索与后台表单共用同一套字典">
      <template #actions>
        <el-button @click="exportList">导出字典</el-button>
        <el-button type="primary" @click="openCreate">新增字典项</el-button>
      </template>
    </PageHeader>

    <div class="dict-layout">
      <!-- 左：字典类型导航 -->
      <aside class="dict-types">
        <div class="panel-label" style="margin-bottom: var(--sp-3)">字典类型</div>
        <button class="dict-type" :class="{ active: !query.dictType }" @click="pickType('')">
          <span>全部类型</span>
          <span class="mono dict-type-count">{{ items.length }}</span>
        </button>
        <button
          v-for="type in typeList"
          :key="type.dictType"
          class="dict-type"
          :class="{ active: query.dictType === type.dictType }"
          @click="pickType(type.dictType)"
        >
          <span>{{ type.title }}</span>
          <span class="mono dict-type-count">{{ type.count }}</span>
        </button>
        <p class="dict-tip">字典编码在录入表单、检索筛选与统计图表中被直接引用，修改编码前请确认无业务数据依赖。</p>
      </aside>

      <!-- 右：字典项表格 -->
      <section>
        <div class="toolbar">
          <el-input
            v-model="query.keyword"
            placeholder="字典名称 / 编码"
            clearable
            style="width: 220px"
            @keyup.enter="load"
            @clear="load"
          />
          <el-button @click="load">查询</el-button>
          <el-button @click="clearFilter">重置</el-button>
          <div class="toolbar-actions">
            <span class="muted mono" style="font-size: 12px">{{ filtered.length }} 项</span>
          </div>
        </div>

        <el-table v-loading="loading" :data="filtered" row-key="id">
          <el-table-column label="字典类型" width="150">
            <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.dictType }}</span></template>
          </el-table-column>
          <el-table-column label="编码" width="130">
            <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.dictCode }}</span></template>
          </el-table-column>
          <el-table-column prop="dictLabel" label="显示名称" min-width="150" />
          <el-table-column label="排序" width="70" align="center">
            <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.sortNo }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="92">
            <template #default="{ row }"><StatusTag :map="ENABLED_STATUS" :value="row.isEnabled" /></template>
          </el-table-column>
          <el-table-column label="备注" min-width="180">
            <template #default="{ row }"><span class="muted">{{ row.remark || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="right" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="removeRow(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑字典项' : '新增字典项'" width="560px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="字典类型" prop="dictType">
            <el-select v-model="form.dictType" style="width: 100%" filterable allow-create default-first-option>
              <el-option v-for="type in typeList" :key="type.dictType" :label="`${type.title}（${type.dictType}）`" :value="type.dictType" />
            </el-select>
          </el-form-item>
          <el-form-item label="字典编码" prop="dictCode">
            <el-input v-model="form.dictCode" placeholder="如 1 / porcelain" />
          </el-form-item>
          <el-form-item label="显示名称" prop="dictLabel"><el-input v-model="form.dictLabel" /></el-form-item>
          <el-form-item label="排序号">
            <el-input-number v-model="form.sortNo" :min="1" :max="999" controls-position="right" style="width: 100%" />
          </el-form-item>
          <el-form-item label="启用状态">
            <el-radio-group v-model="form.isEnabled">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注" class="full">
            <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="120" show-word-limit />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { createDict, deleteDict, listDicts, updateDict } from '@/api/system'
import { exportCsv } from '@/utils/export'

/**
 * 数据字典
 * 对应文档 1.2.3 系统管理模块：字典类型 / 字典项的增删改查。
 * 左侧为字典类型导航，右侧为所选类型下的字典项明细。
 */
const ENABLED_STATUS = {
  1: { label: '启用', tone: 'jade' },
  0: { label: '停用', tone: 'ink' }
}

/** 字典类型的中文标题（未在映射表中的类型直接展示原始编码） */
const TYPE_TITLE = {
  relic_level: '藏品级别',
  relic_category: '藏品类别',
  integrity_status: '完残状况',
  stock_status: '库存状态',
  collection_way: '征集方式',
  record_type: '出入库类型',
  loan_status: '借调状态',
  apply_status: '申请状态',
  trace_type: '溯源节点',
  file_type: '附件类型',
  texture: '质地',
  era: '年代'
}

const loading = ref(false)
const saving = ref(false)
const items = ref([])
const query = reactive({ dictType: '', keyword: '' })

/** 类型导航：由全量数据聚合，避免额外维护一份类型表 */
const typeList = computed(() => {
  const map = new Map()
  items.value.forEach((item) => {
    const current = map.get(item.dictType) || { dictType: item.dictType, title: TYPE_TITLE[item.dictType] || item.dictType, count: 0 }
    current.count += 1
    map.set(item.dictType, current)
  })
  return [...map.values()].sort((a, b) => (a.dictType < b.dictType ? -1 : 1))
})

/** 当前视图数据：类型与关键词均在前端二次过滤，保证切换类型零延迟 */
const filtered = computed(() =>
  items.value
    .filter((item) => (!query.dictType ? true : item.dictType === query.dictType))
    .filter((item) =>
      !query.keyword
        ? true
        : item.dictLabel.includes(query.keyword) || item.dictCode.toLowerCase().includes(query.keyword.toLowerCase())
    )
    .sort((a, b) => (a.dictType === b.dictType ? a.sortNo - b.sortNo : a.dictType < b.dictType ? -1 : 1))
)

async function load() {
  loading.value = true
  try {
    items.value = (await listDicts({})) || []
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

function pickType(type) {
  query.dictType = type
}

function clearFilter() {
  query.dictType = ''
  query.keyword = ''
}

/* ---------------- 新增 / 编辑 ---------------- */
const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({})

const rules = {
  dictType: [{ required: true, message: '请选择或填写字典类型', trigger: 'change' }],
  dictCode: [{ required: true, message: '请填写字典编码', trigger: 'blur' }],
  dictLabel: [{ required: true, message: '请填写显示名称', trigger: 'blur' }]
}

function openCreate() {
  Object.keys(form).forEach((key) => delete form[key])
  const maxSort = Math.max(0, ...items.value.filter((item) => item.dictType === query.dictType).map((item) => item.sortNo || 0))
  Object.assign(form, {
    dictType: query.dictType || '',
    dictCode: '',
    dictLabel: '',
    sortNo: maxSort + 1,
    isEnabled: 1,
    remark: ''
  })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.id) {
      await updateDict(form.id, { ...form })
      ElMessage.success('字典项已更新')
    } else {
      await createDict({ ...form })
      ElMessage.success('字典项已新增')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function removeRow(row) {
  await ElMessageBox.confirm(
    `删除后该项将从所有下拉选项中移除，已引用该编码的历史数据仍保留原值。确定删除「${row.dictLabel}」吗？`,
    '删除字典项',
    { type: 'warning', confirmButtonText: '确认删除' }
  )
  await deleteDict(row.id)
  ElMessage.success('已删除')
  load()
}

function exportList() {
  exportCsv(
    `数据字典-${new Date().toISOString().slice(0, 10)}`,
    ['字典类型', '类型名称', '字典编码', '显示名称', '排序', '状态', '备注'],
    filtered.value.map((row) => [
      row.dictType,
      TYPE_TITLE[row.dictType] || '',
      row.dictCode,
      row.dictLabel,
      row.sortNo,
      ENABLED_STATUS[row.isEnabled]?.label || '',
      row.remark || ''
    ])
  )
  ElMessage.success('已导出当前视图数据')
}

onMounted(load)
</script>

<style scoped>
.dict-layout { display: grid; grid-template-columns: 216px minmax(0, 1fr); gap: var(--sp-6); align-items: start; }
.dict-types { border: 1px solid var(--border); padding: var(--sp-4); position: sticky; top: calc(var(--header-h) + var(--sp-5)); }
.dict-type {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 7px 8px;
  border: 0;
  border-left: 2px solid transparent;
  background: none;
  font: inherit;
  font-size: 13.5px;
  color: var(--text-secondary);
  text-align: left;
  cursor: pointer;
  transition: color 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}
.dict-type:hover { color: var(--ink-900); background: var(--paper-0); }
.dict-type.active { color: var(--ink-900); border-left-color: var(--ink-900); background: var(--paper-0); font-weight: 600; }
.dict-type-count { font-size: 11.5px; color: var(--text-muted); }
.dict-tip { margin-top: var(--sp-4); padding-top: var(--sp-3); border-top: 1px solid var(--border); font-size: 12px; line-height: 1.75; color: var(--text-muted); }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 var(--sp-5); }
.form-grid .full { grid-column: 1 / -1; }
@media (max-width: 980px) {
  .dict-layout { grid-template-columns: 1fr; }
  .dict-types { position: static; }
}
@media (max-width: 640px) {
  .form-grid { grid-template-columns: 1fr; }
}
</style>
