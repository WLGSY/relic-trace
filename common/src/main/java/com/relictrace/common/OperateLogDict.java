package com.relictrace.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志中文化字典。
 *
 * <p>操作日志由 {@link OperateLogAspect} 统一记录。切面只能拿到控制器类名与方法名，
 * 若直接落库会出现 {@code Auth / logout}、{@code Relic / deleteAttachment} 这类英文痕迹，
 * 对使用者不可读。此类把「控制器名 → 模块名」「方法名 → 操作类型 + 操作描述」做统一映射，
 * 保证三个角色看到的工作台「最近操作」与日志管理页文案一致、可检索。</p>
 *
 * <p>新增接口时只需在 {@link #ACTIONS} 或 {@link #MODULE_ACTIONS} 中补一条映射；
 * 未登记的方法会退化为「操作」并保留模块名，不会出现英文。</p>
 *
 * @author 李丰
 */
public final class OperateLogDict {

    private OperateLogDict() {
    }

    /** 控制器 → 业务模块名 */
    private static final Map<String, String> MODULES = new HashMap<>();

    /** 方法名 → 操作（类型 + 描述） */
    private static final Map<String, Action> ACTIONS = new HashMap<>();

    /** 「模块:方法名」→ 操作，用于 create / update / delete 这类同名方法按模块细化 */
    private static final Map<String, Action> MODULE_ACTIONS = new HashMap<>();

    /** 操作类型与描述 */
    private record Action(String type, String detail) {
    }

    static {
        MODULES.put("Auth", "登录认证");
        MODULES.put("Relic", "藏品档案");
        MODULES.put("Collection", "藏品征集");
        MODULES.put("Audit", "业务审核");
        MODULES.put("Repair", "修复管理");
        MODULES.put("Loan", "展览借调");
        MODULES.put("Cancel", "注销管理");
        MODULES.put("Warehouse", "库房管理");
        MODULES.put("System", "系统管理");
        MODULES.put("Stats", "统计分析");

        ACTIONS.put("login", new Action("登录", "登录系统"));
        ACTIONS.put("logout", new Action("退出", "退出登录"));

        ACTIONS.put("create", new Action("新增", "新增单据"));
        ACTIONS.put("update", new Action("修改", "修改单据"));
        ACTIONS.put("delete", new Action("删除", "删除单据"));
        ACTIONS.put("batchDelete", new Action("删除", "批量删除单据"));
        ACTIONS.put("submit", new Action("提交", "提交审核"));

        ACTIONS.put("submitAudit", new Action("审核", "审核单据"));
        ACTIONS.put("batchAudit", new Action("审核", "批量审核单据"));

        ACTIONS.put("createRelic", new Action("新增", "编目建档"));
        ACTIONS.put("updateRelic", new Action("修改", "修改藏品信息"));
        ACTIONS.put("deleteRelic", new Action("删除", "删除藏品"));
        ACTIONS.put("batchDeleteRelics", new Action("删除", "批量删除藏品"));
        ACTIONS.put("rebuildQrCode", new Action("重建", "重建溯源二维码"));
        ACTIONS.put("rebuildAllQrCodes", new Action("重建", "批量重建溯源二维码"));
        ACTIONS.put("uploadAttachment", new Action("上传", "上传藏品影像"));
        ACTIONS.put("deleteAttachment", new Action("删除", "删除藏品影像"));

        ACTIONS.put("handover", new Action("处理", "办理借出手续"));
        ACTIONS.put("returnLoan", new Action("处理", "办理归还手续"));
        ACTIONS.put("process", new Action("处理", "登记修复结果"));
        ACTIONS.put("archive", new Action("处理", "注销归档"));

        ACTIONS.put("createWarehouse", new Action("新增", "新增库房"));
        ACTIONS.put("updateWarehouse", new Action("修改", "修改库房"));
        ACTIONS.put("deleteWarehouse", new Action("删除", "删除库房"));
        ACTIONS.put("createPosition", new Action("新增", "新增仓位"));
        ACTIONS.put("updatePosition", new Action("修改", "修改仓位"));
        ACTIONS.put("deletePosition", new Action("删除", "删除仓位"));
        ACTIONS.put("createStockRecord", new Action("新增", "登记出入库"));
        ACTIONS.put("updateStockRecord", new Action("修改", "修改出入库记录"));
        ACTIONS.put("deleteStockRecord", new Action("删除", "删除出入库记录"));
        ACTIONS.put("createStockCheck", new Action("新增", "发起库存盘点"));
        ACTIONS.put("finishStockCheck", new Action("处理", "完成库存盘点"));

        ACTIONS.put("createUser", new Action("新增", "新增用户"));
        ACTIONS.put("updateUser", new Action("修改", "修改用户"));
        ACTIONS.put("deleteUser", new Action("删除", "删除用户"));
        ACTIONS.put("resetPassword", new Action("修改", "重置用户密码"));
        ACTIONS.put("toggleStatus", new Action("修改", "变更用户状态"));
        ACTIONS.put("createDict", new Action("新增", "新增字典项"));
        ACTIONS.put("updateDict", new Action("修改", "修改字典项"));
        ACTIONS.put("deleteDict", new Action("删除", "删除字典项"));
        ACTIONS.put("updateConfig", new Action("修改", "修改系统参数"));

        // 同名方法按模块细化描述
        MODULE_ACTIONS.put("Collection:create", new Action("新增", "新增征集申请"));
        MODULE_ACTIONS.put("Collection:update", new Action("修改", "修改征集申请"));
        MODULE_ACTIONS.put("Collection:delete", new Action("删除", "删除征集申请"));
        MODULE_ACTIONS.put("Collection:batchDelete", new Action("删除", "批量删除征集申请"));
        MODULE_ACTIONS.put("Repair:create", new Action("新增", "新增修复申请"));
        MODULE_ACTIONS.put("Repair:delete", new Action("删除", "删除修复申请"));
        MODULE_ACTIONS.put("Loan:create", new Action("新增", "新增借调申请"));
        MODULE_ACTIONS.put("Cancel:create", new Action("新增", "新增注销申请"));
    }

    /**
     * 控制器类名 → 模块名。
     *
     * @param controllerSimpleName 控制器简单类名，如 {@code RelicController}
     * @return 中文模块名；未登记时退化为去掉 Controller 后缀的类名
     */
    public static String moduleOf(String controllerSimpleName) {
        if (controllerSimpleName == null || controllerSimpleName.isEmpty()) {
            return "系统";
        }
        String key = controllerSimpleName.replace("Controller", "");
        return MODULES.getOrDefault(key, key);
    }

    /**
     * 方法名 → 操作类型（新增 / 修改 / 删除 / 审核 / 提交 / 登录 / 上传 / 重建 / 处理）。
     *
     * @param controllerSimpleName 控制器简单类名，用于区分不同模块下的同名方法
     * @param method               控制器方法名
     * @return 操作类型
     */
    public static String typeOf(String controllerSimpleName, String method) {
        return resolve(controllerSimpleName, method).type();
    }

    /**
     * 方法名 → 操作描述（如「上传藏品影像」）。
     *
     * @param controllerSimpleName 控制器简单类名，用于区分不同模块下的同名方法
     * @param method               控制器方法名
     * @return 操作描述
     */
    public static String detailOf(String controllerSimpleName, String method) {
        return resolve(controllerSimpleName, method).detail();
    }

    private static Action resolve(String controllerSimpleName, String method) {
        if (method == null || method.isEmpty()) {
            return new Action("操作", "执行操作");
        }
        if (controllerSimpleName != null) {
            String key = controllerSimpleName.replace("Controller", "");
            Action scoped = MODULE_ACTIONS.get(key + ":" + method);
            if (scoped != null) {
                return scoped;
            }
        }
        Action action = ACTIONS.get(method);
        return action != null ? action : new Action("操作", "执行操作");
    }
}
