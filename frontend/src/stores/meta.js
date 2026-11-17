import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCategories } from '@/api/relic'
import { listDicts } from '@/api/system'
import { listWarehouses } from '@/api/warehouse'
import { DICT_FALLBACK } from '@/utils/constants'

/**
 * 基础数据仓库
 * ------------------------------------------------------------------
 * 藏品分类、数据字典、库房仓位属于"全局只读"数据，多处表单需要下拉选项，
 * 这里统一缓存一次，避免每个页面重复请求。
 */
export const useMetaStore = defineStore('meta', () => {
  const categories = ref([])
  const dicts = ref({})
  const warehouses = ref([])
  const loaded = ref(false)
  const loading = ref(false)

  const categoryOptions = computed(() =>
    categories.value.map((c) => ({ value: c.id, label: c.categoryName, code: c.categoryCode }))
  )

  const warehouseOptions = computed(() =>
    warehouses.value.filter((w) => w.isEnabled === 1).map((w) => ({ value: w.id, label: w.warehouseName }))
  )

  /** 字典选项；接口未返回时回退到内置默认值 */
  function dictOptions(type) {
    const list = dicts.value[type]?.length ? dicts.value[type] : DICT_FALLBACK[type] || []
    return list.map((d) => ({ value: d.dictCode, label: d.dictLabel }))
  }

  /** 按编码取字典名称 */
  function dictLabel(type, code) {
    if (code === null || code === undefined || code === '') return '—'
    const list = dicts.value[type]?.length ? dicts.value[type] : DICT_FALLBACK[type] || []
    const hit = list.find((d) => String(d.dictCode) === String(code))
    return hit?.dictLabel || String(code)
  }

  async function loadDicts() {
    const list = await listDicts({})
    const grouped = {}
    list.forEach((item) => {
      if (!grouped[item.dictType]) grouped[item.dictType] = []
      grouped[item.dictType].push(item)
    })
    Object.keys(grouped).forEach((key) => {
      grouped[key].sort((a, b) => a.sortNo - b.sortNo)
    })
    dicts.value = grouped
  }

  /** 首次进入后台时加载；失败不阻断页面渲染 */
  async function ensure(force = false) {
    if (loading.value) return
    if (loaded.value && !force) return
    loading.value = true
    try {
      const [catList, warehouseList] = await Promise.all([getCategories(), listWarehouses()])
      categories.value = catList || []
      warehouses.value = warehouseList || []
      await loadDicts()
      loaded.value = true
    } catch (error) {
      console.warn('[meta] 基础数据加载失败，已使用内置字典兜底', error)
    } finally {
      loading.value = false
    }
  }

  return {
    categories, dicts, warehouses, loaded, loading,
    categoryOptions, warehouseOptions,
    dictOptions, dictLabel, ensure, loadDicts
  }
})
