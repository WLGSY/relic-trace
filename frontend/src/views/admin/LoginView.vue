<template>
  <div class="login-screen">
    <!-- 左：视觉区 -->
    <section class="login-visual">
      <div class="art art-e">
        <span class="art-veil" />
        <span class="art-noise" />
      </div>
      <div class="v-top">
        <AppBrand title="文物藏品管理与溯源系统" subtitle="Relic Archive & Provenance" />
      </div>
      <div class="v-quote">
        <h2>一物一档，<br />一档一迹。</h2>
        <p>
          从征集入藏到展览借调，每一次交接都被完整记录。
          系统以藏品编号为主键，构建可回溯的全生命周期档案。
        </p>
      </div>
      <div class="v-bottom">
        <span>RELICS / 2026</span>
        <span>HUNAN · CHINA</span>
      </div>
    </section>

    <!-- 右：登录表单 -->
    <section class="login-panel">
      <div class="login-card">
        <h1>登录管理端</h1>
        <p class="sub">账号由系统管理员统一创建，系统不提供自助注册</p>

        <div class="form-area">
          <div class="panel-label" style="margin-bottom: 10px">演示身份 / Demo role</div>
          <div class="role-picker">
            <button
              v-for="account in accounts"
              :key="account.role"
              type="button"
              class="role-opt"
              :class="{ selected: selected === account.role }"
              @click="pick(account)"
            >
              <span class="r-name">{{ account.name }}</span>
              <span class="r-code">{{ account.code }}</span>
            </button>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
            <el-form-item label="登录账号" prop="loginName">
              <el-input v-model="form.loginName" placeholder="请输入登录账号" autocomplete="username" />
            </el-form-item>
            <el-form-item label="登录密码" prop="loginPassword">
              <el-input
                v-model="form.loginPassword"
                type="password"
                show-password
                placeholder="请输入登录密码"
                autocomplete="current-password"
                @keyup.enter="submit"
              />
            </el-form-item>
            <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">
              登 录
            </el-button>
          </el-form>

          <p class="demo-hint">
            演示密码统一为 <b class="mono">123456</b>，选择上方身份即可自动填充。
          </p>
          <div class="link-center">
            <router-link to="/">← 返回前台首页</router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppBrand from '@/components/AppBrand.vue'
import { useUserStore } from '@/stores/user'

/**
 * 登录页
 * 对应 1.2.1：角色决定登录后的菜单与数据范围；
 * 演示环境提供三类身份快速切换，真实环境由后端 JWT 携带 role_type。
 */
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const accounts = [
  { role: 1, name: '系统管理员', code: 'ROLE_ADMIN', loginName: 'admin' },
  { role: 2, name: '录入员', code: 'ROLE_OPERATOR', loginName: 'zhangly' },
  { role: 3, name: '审核员', code: 'ROLE_AUDITOR', loginName: 'lishen' }
]

const formRef = ref(null)
const loading = ref(false)
const selected = ref(1)
const form = reactive({ loginName: 'admin', loginPassword: '123456' })

const rules = {
  loginName: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  loginPassword: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于 6 位', trigger: 'blur' }
  ]
}

function pick(account) {
  selected.value = account.role
  form.loginName = account.loginName
  form.loginPassword = '123456'
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const payload = await userStore.login({ ...form })
    ElMessage.success(`欢迎回来，${payload.user.realName}`)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin/workbench'
    router.replace(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.demo-hint {
  margin-top: var(--sp-5);
  padding-top: var(--sp-4);
  border-top: 1px solid var(--border);
  font-size: var(--fs-micro);
  color: var(--text-muted);
  letter-spacing: 0.02em;
}
.demo-hint b { color: var(--text-secondary); }
</style>
