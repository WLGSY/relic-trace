<template>
  <div>
    <PageHeader title="藏品档案" index="03" subtitle="编目建档后的藏品主数据，支持检索、查看、修改与逻辑删除">
      <template #actions>
        <el-button @click="exportList">导出当前结果</el-button>
        <router-link class="btn-line" to="/admin/catalog">去编目建档 →</router-link>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="藏品名称 / 编号 / 年代 / 质地"
        clearable
        style="width: 240px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="query.fkCategoryId" placeholder="全部类别" clearable style="width: 126px" @change="search">
        <el-option v-for="item in meta.categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.relicLevel" placeholder="全部级别" clearable style="width: 116px" @change="search">
        <el-option v-for="(item, key) in RELIC_LEVEL" :key="key" :label="item.label" :value="Number(key)" />
      </el-select>
      <el-select v-model="query.stockStatus" placeholder="全部状态" clearable style="width: 126px" @change="search">
        <el-option v-for="(item, key) in STOCK_STATUS" :key="key" :label="item.label" :value="Number(key)" />
      </el-select>
      <el-select v-model="query.isPublic" placeholder="公开范围" clearable style="width: 122px" @change="search">
        <el-option label="公开展示" :value="1" />
        <el-option label="保密" :value="0" />
      </el-select>
      <el-button @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <div class="toolbar-actions">
        <el-button :disabled="!selection.length" @click="batchRemove">批量删除（{{ selection.length }}）</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="list" row-key="id" @selection-change="(rows) => (selection = rows)">
      <el-table-column type="selection" width="42" />
      <el-table-column prop="relicCode" label="藏品编号" width="124">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.relicCode }}</span></template>
      </el-table-column>
      <el-table-column label="藏品名称" min-width="170">
        <template #default="{ row }">
          <a class="table-link" @click="openDetail(row)">{{ row.relicName }}</a>
          <span v-if="row.isPublic === 0" class="tag ink" style="margin-left: 6px">保密</span>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="类别" width="84" />
      <el-table-column prop="era" label="年代" width="84" />
      <el-table-column label="级别" width="76">
        <template #default="{ row }">{{ RELIC_LEVEL[row.relicLevel]?.label || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="96">
        <template #default="{ row }"><StatusTag :map="STOCK_STATUS" :value="row.stockStatus" /></template>
      </el-table-column>
      <el-table-column label="存放位置" width="150">
        <template #default="{ row }">
          <span class="muted">{{ row.warehouseName }}<template v-if="row.positionName !== '—'"> · {{ row.positionName }}</template></span>
        </template>
      </el-table-column>
      <el-table-column prop="storageTime" label="入藏时间" width="110">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.storageTime || '—' }}</span></template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="right" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">查看</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="removeRow(row)">删除</el-button>
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

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawer" size="640px" :title="detail?.relicName || '藏品详情'">
      <div v-if="detail" class="stack" style="gap: 24px">
        <div class="detail-head">
          <div class="detail-figure">
            <ArtFrame :image="detail.cover" :alt="detail.relicName" />
          </div>
          <div>
            <div class="panel-label">藏品编号</div>
            <div class="mono" style="font-size: 15px; margin: 4px 0 10px">{{ detail.relicCode }}</div>
            <div class="row" style="gap: 6px; flex-wrap: wrap">
              <StatusTag :map="STOCK_STATUS" :value="detail.stockStatus" />
              <span class="tag ink">{{ RELIC_LEVEL[detail.relicLevel]?.label || '未定级' }}</span>
              <span class="tag">{{ detail.categoryName }}</span>
            </div>
            <p class="muted" style="margin-top: 10px">{{ detail.relicDesc || '暂无描述' }}</p>
          </div>
        </div>

        <div>
          <div class="card-title" style="margin-bottom: 12px">档案信息</div>
          <div class="kv-list">
            <div class="kv-row"><span class="kv-key">年代</span><span class="kv-value">{{ detail.era || '—' }}</span></div>
            <div class="kv-row"><span class="kv-key">质地</span><span class="kv-value">{{ detail.texture || '—' }}</span></div>
            <div class="kv-row"><span class="kv-key">规格</span><span class="kv-value">{{ detail.specification || '—' }}</span></div>
            <div class="kv-row"><span class="kv-key">完残</span><span class="kv-value">{{ detail.integrityStatus || '—' }}</span></div>
            <div class="kv-row"><span class="kv-key">库房</span><span class="kv-value">{{ detail.warehouseName }}</span></div>
            <div class="kv-row"><span class="kv-key">仓位</span><span class="kv-value">{{ detail.positionName }}</span></div>
            <div class="kv-row"><span class="kv-key">来源</span><span class="kv-value">{{ detail.collectionSource || '—' }}</span></div>
            <div class="kv-row"><span class="kv-key">入藏</span><span class="kv-value">{{ detail.storageTime || '—' }}</span></div>
            <div class="kv-row"><span class="kv-key">建档人</span><span class="kv-value">{{ detail.createUserName }}</span></div>
            <div class="kv-row"><span class="kv-key">建档时间</span><span class="kv-value">{{ formatDateTime(detail.createTime) }}</span></div>
          </div>
        </div>

        <div>
          <div class="card-title" style="margin-bottom: 12px">溯源时间线</div>
          <TraceTimeline :traces="detailTraces" hide-relic />
        </div>

        <div>
          <div class="card-title" style="margin-bottom: 12px">
            影像资料
            <el-upload
              class="upload-inline"
              accept=".jpg,.jpeg,.png,.webp,.gif,.bmp,.pdf"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="(options) => handleUpload(options.file)"
            >
              <button class="link-btn" type="button" :disabled="uploading">
                {{ uploading ? '上传中…' : '上传影像' }}
              </button>
            </el-upload>
          </div>
          <div v-for="file in detailAttachments" :key="file.id" class="list-row">
            <a class="list-row-main" :href="assetUrl(file.filePath)" target="_blank" rel="noreferrer">
              {{ file.fileName }}
            </a>
            <span class="list-row-meta">{{ formatFileSize(file.fileSize) }}</span>
            <button class="link-btn" type="button" @click="removeAttachment(file)">删除</button>
          </div>
          <div v-if="!detailAttachments.length" class="muted">暂无影像资料</div>
        </div>

        <div class="drawer-qr">
          <img
            v-if="detail.qrcode?.imageUrl"
            class="drawer-qr-img"
            :src="assetUrl(detail.qrcode.imageUrl)"
            :alt="`${detail.relicName} 溯源二维码`"
          />
          <div v-else class="qr-demo" aria-hidden="true" />
          <div>
            <div class="panel-label">溯源二维码</div>
            <div class="mono" style="margin-top: 6px">{{ detail.qrcode?.content || detail.qrcode?.qrContent || detail.qrcodeContent }}</div>
            <div class="muted" style="margin-top: 4px">累计扫码 {{ detail.qrcode?.scanCount || 0 }} 次</div>
            <div class="qr-actions">
              <a
                v-if="detail.qrcode?.imageUrl"
                :href="assetUrl(detail.qrcode.imageUrl)"
                :download="`${detail.relicCode || detail.id}-二维码.png`"
              >下载二维码</a>
              <button class="link-btn" @click="regenerateQr">重新生成</button>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 编辑 -->
    <el-dialog v-model="editVisible" title="修改藏品档案" width="760px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="藏品编号"><el-input v-model="form.relicCode" disabled /></el-form-item>
          <el-form-item label="藏品名称" prop="relicName"><el-input v-model="form.relicName" /></el-form-item>
          <el-form-item label="藏品分类" prop="fkCategoryId">
            <el-select v-model="form.fkCategoryId" style="width: 100%">
              <el-option v-for="item in meta.categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="年代"><el-input v-model="form.era" /></el-form-item>
          <el-form-item label="质地"><el-input v-model="form.texture" /></el-form-item>
          <el-form-item label="规格"><el-input v-model="form.specification" /></el-form-item>
          <el-form-item label="级别">
            <el-select v-model="form.relicLevel" style="width: 100%">
              <el-option v-for="(item, key) in RELIC_LEVEL" :key="key" :label="item.label" :value="Number(key)" />
            </el-select>
          </el-form-item>
          <el-form-item label="完残状况"><el-input v-model="form.integrityStatus" /></el-form-item>
          <el-form-item label="公开范围">
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
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
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
import ArtFrame from '@/components/ArtFrame.vue'
import TraceTimeline from '@/components/TraceTimeline.vue'
import { batchDeleteRelics, deleteRelic, deleteRelicAttachment, getRelic, listRelics, rebuildQrCode, updateRelic, uploadRelicAttachment } from '@/api/relic'
import { useTable } from '@/composables/useTable'
import { useMetaStore } from '@/stores/meta'
import { assetUrl } from '@/utils/asset'
import { RELIC_LEVEL, STOCK_STATUS } from '@/utils/constants'
import { formatDateTime } from '@/utils/format'
import { exportCsv } from '@/utils/export'

/**
 * 藏品档案
 * 覆盖 1.2.2（3）（4）（5）（6）：
 * 修改（记录修改日志）、逻辑删除、快速查询与高级查询。
 */
const meta = useMetaStore()

const { loading, list, total, query, load, search, reset, onPageChange } = useTable(listRelics, {
  defaultQuery: { keyword: '', fkCategoryId: '', relicLevel: '', stockStatus: '', isPublic: '' }
})

const selection = ref([])
const drawer = ref(false)
const detail = ref(null)
// 后端字段为 traceList / attachmentList，兼容早期 mock 的 traces / attachments
const detailTraces = computed(() => [...(detail.value?.traceList || detail.value?.traces || [])].reverse())
const detailAttachments = computed(() => detail.value?.attachmentList || detail.value?.attachments || [])

async function openDetail(row) {
  drawer.value = true
  detail.value = await getRelic(row.id)
}

/** 重新生成当前藏品二维码（修改扫码域名或图片损坏时使用） */
async function regenerateQr() {
  if (!detail.value?.id) return
  try {
    detail.value.qrcode = await rebuildQrCode(detail.value.id)
    ElMessage.success('二维码已重新生成')
  } catch (err) {
    ElMessage.error(err?.message || '二维码生成失败')
  }
}

/* ---------------- 影像资料 ---------------- */
const uploading = ref(false)
/** 与后端 relic.upload.max-size 保持一致 */
const MAX_UPLOAD_MB = 20

/** 上传前校验类型与大小，避免无效请求 */
function beforeUpload(file) {
  if (!/\.(jpe?g|png|webp|gif|bmp|pdf)$/i.test(file.name)) {
    ElMessage.warning('仅支持 jpg / png / webp / gif / bmp / pdf 文件')
    return false
  }
  if (file.size > MAX_UPLOAD_MB * 1024 * 1024) {
    ElMessage.warning(`单个附件不能超过 ${MAX_UPLOAD_MB} MB`)
    return false
  }
  return true
}

async function handleUpload(file) {
  if (!detail.value?.id) return
  uploading.value = true
  try {
    await uploadRelicAttachment(detail.value.id, file, 1)
    ElMessage.success('影像已上传')
    await reloadDetail()
    load()
  } finally {
    uploading.value = false
  }
}

async function removeAttachment(file) {
  await ElMessageBox.confirm(`确定删除「${file.fileName}」吗？服务器上的文件会一并移除。`, '删除附件', {
    type: 'warning',
    confirmButtonText: '删除'
  })
  await deleteRelicAttachment(file.id)
  ElMessage.success('附件已删除')
  await reloadDetail()
  load()
}

/** 重新拉取当前藏品详情（上传 / 删除附件后刷新抽屉） */
async function reloadDetail() {
  if (detail.value?.id) detail.value = await getRelic(detail.value.id)
}

function formatFileSize(size) {
  if (!size) return '—'
  return size < 1024 * 1024
    ? `${Math.max(1, Math.round(size / 1024))} KB`
    : `${(size / 1024 / 1024).toFixed(1)} MB`
}

/* ---------------- 编辑 ---------------- */
const editVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({})

const rules = {
  relicName: [{ required: true, message: '请填写藏品名称', trigger: 'blur' }],
  fkCategoryId: [{ required: true, message: '请选择藏品分类', trigger: 'change' }]
}

function openEdit(row) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, row)
  editVisible.value = true
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await updateRelic(form.id, { ...form })
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function removeRow(row) {
  await ElMessageBox.confirm(
    `删除后藏品将进入逻辑删除状态（数据仍保留可恢复）。确定删除「${row.relicName}」吗？`,
    '删除藏品',
    { type: 'warning', confirmButtonText: '确认删除' }
  )
  await deleteRelic(row.id)
  ElMessage.success('已删除')
  load()
}

async function batchRemove() {
  const ids = selection.value.map((item) => item.id)
  await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 件藏品吗？借出中的藏品会被跳过。`, '批量删除', { type: 'warning' })
  const result = await batchDeleteRelics(ids)
  ElMessage.success(`已删除 ${result.deleted} 件藏品`)
  load()
}

function exportList() {
  exportCsv(
    `藏品档案-${new Date().toISOString().slice(0, 10)}`,
    ['藏品编号', '名称', '类别', '年代', '质地', '级别', '状态', '库房', '仓位', '入藏时间'],
    list.value.map((row) => [
      row.relicCode, row.relicName, row.categoryName, row.era, row.texture,
      RELIC_LEVEL[row.relicLevel]?.label || '', STOCK_STATUS[row.stockStatus]?.label || '',
      row.warehouseName, row.positionName, row.storageTime
    ])
  )
  ElMessage.success('已导出当前页数据')
}

onMounted(() => meta.ensure())
</script>

<style scoped>
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 var(--sp-5); }
.form-grid .full { grid-column: 1 / -1; }
.table-link { cursor: pointer; color: var(--text-primary); border-bottom: 1px solid transparent; }
.table-link:hover { border-bottom-color: var(--ink-900); color: var(--ink-900); }
.detail-head { display: grid; grid-template-columns: 140px 1fr; gap: var(--sp-4); }
.detail-figure { aspect-ratio: 4 / 5; border: 1px solid var(--border); position: relative; overflow: hidden; }
.drawer-qr { display: flex; align-items: center; gap: var(--sp-4); padding-top: var(--sp-4); border-top: 1px solid var(--border); }
.drawer-qr .qr-demo { margin: 0; width: 104px; height: 104px; }
.drawer-qr-img {
  flex: none;
  width: 104px;
  height: 104px;
  border: 6px solid #fff;
  outline: 1px solid var(--border-strong);
  image-rendering: pixelated;
}
.qr-actions { display: flex; gap: var(--sp-4); margin-top: 10px; font-size: var(--fs-micro); }
.qr-actions a { color: var(--accent); text-decoration: underline; text-underline-offset: 3px; }
.link-btn {
  border: 0;
  background: none;
  padding: 0;
  font-size: var(--fs-micro);
  color: var(--text-muted);
  text-decoration: underline;
  text-underline-offset: 3px;
  cursor: pointer;
}
.link-btn:hover { color: var(--accent); }
.upload-inline { display: inline-block; margin-left: var(--sp-3); vertical-align: baseline; }
.list-row-main { color: var(--text-primary); }
.list-row-main:hover { color: var(--accent); }
@media (max-width: 640px) {
  .form-grid { grid-template-columns: 1fr; }
}
</style>
