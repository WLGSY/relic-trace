package com.relictrace.service;

import com.relictrace.vo.QrCodeVO;

/**
 * 溯源二维码服务。
 *
 * <p>负责按藏品编号生成可扫描的二维码图片（PNG 落盘）并维护 relic_qr_code 记录，
 * 使“扫码溯源”从占位演示变为真实可用的链路。</p>
 *
 * @author 李丰
 */
public interface QrCodeService {

    /**
     * 查询藏品二维码；不存在或图片文件缺失时自动补建。
     *
     * @param relicId 藏品 id
     * @return 二维码信息
     */
    QrCodeVO getOrCreate(String relicId);

    /**
     * 强制重新生成某件藏品的二维码（内容或图片需要刷新时使用）。
     *
     * @param relicId 藏品 id
     * @return 二维码信息
     */
    QrCodeVO rebuild(String relicId);

    /**
     * 批量重建全部藏品的二维码，用于存量数据补齐或修改扫码域名后刷新。
     *
     * @return 生成（含刷新）的二维码数量
     */
    int rebuildAll();
}
