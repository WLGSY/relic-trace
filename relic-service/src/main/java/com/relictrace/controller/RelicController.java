package com.relictrace.controller;

import com.relictrace.common.PageResult;
import com.relictrace.common.Result;
import com.relictrace.po.RelicCategoryDO;
import com.relictrace.po.RelicInfoDO;
import com.relictrace.query.RelicQuery;
import com.relictrace.service.QrCodeService;
import com.relictrace.service.RelicService;
import com.relictrace.vo.AttachmentVO;
import com.relictrace.vo.QrCodeVO;
import com.relictrace.vo.RelicVO;
import com.relictrace.vo.TraceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 藏品信息控制层。
 *
 * <p>提供藏品档案的增删改查、溯源、扫码、公开检索等接口。</p>
 *
 * @author 李丰
 */
@RestController
@RequestMapping
public class RelicController {

    @Autowired
    private RelicService relicService;

    @Autowired
    private QrCodeService qrCodeService;

    /** 藏品分类列表（relic_category） */
    @GetMapping("/categories")
    public Result<List<RelicCategoryDO>> categories() {
        return Result.success(relicService.listCategories());
    }

    /** 后台：藏品档案分页查询 */
    @GetMapping("/relics")
    public Result<PageResult<RelicVO>> listRelics(RelicQuery query) {
        return Result.success(relicService.pageRelic(query));
    }

    /** 后台：藏品详情（含附件、二维码、溯源、征集来源） */
    @GetMapping("/relics/{id}")
    public Result<RelicVO> getRelic(@PathVariable String id) {
        return Result.success(relicService.getRelic(id));
    }

    /** 后台：编目建档 */
    @PostMapping("/relics")
    public Result<String> createRelic(@RequestBody RelicInfoDO relic) {
        return Result.success(relicService.createRelic(relic), "编目建档成功");
    }

    /** 后台：修改藏品档案 */
    @PutMapping("/relics/{id}")
    public Result<Void> updateRelic(@PathVariable String id, @RequestBody RelicInfoDO relic) {
        relicService.updateRelic(id, relic);
        return Result.success(null, "修改成功");
    }

    /** 后台：逻辑删除 */
    @DeleteMapping("/relics/{id}")
    public Result<Void> deleteRelic(@PathVariable String id) {
        relicService.deleteRelic(id);
        return Result.success(null, "删除成功");
    }

    /** 后台：批量逻辑删除 */
    @PostMapping("/relics/batch-delete")
    public Result<Void> batchDeleteRelics(@RequestBody Map<String, List<String>> body) {
        relicService.batchDeleteRelic(body.get("ids"));
        return Result.success(null, "批量删除成功");
    }

    /** 溯源时间线 */
    @GetMapping("/relics/{id}/trace")
    public Result<List<TraceVO>> getRelicTrace(@PathVariable String id) {
        return Result.success(relicService.getRelicTrace(id));
    }

    /** 扫码溯源：按藏品编号 / 二维码内容查询（公开） */
    @GetMapping("/relics/by-code/{code}")
    public Result<RelicVO> getRelicByCode(@PathVariable String code) {
        return Result.success(relicService.getRelicByCode(code));
    }

    /** 前台：公开藏品详情（公开，仅返回 is_public = 1 的藏品） */
    @GetMapping("/relics/public/{id}")
    public Result<RelicVO> publicDetail(@PathVariable String id) {
        return Result.success(relicService.getPublicRelic(id));
    }

    /** 前台：精选藏品（公开） */
    @GetMapping("/relics/public/highlights")
    public Result<List<RelicVO>> highlights() {
        return Result.success(relicService.listHighlights());
    }

    /** 前台：公开藏品检索（公开） */
    @GetMapping("/relics/public/search")
    public Result<PageResult<RelicVO>> searchPublic(RelicQuery query) {
        return Result.success(relicService.searchPublic(query));
    }

    /** 查询藏品溯源二维码（记录缺失时自动补建） */
    @GetMapping("/relics/{id}/qrcode")
    public Result<QrCodeVO> getQrCode(@PathVariable String id) {
        return Result.success(qrCodeService.getOrCreate(id));
    }

    /** 重新生成某件藏品的溯源二维码 */
    @PostMapping("/relics/{id}/qrcode/rebuild")
    public Result<QrCodeVO> rebuildQrCode(@PathVariable String id) {
        return Result.success(qrCodeService.rebuild(id), "二维码已重新生成");
    }

    /** 批量重建全部藏品二维码（存量补齐 / 修改扫码域名后刷新） */
    @PostMapping("/relics/qrcodes/rebuild")
    public Result<Integer> rebuildAllQrCodes() {
        int count = qrCodeService.rebuildAll();
        return Result.success(count, "已重建 " + count + " 件藏品的溯源二维码");
    }

    /** 后台：上传藏品附件（影像资料、修复照片、借调协议等） */
    @PostMapping("/relics/{id}/attachments")
    public Result<AttachmentVO> uploadAttachment(@PathVariable String id,
                                                @RequestParam("file") MultipartFile file,
                                                @RequestParam(value = "fileType", defaultValue = "1") Integer fileType) {
        return Result.success(relicService.uploadAttachment(id, file, fileType), "上传成功");
    }

    /** 后台：删除藏品附件（逻辑删除并清理磁盘文件） */
    @DeleteMapping("/relics/attachments/{attachmentId}")
    public Result<Void> deleteAttachment(@PathVariable String attachmentId) {
        relicService.deleteAttachment(attachmentId);
        return Result.success(null, "附件已删除");
    }
}
