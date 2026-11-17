package com.relictrace.common;

/**
 * 业务单号生成工具。
 *
 * <p>单号格式统一为 {@code 前缀-yyyy-NNN}，例如 {@code ZJ-2026-001}（征集）、
 * {@code CK-2026-003}（出库）。流水号按「当年 + 同前缀」的最大值加一，
 * 各服务只需把 Mapper 查到的最大编号传入即可。</p>
 *
 * @author 李丰
 */
public final class BizCodeUtils {

    private BizCodeUtils() {
    }

    /**
     * 生成下一个业务单号（流水号补足 3 位）。
     *
     * @param maxCode 同前缀下已有的最大单号，可为 null
     * @param prefix  单号前缀，需包含年份与连字符，如 {@code ZJ-2026-}
     * @return 下一个单号，如 {@code ZJ-2026-002}
     */
    public static String nextCode(String maxCode, String prefix) {
        return nextCode(maxCode, prefix, 3);
    }

    /**
     * 生成下一个业务单号。
     *
     * @param maxCode 同前缀下已有的最大单号，可为 null
     * @param prefix  单号前缀，如 {@code RW-} 或 {@code ZJ-2026-}
     * @param digits  流水号位数，藏品编号为 4 位，业务单据为 3 位
     * @return 下一个单号，如 {@code RW-0007}
     */
    public static String nextCode(String maxCode, String prefix, int digits) {
        if (maxCode == null || maxCode.length() <= prefix.length()) {
            return prefix + String.format("%0" + digits + "d", 1);
        }
        String seqText = maxCode.substring(prefix.length());
        try {
            int seq = Integer.parseInt(seqText) + 1;
            return prefix + String.format("%0" + digits + "d", seq);
        } catch (NumberFormatException e) {
            // 历史数据不符合规范时退化为时间戳后缀，保证唯一且不阻断业务
            return prefix + System.currentTimeMillis() % 100000;
        }
    }
}
