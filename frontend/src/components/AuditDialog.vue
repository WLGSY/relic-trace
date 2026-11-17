<template>
  <el-dialog
    :model-value="modelValue"
    title="业务审核"
    width="520px"
    append-to-body
    @update:model-value="(visible) => emit('update:modelValue', visible)"
  >
    <div v-if="item" class="stack">
      <div class="audit-meta">
        <div>
          <div class="panel-label">单据编号</div>
          <div class="mono" style="margin-top: 4px">{{ item.bizCode }}</div>
        </div>
        <div>
          <div class="panel-label">业务类型</div>
          <div style="margin-top: 4px">{{ AUDIT_BIZ_TYPE[item.bizType]?.label }}</div>
        </div>
        <div>
          <div class="panel-label">提交人</div>
          <div style="margin-top: 4px">{{ item.submitUser }}</div>
        </div>
      </div>

      <div>
        <div class="panel-label">审核对象</div>
        <div class="audit-title">{{ item.title }}</div>
        <div class="muted">{{ item.subtitle }}</div>
        <p v-if="item.detail" class="audit-detail">{{ item.detail }}</p>
      </div>

      <div>
        <div class="panel-label" style="margin-bottom: 6px">审核结论</div>
        <el-radio-group v-model="result">
          <el-radio :value="1">审核通过</el-radio>
          <el-radio :value="2">驳回</el-radio>
        </el-radio-group>
      </div>

      <div>
        <div class="panel-label" style="margin-bottom: 6px">
          审核意见<span v-if="result === 2" style="color: var(--danger)">（驳回必填）</span>
        </div>
        <el-input
          v-model="opinion"
          type="textarea"
          :rows="3"
          maxlength="200"
          show-word-limit
          :placeholder="result === 2 ? '请说明驳回原因，便于录入员修改后重新提交' : '可填写补充说明（选填）'"
        />
      </div>
    </div>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">提交审核</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { submitAudit } from '@/api/audit'
import { AUDIT_BIZ_TYPE } from '@/utils/constants'

/**
 * 审核弹窗
 * 对应 sys_audit_record：通过 / 驳回 + 审核意见（驳回时必填）
 */
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  item: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'done'])

const result = ref(1)
const opinion = ref('')
const submitting = ref(false)

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      result.value = 1
      opinion.value = ''
    }
  }
)

async function submit() {
  if (!props.item) return
  if (result.value === 2 && !opinion.value.trim()) {
    ElMessage.warning('驳回时必须填写审核意见')
    return
  }
  submitting.value = true
  try {
    await submitAudit({
      bizType: props.item.bizType,
      bizId: props.item.bizId,
      auditResult: result.value,
      auditOpinion: opinion.value.trim()
    })
    ElMessage.success(result.value === 1 ? '审核通过' : '已驳回')
    emit('update:modelValue', false)
    emit('done')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.audit-meta { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--sp-4); padding-bottom: var(--sp-4); border-bottom: 1px solid var(--border); }
.audit-title { margin-top: 6px; font-family: var(--font-display); font-size: 18px; color: var(--text-primary); }
.audit-detail { margin-top: var(--sp-2); font-size: var(--fs-caption); color: var(--text-secondary); line-height: 1.75; }
</style>
