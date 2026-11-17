<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="780px"
    append-to-body
    @update:model-value="(visible) => emit('update:modelValue', visible)"
  >
    <div class="toolbar" style="margin-top: 0">
      <el-input
        v-model="keyword"
        placeholder="按藏品名称 / 藏品编号检索"
        clearable
        style="width: 260px"
        @keyup.enter="load"
        @clear="load"
      />
      <el-button @click="load">查询</el-button>
      <span class="toolbar-meta">双击行可快速选择</span>
    </div>

    <el-table v-loading="loading" :data="list" height="360" @row-dblclick="choose">
      <el-table-column prop="relicCode" label="藏品编号" width="130">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ row.relicCode }}</span></template>
      </el-table-column>
      <el-table-column prop="relicName" label="藏品名称" min-width="160" />
      <el-table-column prop="era" label="年代" width="90" />
      <el-table-column prop="categoryName" label="类别" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusTag :map="STOCK_STATUS" :value="row.stockStatus" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="choose(row)">选择</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      layout="prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.pageNum"
      @current-change="onPageChange"
    />
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { listRelics } from '@/api/relic'
import { STOCK_STATUS } from '@/utils/constants'
import StatusTag from './StatusTag.vue'

/**
 * 藏品选择器
 * 出入库、修复、借调、注销等业务都需要先定位一件藏品，
 * 统一抽成弹窗，避免每个页面重复实现检索逻辑。
 */
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '选择藏品' },
  query: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['update:modelValue', 'select'])

const keyword = ref('')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref({ pageNum: 1, pageSize: 8 })

async function load() {
  loading.value = true
  try {
    const result = await listRelics({ ...props.query, keyword: keyword.value, pageNum: page.value.pageNum, pageSize: page.value.pageSize })
    list.value = result.list
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function onPageChange(current) {
  page.value.pageNum = current
  load()
}

function choose(row) {
  emit('select', row)
  emit('update:modelValue', false)
}

/** 打开时重置并加载 */
function reset() {
  keyword.value = ''
  page.value.pageNum = 1
  load()
}

defineExpose({ reset })
</script>
