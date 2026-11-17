import { reactive, ref } from 'vue'

/**
 * 列表页通用逻辑：分页、查询、重置、加载态
 * ------------------------------------------------------------------
 * @param {(params:object)=>Promise<{list:Array,total:number}>} fetcher 接口函数
 * @param {{pageSize?:number, defaultQuery?:object, immediate?:boolean}} options
 */
export function useTable(fetcher, options = {}) {
  const { pageSize = 10, defaultQuery = {}, immediate = true } = options

  const loading = ref(false)
  const list = ref([])
  const total = ref(0)
  const query = reactive({ pageNum: 1, pageSize, ...defaultQuery })

  const snapshot = JSON.parse(JSON.stringify(defaultQuery))

  async function load() {
    loading.value = true
    try {
      const result = await fetcher({ ...query })
      list.value = result?.list || []
      total.value = result?.total || 0
    } catch (error) {
      // 错误提示已由 axios 拦截器统一处理，这里仅保证页面状态可控
      list.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  /** 条件查询：回到第一页 */
  function search() {
    query.pageNum = 1
    return load()
  }

  /** 重置查询条件 */
  function reset() {
    Object.keys(snapshot).forEach((key) => {
      query[key] = Array.isArray(snapshot[key]) ? [...snapshot[key]] : snapshot[key]
    })
    query.pageNum = 1
    return load()
  }

  function onPageChange(page) {
    query.pageNum = page
    return load()
  }

  function onSizeChange(size) {
    query.pageSize = size
    query.pageNum = 1
    return load()
  }

  if (immediate) load()

  return { loading, list, total, query, load, search, reset, onPageChange, onSizeChange }
}
