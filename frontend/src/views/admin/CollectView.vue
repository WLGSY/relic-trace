<template>
  <div>
    <PageHeader title="藏品征集" index="01" subtitle="登记新征集的藏品信息，提交审核后进入编目建档">
      <template #actions>
        <el-button type="primary" @click="openCreate">新增征集</el-button>
      </template>
    </PageHeader>

    <!-- 快速查询 + 高级查询 -->
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="藏品名称 / 征集编号 / 征集人"
        clearable
        style="width: 240px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="query.applyStatus" placeholder="全部状态" clearable style="width: 130px" @change="search">
        <el-option v-for="(item, key) in APPLY_STATUS" :key="key" :label="item.label" :value="Number(key)" />
      </el-select>
      <el-button @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button link @click="advanced = !advanced">{{ advanced ? '收起高级查询' : '高级查询' }}</el-button>
      <div class="toolbar-actions">
        <el-button :disabled="!selection.length" @click="batchRemove">批量删除（{{ selection.length }}）</el-button>
      </div>
    </div>

    <div v-if="advanced" class="toolbar" style="margin-top: -12px">
      <el-select v-model="query.categoryId" placeholder="全部类别" clearable style="width: 130px" @change="search">
        <el-option v-for="item in meta.categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.era" placeholder="全部年代" clearable style="width: 130px" @change="search">
        <el-option v-for="item in eraOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.collectionWay" placeholder="全部征集方式" clearable style="width: 150px" @change="search">
        <el-option v-for="item in wayOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        value-format="YYYY-MM-DD"
        start-placeholder="征集开始日期"
        end-placeholder="结束日期"
        style="width: 260px"
        @change="onDateChange"
      />
      <span class="toolbar-meta">共 {{ total }} 条</span>
    </div>

    <el-table v-loading="loading" :data="list" row-key="id" @selection-change="(rows) => (selection = rows)">
      <el-table-column type="selection" width="42" :selectable="(row) => [0, 3].includes(row.applyStatus)" />
      <el-table-column prop="applyCode" label="征集编号" width="132">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.applyCode }}</span></template>
      </el-table-column>
      <el-table-column prop="relicName" label="藏品名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="类别" width="80" />
      <el-table-column prop="era" label="年代" width="80" />
      <el-table-column prop="collectionWay" label="征集方式" width="96">
        <template #default="{ row }">{{ meta.dictLabel('collection_way', row.collectionWay) }}</template>
      </el-table-column>
      <el-table-column prop="collectionTime" label="征集时间" width="110">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.collectionTime }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><StatusTag :map="APPLY_STATUS" :value="row.applyStatus" /></template>
      </el-table-column>
      <el-table-column prop="auditOpinion" label="审核意见" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <span :class="row.applyStatus === 3 ? 'opinion-reject' : 'muted'">{{ row.auditOpinion || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190" align="right" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openView(row)">查看</el-button>
          <el-button v-if="[0, 3].includes(row.applyStatus)" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="[0, 3].includes(row.applyStatus)" link type="primary" @click="submitRow(row)">提交</el-button>
          <el-button v-if="[0, 3].includes(row.applyStatus)" link type="danger" @click="removeRow(row)">删除</el-button>
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

    <!-- 新增 / 编辑 / 查看 -->
    <el-dialog v-model="dialog" :title="dialogTitle" width="820px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="readonly">
        <div class="form-grid">
          <el-form-item label="藏品名称" prop="relicName">
            <el-input v-model="form.relicName" placeholder="如 青釉莲瓣纹碗" />
          </el-form-item>
          <el-form-item label="藏品分类" prop="fkCategoryId">
            <el-select v-model="form.fkCategoryId" placeholder="请选择分类" style="width: 100%">
              <el-option v-for="item in meta.categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="年代">
            <el-select v-model="form.era" placeholder="请选择年代" clearable style="width: 100%">
              <el-option v-for="item in eraOptions" :key="item.value" :label="item.label" :value="item.label" />
            </el-select>
          </el-form-item>
          <el-form-item label="质地">
            <el-select v-model="form.texture" placeholder="请选择质地" clearable style="width: 100%">
              <el-option v-for="item in textureOptions" :key="item.value" :label="item.label" :value="item.label" />
            </el-select>
          </el-form-item>
          <el-form-item label="规格（尺寸 / 重量）">
            <el-input v-model="form.specification" placeholder="如 口径 18.2cm，高 7.6cm" />
          </el-form-item>
          <el-form-item label="藏品级别">
            <el-select v-model="form.relicLevel" placeholder="请选择级别" clearable style="width: 100%">
              <el-option v-for="(item, key) in RELIC_LEVEL" :key="key" :label="item.label" :value="Number(key)" />
            </el-select>
          </el-form-item>
          <el-form-item label="征集方式">
            <el-select v-model="form.collectionWay" placeholder="请选择征集方式" clearable style="width: 100%">
              <el-option v-for="item in wayOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="征集来源">
            <el-input v-model="form.collectionSource" placeholder="如 民间捐赠 / 湖南博物院调拨" />
          </el-form-item>
          <el-form-item label="征集价格（元）">
            <el-input-number v-model="form.collectionPrice" :min="0" :step="1000" :controls="false" style="width: 100%" />
          </el-form-item>
          <el-form-item label="征集人">
            <el-input v-model="form.collector" placeholder="经办人姓名" />
          </el-form-item>
          <el-form-item label="征集时间">
            <el-date-picker v-model="form.collectionTime" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="影像资料">
            <el-upload action="#" :auto-upload="false" :limit="3" :disabled="readonly">
              <el-button size="small">选择影像</el-button>
              <template #tip><div class="muted" style="margin-top: 4px">演示环境不上传文件，真实环境提交至 /api/files</div></template>
            </el-upload>
          </el-form-item>
          <el-form-item label="藏品描述" class="full">
            <el-input v-model="form.collectionDesc" type="textarea" :rows="3" maxlength="1000" show-word-limit placeholder="器形、纹饰、来源背景等" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialog = false">{{ readonly ? '关闭' : '取消' }}</el-button>
        <template v-if="!readonly">
          <el-button @click="save(false)">保存草稿</el-button>
          <el-button type="primary" :loading="saving" @click="save(true)">提交审核</el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import {
  batchDeleteCollections,
  createCollection,
  deleteCollection,
  listCollections,
  submitCollection,
  updateCollection
} from '@/api/collection'
import { useTable } from '@/composables/useTable'
import { useMetaStore } from '@/stores/meta'
import { APPLY_STATUS, RELIC_LEVEL } from '@/utils/constants'
import { today } from '@/utils/format'

/**
 * 藏品征集管理
 * 覆盖 1.2.2（1）与 2.3.2 用例 003：
 * 新增 / 编辑 / 保存草稿 / 提交审核 / 删除 / 批量删除 / 快速查询 / 高级查询。
 * 交互约束：已提交（待审核）不可编辑；驳回后可修改并重新提交。
 */
const meta = useMetaStore()
const advanced = ref(false)
const selection = ref([])
const dateRange = ref([])

const { loading, list, total, query, load, search, reset, onPageChange } = useTable(listCollections, {
  defaultQuery: { keyword: '', applyStatus: '', categoryId: '', era: '', collectionWay: '', beginTime: '', endTime: '' }
})

const eraOptions = computed(() => meta.dictOptions('era'))
const textureOptions = computed(() => meta.dictOptions('texture'))
const wayOptions = computed(() => meta.dictOptions('collection_way'))

/* ---------------- 表单 ---------------- */
const dialog = ref(false)
const readonly = ref(false)
const saving = ref(false)
const formRef = ref(null)

const emptyForm = () => ({
  id: null,
  relicName: '',
  fkCategoryId: '',
  era: '',
  texture: '',
  specification: '',
  relicLevel: null,
  collectionWay: '',
  collectionSource: '',
  collectionPrice: 0,
  collector: '',
  collectionTime: today(),
  collectionDesc: ''
})

const form = reactive(emptyForm())

const rules = {
  relicName: [{ required: true, message: '请填写藏品名称', trigger: 'blur' }],
  fkCategoryId: [{ required: true, message: '请选择藏品分类', trigger: 'change' }]
}

const dialogTitle = computed(() => (readonly.value ? '征集详情' : form.id ? '编辑征集申请' : '新增征集'))

function openCreate() {
  Object.assign(form, emptyForm())
  readonly.value = false
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, emptyForm(), row)
  readonly.value = false
  dialog.value = true
}

function openView(row) {
  Object.assign(form, emptyForm(), row)
  readonly.value = true
  dialog.value = true
}

function onDateChange(value) {
  query.beginTime = value?.[0] || ''
  query.endTime = value?.[1] || ''
  search()
}

async function save(submit) {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.id) {
      await updateCollection(form.id, { ...form })
      if (submit) await submitCollection(form.id)
    } else {
      // 新增接口返回新单据 id，需要据此再调提交接口，否则"保存并提交"只会留下草稿
      const id = await createCollection({ ...form })
      if (submit && id) await submitCollection(id)
    }
    ElMessage.success(submit ? '已提交审核' : '草稿已保存')
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function submitRow(row) {
  await submitCollection(row.id)
  ElMessage.success('已提交审核')
  load()
}

async function removeRow(row) {
  await ElMessageBox.confirm(`确定删除征集申请「${row.applyCode}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await deleteCollection(row.id)
  ElMessage.success('已删除')
  load()
}

async function batchRemove() {
  const ids = selection.value.map((item) => item.id)
  await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 条征集申请吗？`, '批量删除', { type: 'warning' })
  const result = await batchDeleteCollections(ids)
  ElMessage.success(`已删除 ${result.deleted} 条`)
  load()
}

onMounted(() => meta.ensure())
</script>

<style scoped>
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 var(--sp-5); }
.form-grid .full { grid-column: 1 / -1; }
.opinion-reject { color: var(--danger); font-size: var(--fs-micro); }
@media (max-width: 640px) {
  .form-grid { grid-template-columns: 1fr; }
}
</style>
