<template>
  <router-view v-slot="{ Component, route }">
    <transition name="fade" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup>
/**
 * 根组件
 * 只负责路由出口与页面过渡；布局、密度、导航分别由 FrontLayout / AdminLayout 承担。
 */
</script>
