<template>
  <div class="wrap">
    <PageHeader title="藏品检索" index="01" subtitle="按关键词、分类、年代与级别组合检索公开藏品档案">
      <template #actions>
        <router-link class="link-quiet" to="/scan">扫码直达 →</router-link>
      </template>
    </PageHeader>

    <!-- 检索条件 -->
    <div class="search-bar">
      <el-input
        v-model="query.keyword"
        placeholder="输入藏品名称或藏品编号"
        clearable
        size="large"
        class="search-input"
        @keyup.enter="search"
        @clear="search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>

      <el-select v-model="query.categoryId" placeholder="全部类别" clearable size="large" style="width: 132px" @change="search">
        <el-option v-for="item in meta.categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.era" placeholder="全部年代" clearable size="large" style="width: 132px" @change="search">
        <el-option v-for="item in eraOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="query.relicLevel" placeholder="全部级别" clearable size="large" style="width: 118px" @change="search">
        <el-option v-for="(item, key) in RELIC_LEVEL" :key="key" :label="item.label" :value="Number(key)" />
      </el-select>
      <el-select v-model="query.sort" size="large" style="width: 148px" @change="search">
        <el-option v-for="item in sorts" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button size="large" type="primary" @click="search">检索</el-button>
    </div>

    <!-- 结果统计 -->
    <div class="result-bar">
      <span class="panel-label">检索结果</span>
      <span class="result-count">共 <b>{{ total }}</b> 件藏品</span>
      <button v-if="hasFilter" class="clear-btn" type="button" @click="reset">清除全部条件</button>
    </div>

    <div v-loading="loading" class="result-area">
      <div v-if="list.length" class="relic-grid">
        <RelicCard v-for="relic in list" :key="relic.id" :relic="relic" />
      </div>
      <EmptyState
        v-else-if="!loading"
        big="空"
        title="未找到相关藏品"
        msg="请尝试更换关键词，或减少筛选条件"
      >
        <button class="btn-line" type="button" @click="reset">重置检索条件</button>
      </EmptyState>
    </div>

    <el-pagination
      v-if="total"
      layout="prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.pageNum"
      @current-change="onPageChange"
    />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import RelicCard from '@/components/RelicCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import { searchPublicRelics } from '@/api/relic'
import { useMetaStore } from '@/stores/meta'
import { useTable } from '@/composables/useTable'
import { RELIC_LEVEL } from '@/utils/constants'

/**
 * 藏品检索（前台）
 * 覆盖 2.3.4 用例 015：关键词检索、按分类/年代/级别筛选、排序与翻页。
 */
const route = useRoute()
const meta = useMetaStore()

const sorts = [
  { value: '', label: '最新入藏' },
  { value: 'level', label: '级别优先' },
  { value: 'oldest', label: '最早入藏' },
  { value: 'views', label: '最多扫码' }
]

const { loading, list, total, query, search, reset, onPageChange } = useTable(searchPublicRelics, {
  // 一页 6 件：3 列 × 2 行，与首页精选的栅格节奏一致
  pageSize: 6,
  defaultQuery: {
    keyword: typeof route.query.keyword === 'string' ? route.query.keyword : '',
    categoryId: '',
    era: '',
    relicLevel: '',
    sort: ''
  }
})

const eraOptions = computed(() => meta.dictOptions('era'))
const hasFilter = computed(() =>
  Boolean(query.keyword || query.categoryId || query.era || query.relicLevel !== '' || query.sort)
)

onMounted(() => meta.ensure())
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: var(--sp-2);
  flex-wrap: wrap;
  align-items: center;
  padding-bottom: var(--sp-5);
  border-bottom: 1px solid var(--border);
}
.search-input { width: 300px; }
.search-input :deep(.el-input__wrapper) { box-shadow: 0 0 0 1px var(--border-strong) inset; }
.result-bar {
  display: flex;
  align-items: baseline;
  gap: var(--sp-4);
  padding: var(--sp-4) 0 var(--sp-5);
}
.result-count { font-size: var(--fs-caption); color: var(--text-secondary); }
.result-count b { font-family: var(--font-display); font-size: 20px; color: var(--text-primary); margin: 0 2px; }
.clear-btn {
  border: 0;
  background: none;
  padding: 0;
  font-size: var(--fs-micro);
  color: var(--text-muted);
  cursor: pointer;
  border-bottom: 1px solid var(--line-strong);
}
.clear-btn:hover { color: var(--text-primary); border-bottom-color: var(--ink-900); }
.result-area { min-height: 320px; }
@media (max-width: 760px) {
  .search-input { width: 100%; }
  .search-bar :deep(.el-select) { flex: 1; width: auto !important; }
}
</style>
