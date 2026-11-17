<template>
  <router-link class="relic-card" :to="{ name: 'relic-detail', params: { id: relic.id } }">
    <div class="thumb">
      <ArtFrame :image="relic.cover" :alt="relic.relicName" />
      <span class="tag ink level">{{ levelLabel }}</span>
    </div>
    <div class="body">
      <div class="name">{{ relic.relicName }}</div>
      <div class="meta">
        <span>{{ relic.era || '年代待考' }}</span>
        <span class="sep">/</span>
        <span>{{ relic.categoryName }}</span>
        <StatusTag :map="STOCK_STATUS" :value="relic.stockStatus" />
      </div>
      <div class="code">NO. {{ relic.relicCode }}</div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import ArtFrame from './ArtFrame.vue'
import StatusTag from './StatusTag.vue'
import { RELIC_LEVEL, STOCK_STATUS } from '@/utils/constants'

/** 藏品卡片：影像 + 名称 + 年代/类别/状态 + 藏品编号 */
const props = defineProps({
  relic: { type: Object, required: true }
})

const levelLabel = computed(() => RELIC_LEVEL[props.relic.relicLevel]?.label || '未定级')
</script>
