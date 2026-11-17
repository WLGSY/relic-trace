<template>
  <div>
    <PageHeader title="用户管理" index="11" subtitle="维护系统操作人员账号，按角色分配藏品业务与审核权限">
      <template #actions>
        <el-button @click="exportList">导出账号清单</el-button>
        <el-button type="primary" @click="openCreate">新增用户</el-button>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="登录账号 / 姓名 / 手机号"
        clearable
        style="width: 236px"
        @keyup.enter="search"
        @clear="search"
      />
      <el-select v-model="query.roleType" placeholder="全部角色" clearable style="width: 128px" @change="search">
        <el-option v-for="(item, key) in ROLE_TYPE" :key="key" :label="item.label" :value="Number(key)" />
      </el-select>
      <el-select v-model="query.isEnabled" placeholder="全部状态" clearable style="width: 118px" @change="search">
        <el-option label="已启用" :value="1" />
        <el-option label="已停用" :value="0" />
      </el-select>
      <el-button @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" row-key="id">
      <el-table-column label="登录账号" width="150">
        <template #default="{ row }">
          <span class="mono" style="font-size: 12.5px">{{ row.loginName }}</span>
          <span v-if="row.id === userStore.user?.id" class="tag jade" style="margin-left: 6px">当前</span>
        </template>
      </el-table-column>
      <el-table-column label="姓名" width="120">
        <template #default="{ row }">
          <span class="user-cell">
            <span class="user-mark">{{ initials(row.realName) }}</span>
            {{ row.realName }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <span class="tag" :class="roleTone(row.roleType)">{{ ROLE_TYPE[row.roleType]?.label || '未知' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="联系电话" width="140">
        <template #default="{ row }"><span class="mono" style="font-size: 12.5px">{{ row.phone || '—' }}</span></template>
      </el-table-column>
      <el-table-column label="权限范围" min-width="230">
        <template #default="{ row }"><span class="muted">{{ scopeOf(row.roleType) }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="96">
        <template #default="{ row }">
          <StatusTag :map="ENABLED_STATUS" :value="row.isEnabled" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="150">
        <template #default="{ row }"><span class="mono" style="font-size: 12px">{{ formatDateTime(row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="right" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="primary" :disabled="row.id === userStore.user?.id" @click="openReset(row)">重置密码</el-button>
          <el-button link :type="row.isEnabled === 1 ? 'warning' : 'primary'" :disabled="row.id === userStore.user?.id" @click="toggle(row)">
            {{ row.isEnabled === 1 ? '停用' : '启用' }}
          </el-button>
          <el-button link type="danger" :disabled="row.id === userStore.user?.id" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.pageNum"
      @current-change="onPageChange"
    />

    <!-- 新增 / 编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="620px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="登录账号" prop="loginName">
            <el-input v-model="form.loginName" :disabled="!!form.id" placeholder="4~20 位字母或数字" />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item>
          <el-form-item label="角色类型" prop="roleType">
            <el-select v-model="form.roleType" style="width: 100%">
              <el-option v-for="(item, key) in ROLE_TYPE" :key="key" :label="item.label" :value="Number(key)" />
            </el-select>
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" maxlength="11" placeholder="11 位手机号" />
          </el-form-item>
          <el-form-item v-if="!form.id" label="初始密码" prop="loginPassword">
            <el-input v-model="form.loginPassword" type="password" show-password placeholder="默认 123456" />
          </el-form-item>
          <el-form-item label="账号状态">
            <el-radio-group v-model="form.isEnabled">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注" class="full">
            <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="120" show-word-limit />
          </el-form-item>
        </div>
      </el-form>
      <p class="hint-line">角色决定可见菜单与可执行操作：管理员全量、录入员负责征集与编目、审核员负责审核与注销审批。</p>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码 -->
    <el-dialog v-model="resetVisible" title="重置登录密码" width="420px" append-to-body destroy-on-close>
      <p class="muted" style="margin-bottom: var(--sp-4)">
        即将为 <b>{{ resetTarget?.realName }}</b>（{{ resetTarget?.loginName }}）重置密码，重置后请及时通知本人修改。
      </p>
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-position="top">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetForm.password" type="password" show-password placeholder="6~20 位字符" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReset">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import {
  createUser,
  deleteUser,
  listUsers,
  resetUserPassword,
  toggleUserStatus,
  updateUser
} from '@/api/system'
import { useTable } from '@/composables/useTable'
import { useUserStore } from '@/stores/user'
import { ROLE_TYPE } from '@/utils/constants'
import { formatDateTime, initials } from '@/utils/format'
import { exportCsv } from '@/utils/export'

/**
 * 用户管理
 * 对应文档 1.2.3 系统管理模块：
 * 新增用户、修改用户、重置密码、启用/停用、逻辑删除，并按角色控制菜单与操作权限。
 */
const userStore = useUserStore()

/** 账号启用状态（后端用 isEnabled 0/1 表示，与藏品状态枚举区分开） */
const ENABLED_STATUS = {
  1: { label: '已启用', tone: 'jade' },
  0: { label: '已停用', tone: 'ink' }
}

const { loading, list, total, query, load, search, reset, onPageChange } = useTable(listUsers, {
  defaultQuery: { keyword: '', roleType: '', isEnabled: '' }
})

/** 角色对应的权限范围文案，用于列表直观展示授权边界 */
const ROLE_SCOPE = {
  1: '全部菜单 · 可管理用户、字典、参数与日志',
  2: '征集 · 编目 · 出入库 · 修复 · 借调 · 注销申请',
  3: '审核工作台 · 注销审批 · 统计分析'
}
const scopeOf = (roleType) => ROLE_SCOPE[roleType] || '—'
const roleTone = (roleType) => ({ 1: 'cinnabar', 2: 'jade', 3: 'bronze' }[roleType] || 'ink')

/* ---------------- 新增 / 编辑 ---------------- */
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({})

const rules = {
  loginName: [
    { required: true, message: '请填写登录账号', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]{4,20}$/, message: '4~20 位字母、数字或下划线', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请填写真实姓名', trigger: 'blur' }],
  roleType: [{ required: true, message: '请选择角色类型', trigger: 'change' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请填写 11 位手机号', trigger: 'blur' }],
  loginPassword: [{ min: 6, max: 20, message: '密码长度为 6~20 位', trigger: 'blur' }]
}

function openCreate() {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, { loginName: '', realName: '', roleType: 2, phone: '', loginPassword: '123456', isEnabled: 1, remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.id) {
      await updateUser(form.id, {
        realName: form.realName,
        roleType: form.roleType,
        phone: form.phone,
        isEnabled: form.isEnabled,
        remark: form.remark
      })
      ElMessage.success('用户信息已更新')
    } else {
      await createUser({ ...form })
      ElMessage.success('用户创建成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

/* ---------------- 启用 / 停用 ---------------- */
async function toggle(row) {
  const next = row.isEnabled === 1 ? 0 : 1
  await ElMessageBox.confirm(
    next === 0
      ? `停用后「${row.realName}」将无法登录系统，已产生的业务数据不受影响。确定停用吗？`
      : `确定启用「${row.realName}」的登录账号吗？`,
    next === 0 ? '停用账号' : '启用账号',
    { type: 'warning' }
  )
  await toggleUserStatus(row.id, next)
  ElMessage.success(next === 0 ? '账号已停用' : '账号已启用')
  load()
}

/* ---------------- 重置密码 ---------------- */
const resetVisible = ref(false)
const resetTarget = ref(null)
const resetFormRef = ref(null)
const resetForm = reactive({ password: '' })
const resetRules = {
  password: [
    { required: true, message: '请填写新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6~20 位', trigger: 'blur' }
  ]
}

function openReset(row) {
  resetTarget.value = row
  resetForm.password = ''
  resetVisible.value = true
}

async function submitReset() {
  const valid = await resetFormRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await resetUserPassword(resetTarget.value.id, resetForm.password)
    ElMessage.success('密码已重置')
    resetVisible.value = false
  } finally {
    saving.value = false
  }
}

/* ---------------- 删除 ---------------- */
async function removeRow(row) {
  await ElMessageBox.confirm(
    `删除后该账号将进入逻辑删除状态（历史操作日志保留）。确定删除「${row.realName}」吗？`,
    '删除用户',
    { type: 'warning', confirmButtonText: '确认删除' }
  )
  await deleteUser(row.id)
  ElMessage.success('已删除')
  load()
}

/* ---------------- 导出 ---------------- */
function exportList() {
  exportCsv(
    `用户清单-${new Date().toISOString().slice(0, 10)}`,
    ['登录账号', '姓名', '角色', '联系电话', '状态', '创建时间', '备注'],
    list.value.map((row) => [
      row.loginName,
      row.realName,
      ROLE_TYPE[row.roleType]?.label || '',
      row.phone || '',
      ENABLED_STATUS[row.isEnabled]?.label || '',
      formatDateTime(row.createTime),
      row.remark || ''
    ])
  )
  ElMessage.success('已导出当前页数据')
}
</script>

<style scoped>
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 var(--sp-5); }
.form-grid .full { grid-column: 1 / -1; }
.user-cell { display: inline-flex; align-items: center; gap: 8px; }
.user-mark {
  display: inline-grid;
  place-items: center;
  width: 22px;
  height: 22px;
  border: 1px solid var(--ink-900);
  font-size: 10px;
  letter-spacing: 0.02em;
  color: var(--ink-900);
}
.hint-line {
  margin-top: calc(-1 * var(--sp-2));
  padding-top: var(--sp-3);
  border-top: 1px solid var(--border);
  font-size: 12.5px;
  color: var(--text-muted);
  line-height: 1.7;
}
@media (max-width: 640px) {
  .form-grid { grid-template-columns: 1fr; }
}
</style>
