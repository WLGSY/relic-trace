<template>
  <div>
    <PageHeader title="系统参数" index="13" subtitle="调整编号规则、二维码模板与业务阈值，修改即时生效并写入操作日志">
      <template #actions>
        <el-button @click="exportList">导出参数</el-button>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="参数名称 / 参数键"
        clearable
        style="width: 240px"
      />
      <div class="toolbar-actions">
        <span class="muted mono" style="font-size: 12px">{{ filtered.length }} 项参数 · 已修改 {{ dirtyCount }} 项</span>
      </div>
    </div>

    <div v-loading="loading" class="param-list">
      <div v-for="item in filtered" :key="item.id" class="param-row">
        <div class="param-info">
          <div class="param-name">
            {{ item.configName }}
            <span v-if="isDirty(item)" class="tag cinnabar">未保存</span>
          </div>
          <div class="param-key mono">{{ item.configKey }}</div>
          <p class="param-desc">{{ item.description || '暂无说明' }}</p>
        </div>

        <div class="param-editor">
          <!-- 布尔型参数：开关直接提交 -->
          <el-switch
            v-if="kindOf(item) === 'boolean'"
            :model-value="draftOf(item)"
            active-value="true"
            inactive-value="false"
            active-text="启用"
            inactive-text="停用"
            inline-prompt
            @update:model-value="(val) => setDraft(item, val)"
          />
          <!-- 数值型参数：数字输入 -->
          <el-input-number
            v-else-if="kindOf(item) === 'number'"
            :model-value="Number(draftOf(item))"
            :min="0"
            :max="9999"
            controls-position="right"
            style="width: 150px"
            @update:model-value="(val) => setDraft(item, String(val ?? 0))"
          />
          <!-- 文本型参数：普通输入 -->
          <el-input
            v-else
            :model-value="draftOf(item)"
            style="width: 100%"
            @update:model-value="(val) => setDraft(item, val)"
          />
          <el-button :disabled="!isDirty(item)" :loading="savingId === item.id" @click="save(item)">保存</el-button>
          <el-button link :disabled="!isDirty(item)" @click="revert(item)">撤销</el-button>
        </div>
      </div>
      <EmptyState v-if="!loading && !filtered.length" title="没有匹配的参数" msg="请调整关键词后重试" />
    </div>

    <div class="param-foot">
      <div class="panel-label">更新记录</div>
      <div v-for="log in recentLogs" :key="log.id" class="kv-row">
        <span class="kv-key mono">{{ formatDateTime(log.operateTime) }}</span>
        <span class="kv-value">{{ log.operateContent }}<span class="muted"> · {{ log.operateUserName }}</span></span>
      </div>
      <div v-if="!recentLogs.length" class="muted">暂无参数变更记录</div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import { listConfigs, listOperateLogs, updateConfig } from '@/api/system'
import { formatDateTime } from '@/utils/format'
import { exportCsv } from '@/utils/export'

/**
 * 系统参数
 * 对应文档 1.2.3 系统管理模块：参数查询与修改。
 * 参数类型由原始值推断——true/false 用开关、纯数字用数字框、其余用文本输入。
 */
const loading = ref(false)
const savingId = ref('')
const keyword = ref('')
const configs = ref([])
const drafts = reactive({})
const recentLogs = ref([])

const filtered = computed(() =>
  configs.value.filter((item) =>
    !keyword.value
      ? true
      : item.configName.includes(keyword.value) || item.configKey.toLowerCase().includes(keyword.value.toLowerCase())
  )
)

const dirtyCount = computed(() => configs.value.filter((item) => isDirty(item)).length)

const kindOf = (item) => {
  const value = String(item.configValue ?? '')
  if (value === 'true' || value === 'false') return 'boolean'
  if (/^\d+$/.test(value)) return 'number'
  return 'text'
}

const draftOf = (item) => drafts[item.id] ?? String(item.configValue ?? '')
const isDirty = (item) => String(draftOf(item)) !== String(item.configValue ?? '')

function setDraft(item, value) {
  drafts[item.id] = value
}

function revert(item) {
  drafts[item.id] = String(item.configValue ?? '')
}

async function load() {
  loading.value = true
  try {
    configs.value = (await listConfigs({})) || []
    configs.value.forEach((item) => { drafts[item.id] = String(item.configValue ?? '') })
    const logs = await listOperateLogs({ module: '系统参数', pageNum: 1, pageSize: 6 })
    recentLogs.value = logs?.list || []
  } catch {
    configs.value = []
  } finally {
    loading.value = false
  }
}

async function save(item) {
  const value = String(draftOf(item) ?? '').trim()
  if (!value) {
    ElMessage.warning('参数值不能为空')
    return
  }
  savingId.value = item.id
  try {
    await updateConfig(item.id, { configValue: value })
    item.configValue = value
    drafts[item.id] = value
    ElMessage.success(`「${item.configName}」已更新`)
    const logs = await listOperateLogs({ module: '系统参数', pageNum: 1, pageSize: 6 })
    recentLogs.value = logs?.list || []
  } finally {
    savingId.value = ''
  }
}

function exportList() {
  exportCsv(
    `系统参数-${new Date().toISOString().slice(0, 10)}`,
    ['参数键', '参数名称', '当前值', '说明'],
    filtered.value.map((row) => [row.configKey, row.configName, row.configValue, row.description || ''])
  )
  ElMessage.success('已导出参数清单')
}

onMounted(load)
</script>

<style scoped>
.param-list { border-top: 1px solid var(--border); }
.param-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 420px);
  gap: var(--sp-5);
  align-items: center;
  padding: var(--sp-4) 0;
  border-bottom: 1px solid var(--border);
}
.param-info { min-width: 0; }
.param-name { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; color: var(--text-primary); }
.param-key { margin-top: 3px; font-size: 11.5px; letter-spacing: 0.04em; color: var(--text-muted); }
.param-desc { margin-top: 6px; font-size: 12.5px; line-height: 1.7; color: var(--text-secondary); }
.param-editor { display: flex; align-items: center; gap: var(--sp-3); }
.param-foot { margin-top: var(--sp-6); padding-top: var(--sp-4); border-top: 1px solid var(--ink-900); }
.param-foot .kv-row { border-bottom: 1px solid var(--border); }
@media (max-width: 900px) {
  .param-row { grid-template-columns: 1fr; gap: var(--sp-3); }
}
</style>
