<template>
  <div class="wrap">
    <div class="detail-nav">
      <router-link class="link-quiet" to="/search">← 返回检索</router-link>
      <span class="muted mono">COLLECTION / {{ relic?.relicCode || '—' }}</span>
    </div>

    <div v-if="loading" class="detail-loading">
      <div class="skeleton-figure" />
      <div class="stack" style="flex: 1">
        <div class="skeleton-line" style="width: 40%" />
        <div class="skeleton-line" style="width: 70%" />
        <div class="skeleton-line" style="width: 100%" />
      </div>
    </div>

    <EmptyState v-else-if="!relic" big="？" title="未找到该藏品" msg="藏品可能已下架或编号有误">
      <router-link class="btn-line" to="/search">返回检索</router-link>
    </EmptyState>

    <template v-else>
      <section class="detail-hero">
        <div class="detail-figure">
          <ArtFrame :image="relic.cover" :alt="relic.relicName" />
        </div>

        <div>
          <div class="eyebrow">馆藏档案 · {{ relic.categoryName }}</div>
          <h1 class="detail-title">{{ relic.relicName }}</h1>
          <div class="detail-tags">
            <StatusTag :map="STOCK_STATUS" :value="relic.stockStatus" />
            <span class="tag ink">{{ RELIC_LEVEL[relic.relicLevel]?.label || '未定级' }}</span>
            <span class="tag">{{ relic.era || '年代待考' }}</span>
            <span class="tag">{{ relic.texture || '—' }}</span>
          </div>
          <p class="detail-lead">{{ relic.relicDesc || '该藏品暂无详细描述。' }}</p>

          <div class="detail-facts">
            <div class="fact">
              <div class="panel-label">藏品编号</div>
              <div class="fact-value mono">{{ relic.relicCode }}</div>
            </div>
            <div class="fact">
              <div class="panel-label">入藏时间</div>
              <div class="fact-value">{{ relic.storageTime || relic.createTime || '—' }}</div>
            </div>
            <div class="fact">
              <div class="panel-label">存放位置</div>
              <div class="fact-value">
                {{ relic.warehouseName }}<span v-if="relic.positionName !== '—'"> · {{ relic.positionName }}</span>
              </div>
            </div>
            <div class="fact">
              <div class="panel-label">溯源记录</div>
              <div class="fact-value">{{ traces.length }} 条</div>
            </div>
          </div>

          <div class="detail-actions">
            <button class="btn-solid" type="button" @click="scrollToTrace">查看溯源时间线</button>
            <router-link class="btn-line" to="/scan">扫码核对</router-link>
          </div>
        </div>
      </section>

      <!-- 档案信息 + 影像 -->
      <section class="section grid-aside">
        <div>
          <SectionHead title="档案信息" sub="依据编目建档内容展示，保密字段不在前台公开" />
          <div class="card card-pad">
            <div class="kv-list">
              <div class="kv-row"><span class="kv-key">藏品编号</span><span class="kv-value mono">{{ relic.relicCode }}</span></div>
              <div class="kv-row"><span class="kv-key">藏品类别</span><span class="kv-value">{{ relic.categoryName }}</span></div>
              <div class="kv-row"><span class="kv-key">年代</span><span class="kv-value">{{ relic.era || '—' }}</span></div>
              <div class="kv-row"><span class="kv-key">质地</span><span class="kv-value">{{ relic.texture || '—' }}</span></div>
              <div class="kv-row"><span class="kv-key">规格</span><span class="kv-value">{{ relic.specification || '—' }}</span></div>
              <div class="kv-row"><span class="kv-key">数量</span><span class="kv-value">{{ relic.quantity || 1 }} 件</span></div>
              <div class="kv-row"><span class="kv-key">完残状况</span><span class="kv-value">{{ relic.integrityStatus || '—' }}</span></div>
              <div class="kv-row"><span class="kv-key">征集方式</span><span class="kv-value">{{ relic.collectionWay || '—' }}</span></div>
              <div class="kv-row full"><span class="kv-key">来源</span><span class="kv-value">{{ relic.collectionSource || '—' }}</span></div>
            </div>
          </div>
        </div>

        <div>
          <SectionHead title="影像资料" sub="藏品本体与细部影像" />
          <div class="card card-pad">
            <div class="attachment-list">
              <div v-for="file in attachments" :key="file.id" class="attachment">
                <img
                  v-if="file.fileType === 1"
                  class="file-thumb"
                  :src="uploadUrl(file.filePath)"
                  :alt="file.fileName"
                  loading="lazy"
                />
                <span v-else class="file-mark">{{ FILE_TYPE[file.fileType]?.label?.charAt(0) || '影' }}</span>
                <div style="min-width: 0">
                  <div class="file-name">{{ file.fileName }}</div>
                  <div class="muted mono">{{ formatFileSize(file.fileSize) }} · {{ formatDate(file.uploadTime) }}</div>
                </div>
              </div>
              <div v-if="!attachments.length" class="muted">暂无影像资料</div>
            </div>

            <div class="qr-block">
              <img
                v-if="relic.qrcode?.imageUrl"
                class="qr-img"
                :src="uploadUrl(relic.qrcode.imageUrl)"
                :alt="`${relic.relicName} 溯源二维码`"
              />
              <div v-else class="qr-demo" aria-hidden="true" />
              <div class="panel-label">溯源二维码</div>
              <div class="mono qr-content">{{ relic.qrcode?.content || relic.qrcode?.qrContent || relic.qrcodeContent }}</div>
              <div class="muted">累计扫码 {{ relic.qrcode?.scanCount || 0 }} 次</div>
              <a
                v-if="relic.qrcode?.imageUrl"
                class="qr-download"
                :href="uploadUrl(relic.qrcode.imageUrl)"
                :download="`${relic.relicCode || relic.id}-二维码.png`"
              >下载二维码</a>
            </div>
          </div>
        </div>
      </section>

      <!-- 溯源时间线 -->
      <section id="trace" class="section">
        <SectionHead title="溯源时间线" sub="征集、入藏、出入库、修复、借调与注销的完整经历" />
        <div class="card card-pad">
          <TraceTimeline :traces="traces" hide-relic />
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import ArtFrame from '@/components/ArtFrame.vue'
import PageHeader from '@/components/PageHeader.vue'
import SectionHead from '@/components/SectionHead.vue'
import StatusTag from '@/components/StatusTag.vue'
import TraceTimeline from '@/components/TraceTimeline.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getPublicRelic } from '@/api/relic'
import { FILE_TYPE, RELIC_LEVEL, STOCK_STATUS } from '@/utils/constants'
import { formatDate } from '@/utils/format'
import { uploadUrl } from '@/utils/asset'

/**
 * 藏品详情与溯源（前台）
 * 覆盖 2.3.4 用例 016：档案信息 + 溯源时间线 + 影像资料。
 * 保密度为 0 的藏品不会出现在前台列表，但详情接口仍做兜底判断。
 */
const route = useRoute()
const relic = ref(null)
const loading = ref(true)

/** 时间线按时间倒序（最新在前）；后端字段为 traceList，兼容 mock 的 traces */
const traces = computed(() => [...(relic.value?.traceList || relic.value?.traces || [])].reverse())

/** 影像资料；后端字段为 attachmentList，兼容 mock 的 attachments */
const attachments = computed(() => relic.value?.attachmentList || relic.value?.attachments || [])

function formatFileSize(size) {
  if (!size) return '—'
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function scrollToTrace() {
  document.getElementById('trace')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(async () => {
  try {
    relic.value = await getPublicRelic(route.params.id)
  } catch {
    relic.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-5) 0 var(--sp-4);
  border-bottom: 1px solid var(--border);
  margin-bottom: var(--sp-6);
}
.detail-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: var(--sp-4); }
.detail-facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--sp-4);
  margin-top: var(--sp-5);
  padding-top: var(--sp-4);
  border-top: 1px solid var(--border);
}
.fact-value { margin-top: 4px; font-size: var(--fs-caption); color: var(--text-primary); }
.detail-actions { display: flex; gap: var(--sp-2); margin-top: var(--sp-6); flex-wrap: wrap; }
.attachment-list { display: flex; flex-direction: column; gap: var(--sp-3); }
.attachment { display: flex; align-items: center; gap: var(--sp-3); padding-bottom: var(--sp-3); border-bottom: 1px solid var(--border); }
.file-thumb { width: 46px; height: 46px; object-fit: cover; border: 1px solid var(--border); flex: none; }
.attachment:last-child { border-bottom: 0; padding-bottom: 0; }
.file-mark {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  flex: none;
  border: 1px solid var(--border-strong);
  font-family: var(--font-display);
  font-size: 14px;
  color: var(--text-secondary);
}
.file-name { font-size: var(--fs-caption); color: var(--text-primary); margin-bottom: 2px; }
.qr-block { margin-top: var(--sp-5); padding-top: var(--sp-4); border-top: 1px solid var(--border); text-align: center; }
.qr-img {
  display: block;
  width: 132px;
  height: 132px;
  margin: 0 auto var(--sp-4);
  border: 8px solid #fff;
  outline: 1px solid var(--border-strong);
  image-rendering: pixelated;
}
.qr-download {
  display: inline-block;
  margin-top: 8px;
  font-size: var(--fs-micro);
  color: var(--accent);
  text-decoration: underline;
  text-underline-offset: 3px;
}
.qr-content { font-size: var(--fs-micro); color: var(--text-secondary); margin: 8px 0 4px; word-break: break-all; }
.detail-loading { display: flex; gap: var(--sp-6); padding-bottom: var(--sp-6); }
.skeleton-figure { width: 48%; aspect-ratio: 4 / 3; background: var(--paper-1); border: 1px solid var(--border); }
.skeleton-line { height: 14px; background: var(--paper-1); margin-bottom: 12px; }
@media (max-width: 900px) {
  .detail-loading { flex-direction: column; }
  .skeleton-figure { width: 100%; }
  .detail-facts { grid-template-columns: 1fr; }
}
</style>
