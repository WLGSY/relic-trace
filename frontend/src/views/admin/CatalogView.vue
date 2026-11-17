<template>
  <div>
    <PageHeader title="编目建档" index="02" subtitle="为审核通过的征集藏品建立正式档案，生成唯一编号与溯源二维码">
      <template #actions>
        <router-link class="btn-line" to="/admin/archive">前往藏品档案 →</router-link>
      </template>
    </PageHeader>

    <div class="flow-note">
      <span class="step">01 征集审核通过</span>
      <span class="arrow">→</span>
      <span class="step active">02 编目建档</span>
      <span class="arrow">→</span>
      <span class="step">03 入库登记</span>
      <span class="arrow">→</span>
      <span class="step">04 溯源可查</span>
    </div>

    <SectionHead title="待编目藏品" :sub="`${pending.length} 件征集已通过审核，尚未建立档案`" />

    <el-table v-loading="loading" :data="pending" row-key="id">
      <el-table-column prop="applyCode" label="征集编号" width="132">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.applyCode }}</span></template>
      </el-table-column>
      <el-table-column prop="relicName" label="藏品名称" min-width="150" />
      <el-table-column prop="categoryName" label="类别" width="90" />
      <el-table-column prop="era" label="年代" width="90" />
      <el-table-column label="级别" width="80">
        <template #default="{ row }">{{ RELIC_LEVEL[row.relicLevel]?.label || '待定' }}</template>
      </el-table-column>
      <el-table-column prop="collectionWay" label="征集方式" width="96" />
      <el-table-column prop="collectionTime" label="征集时间" width="110" />
      <el-table-column label="操作" width="110" align="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openCatalog(row)">编目建档</el-button>
        </template>
      </el-table-column>
    </el-table>

    <EmptyState
      v-if="!loading && !pending.length"
      big="✓"
      title="暂无待编目藏品"
      msg="所有已通过的征集均已完成建档"
    />

    <section class="section">
      <SectionHead title="最近建档" sub="编目完成后自动生成藏品编号与二维码" link="/admin/archive" />
      <el-table :data="recent" size="small">
        <el-table-column prop="relicCode" label="藏品编号" width="140">
          <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.relicCode }}</span></template>
        </el-table-column>
        <el-table-column prop="relicName" label="藏品名称" min-width="150" />
        <el-table-column prop="categoryName" label="类别" width="90" />
        <el-table-column prop="createUserName" label="建档人" width="100" />
        <el-table-column prop="createTime" label="建档时间" width="160" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><StatusTag :map="STOCK_STATUS" :value="row.stockStatus" /></template>
        </el-table-column>
      </el-table>
    </section>

    <!-- 编目表单 -->
    <el-dialog v-model="dialog" title="编目建档" width="860px" append-to-body destroy-on-close>
      <div class="catalog-source">
        <div>
          <div class="panel-label">来源征集</div>
          <div class="source-code mono">{{ source.applyCode || '—' }}</div>
        </div>
        <div>
          <div class="panel-label">藏品名称</div>
          <div>{{ source.relicName }}</div>
        </div>
        <div>
          <div class="panel-label">征集方式</div>
          <div>{{ source.collectionWay || '—' }}</div>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="藏品编号">
            <el-input v-model="form.relicCode" placeholder="留空则按系统参数规则自动生成">
              <template #append>全局唯一</template>
            </el-input>
          </el-form-item>
          <el-form-item label="藏品名称" prop="relicName">
            <el-input v-model="form.relicName" />
          </el-form-item>
          <el-form-item label="藏品分类" prop="fkCategoryId">
            <el-select v-model="form.fkCategoryId" style="width: 100%">
              <el-option v-for="item in meta.categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="年代">
            <el-input v-model="form.era" placeholder="如 五代" />
          </el-form-item>
          <el-form-item label="质地">
            <el-input v-model="form.texture" placeholder="如 陶瓷" />
          </el-form-item>
          <el-form-item label="规格">
            <el-input v-model="form.specification" placeholder="尺寸 / 重量" />
          </el-form-item>
          <el-form-item label="藏品级别" prop="relicLevel">
            <el-select v-model="form.relicLevel" style="width: 100%">
              <el-option v-for="(item, key) in RELIC_LEVEL" :key="key" :label="item.label" :value="Number(key)" />
            </el-select>
          </el-form-item>
          <el-form-item label="完残状况" prop="integrityStatus">
            <el-select v-model="form.integrityStatus" style="width: 100%">
              <el-option v-for="item in integrityOptions" :key="item.value" :label="item.label" :value="item.label" />
            </el-select>
          </el-form-item>
          <el-form-item label="存放库房">
            <el-select v-model="form.fkWarehouseId" clearable placeholder="可稍后在入库时指定" style="width: 100%" @change="onWarehouseChange">
              <el-option v-for="item in meta.warehouseOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="存放仓位">
            <el-select v-model="form.fkPositionId" clearable placeholder="请选择仓位" style="width: 100%">
              <el-option v-for="item in positionOptions" :key="item.id" :label="`${item.positionCode} ${item.positionName}`" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="入藏时间">
            <el-date-picker v-model="form.storageTime" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="是否公开">
            <el-radio-group v-model="form.isPublic">
              <el-radio :value="1">公开展示</el-radio>
              <el-radio :value="0">保密</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="藏品描述" class="full">
            <el-input v-model="form.relicDesc" type="textarea" :rows="3" maxlength="1000" show-word-limit />
          </el-form-item>
        </div>
      </el-form>

      <div class="qr-preview">
        <div class="qr-demo" aria-hidden="true" />
        <div>
          <div class="panel-label">溯源二维码（预览）</div>
          <div class="mono" style="margin-top: 6px">relic://trace/{{ form.relicCode || '待生成' }}</div>
          <div class="muted" style="margin-top: 4px">建档后生成，一物一码，可用于打印藏品标签</div>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">确认建档</el-button>
      </template>
    </el-dialog>

    <!-- 建档结果 -->
    <el-dialog v-model="resultVisible" title="建档成功" width="480px" append-to-body>
      <div v-if="created" class="result">
        <div class="qr-demo" aria-hidden="true" />
        <div class="result-code mono">{{ created.relicCode }}</div>
        <div class="result-name">{{ created.relicName }}</div>
        <p class="muted" style="margin-top: 8px">
          已生成溯源二维码，藏品状态为「在库」。可打印标签粘贴于藏品包装。
        </p>
      </div>
      <template #footer>
        <el-button @click="resultVisible = false">关闭</el-button>
        <el-button @click="printLabel">打印藏品标签</el-button>
        <el-button type="primary" @click="goArchive">查看档案</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import SectionHead from '@/components/SectionHead.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import { listCollections } from '@/api/collection'
import { createRelic, listRelics } from '@/api/relic'
import { listPositions } from '@/api/warehouse'
import { useMetaStore } from '@/stores/meta'
import { RELIC_LEVEL, STOCK_STATUS } from '@/utils/constants'
import { formatDate } from '@/utils/format'
import { printHtml } from '@/utils/export'

/**
 * 编目建档
 * 覆盖 1.2.2（2）与用例 004：
 * 选择已通过审核的征集 → 完善档案 → 生成唯一编号与二维码 → 提交生效。
 */
const router = useRouter()
const meta = useMetaStore()

const loading = ref(false)
const pending = ref([])
const recent = ref([])
const dialog = ref(false)
const saving = ref(false)
const resultVisible = ref(false)
const created = ref(null)
const formRef = ref(null)
const source = ref({})
const positionOptions = ref([])

const integrityOptions = ref([])

const emptyForm = () => ({
  relicCode: '',
  relicName: '',
  fkCategoryId: '',
  era: '',
  texture: '',
  specification: '',
  relicLevel: 2,
  integrityStatus: '基本完好',
  fkWarehouseId: '',
  fkPositionId: '',
  storageTime: formatDate(new Date()),
  isPublic: 1,
  relicDesc: ''
})

const form = reactive(emptyForm())

const rules = {
  relicName: [{ required: true, message: '请填写藏品名称', trigger: 'blur' }],
  fkCategoryId: [{ required: true, message: '请选择藏品分类', trigger: 'change' }],
  relicLevel: [{ required: true, message: '请选择藏品级别', trigger: 'change' }],
  integrityStatus: [{ required: true, message: '请选择完残状况', trigger: 'change' }]
}

async function loadPending() {
  loading.value = true
  try {
    const result = await listCollections({ applyStatus: 2, pageNum: 1, pageSize: 100 })
    pending.value = (result.list || []).filter((item) => !item.relicCode)
  } finally {
    loading.value = false
  }
}

async function loadRecent() {
  const result = await listRelics({ pageNum: 1, pageSize: 5 })
  recent.value = result.list || []
}

function openCatalog(row) {
  source.value = row
  Object.assign(form, emptyForm(), {
    relicName: row.relicName,
    fkCategoryId: row.fkCategoryId,
    era: row.era,
    texture: row.texture,
    specification: row.specification,
    relicLevel: row.relicLevel || 2,
    relicDesc: row.collectionDesc,
    relicCode: ''
  })
  positionOptions.value = []
  dialog.value = true
}

async function onWarehouseChange(id) {
  form.fkPositionId = ''
  positionOptions.value = id ? await listPositions({ warehouseId: id }) : []
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    created.value = await createRelic({ ...form, fkCollectionApplyId: source.value.id })
    dialog.value = false
    resultVisible.value = true
    await Promise.all([loadPending(), loadRecent()])
  } finally {
    saving.value = false
  }
}

function printLabel() {
  if (!created.value) return
  printHtml(
    `藏品标签 ${created.value.relicCode}`,
    `<h1>${created.value.relicName}</h1>
     <div class="sub">${created.value.relicCode} · ${created.value.categoryName || ''}</div>
     <div class="center"><div class="qr" style="margin:0 auto"></div></div>
     <table>
       <tr><th>年代</th><td>${created.value.era || '—'}</td></tr>
       <tr><th>级别</th><td>${RELIC_LEVEL[created.value.relicLevel]?.label || '—'}</td></tr>
       <tr><th>规格</th><td>${created.value.specification || '—'}</td></tr>
       <tr><th>入藏时间</th><td>${created.value.storageTime || '—'}</td></tr>
     </table>`
  )
}

function goArchive() {
  resultVisible.value = false
  router.push('/admin/archive')
}

onMounted(async () => {
  await meta.ensure()
  integrityOptions.value = meta.dictOptions('integrity_status')
  await Promise.all([loadPending(), loadRecent()])
})
</script>

<style scoped>
.flow-note {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  flex-wrap: wrap;
  padding: var(--sp-3) var(--sp-4);
  margin-bottom: var(--sp-6);
  border: 1px solid var(--border);
  font-size: var(--fs-micro);
  letter-spacing: 0.04em;
}
.flow-note .step { color: var(--text-muted); }
.flow-note .step.active { color: var(--ink-900); font-weight: 600; }
.flow-note .arrow { color: var(--line-strong); }
.catalog-source {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--sp-4);
  padding: 0 0 var(--sp-4);
  margin-bottom: var(--sp-4);
  border-bottom: 1px solid var(--border);
  font-size: var(--fs-caption);
}
.source-code { margin-top: 4px; color: var(--text-primary); }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 var(--sp-5); }
.form-grid .full { grid-column: 1 / -1; }
.qr-preview {
  display: flex;
  align-items: center;
  gap: var(--sp-5);
  margin-top: var(--sp-4);
  padding-top: var(--sp-4);
  border-top: 1px solid var(--border);
}
.qr-preview .qr-demo { margin: 0; width: 96px; height: 96px; }
.result { text-align: center; }
.result-code { font-size: 20px; color: var(--ink-900); margin-top: var(--sp-3); letter-spacing: 0.04em; }
.result-name { font-family: var(--font-display); font-size: 18px; margin-top: 4px; }
@media (max-width: 640px) {
  .form-grid, .catalog-source { grid-template-columns: 1fr; }
  .qr-preview { flex-direction: column; align-items: flex-start; gap: var(--sp-3); }
}
</style>
