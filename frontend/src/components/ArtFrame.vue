<template>
  <div class="art" :class="isPhoto ? 'art-photo' : `art-${artSeed}`">
    <img v-if="isPhoto" class="art-img" :src="uploadUrl(image)" :alt="alt" loading="lazy" />
    <template v-else>
      <span class="art-veil" />
      <span class="art-noise" />
    </template>
    <slot />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { uploadUrl } from '@/utils/asset'

/**
 * 藏品图像
 * ------------------------------------------------------------------
 * image 是影像路径（/uploads/relics/xx.jpg）时渲染真实器物照片；
 * 是 a~f 这类纹样种子（mock 数据）或为空时，回退到低饱和矿物色抽象器物线稿，
 * 保证离线环境下列表与详情页都不会出现破图或空白。
 */
const props = defineProps({
  /** 影像地址，如 /uploads/relics/TC-0001.jpg */
  image: { type: String, default: '' },
  /** 无影像时的抽象纹样种子（a ~ f） */
  seed: { type: String, default: 'a' },
  /** 无障碍替代文本 */
  alt: { type: String, default: '' }
})

/** 含 / 或 . 的视为真实图片路径，否则当作纹样种子 */
const isPhoto = computed(() => !!props.image && /[/.]/.test(props.image))
const artSeed = computed(() => (isPhoto.value ? props.seed : props.image || props.seed))
</script>
