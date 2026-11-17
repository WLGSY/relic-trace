package com.relictrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.relictrace.common.BizCodeUtils;
import com.relictrace.common.BusinessException;
import com.relictrace.common.LoginUser;
import com.relictrace.common.PageResult;
import com.relictrace.mapper.RelicAttachmentMapper;
import com.relictrace.mapper.RelicCategoryMapper;
import com.relictrace.mapper.RelicInfoMapper;
import com.relictrace.mapper.RelicQrCodeMapper;
import com.relictrace.mapper.RelicStoragePositionMapper;
import com.relictrace.mapper.RelicTraceRecordMapper;
import com.relictrace.mapper.RelicWarehouseMapper;
import com.relictrace.mapper.SysConfigMapper;
import com.relictrace.po.RelicAttachmentDO;
import com.relictrace.po.RelicCategoryDO;
import com.relictrace.po.RelicInfoDO;
import com.relictrace.po.RelicQrCodeDO;
import com.relictrace.po.RelicStoragePositionDO;
import com.relictrace.po.RelicTraceRecordDO;
import com.relictrace.po.RelicWarehouseDO;
import com.relictrace.po.SysConfigDO;
import com.relictrace.query.RelicQuery;
import com.relictrace.service.QrCodeService;
import com.relictrace.service.RelicService;
import com.relictrace.vo.AttachmentVO;
import com.relictrace.vo.RelicVO;
import com.relictrace.vo.TraceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 藏品信息服务实现。
 *
 * @author 李丰
 */
@Slf4j
@Service
public class RelicServiceImpl implements RelicService {

    /** 附件上传允许的扩展名（影像与协议文档） */
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp", "gif", "bmp", "pdf");

    /** 附件文件名的落盘时间戳格式 */
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /** 附件根目录（与 WebConfig 的静态资源映射保持一致） */
    @Value("${relic.upload-dir:uploads}")
    private String uploadDir;

    /** 单个附件大小上限（MB） */
    @Value("${relic.upload.max-size:20}")
    private long uploadMaxSize;

    @Autowired
    private RelicInfoMapper relicInfoMapper;

    @Autowired
    private RelicCategoryMapper relicCategoryMapper;

    @Autowired
    private RelicWarehouseMapper relicWarehouseMapper;

    @Autowired
    private RelicStoragePositionMapper relicStoragePositionMapper;

    @Autowired
    private RelicTraceRecordMapper relicTraceRecordMapper;

    @Autowired
    private RelicQrCodeMapper relicQrCodeMapper;

    @Autowired
    private RelicAttachmentMapper relicAttachmentMapper;

    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private QrCodeService qrCodeService;

    @Override
    public PageResult<RelicVO> pageRelic(RelicQuery query) {
        LambdaQueryWrapper<RelicInfoDO> wrapper = buildQueryWrapper(query, false);
        IPage<RelicInfoDO> page = relicInfoMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        List<RelicVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        fillCovers(voList);
        return new PageResult<>(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public RelicVO getRelic(String id) {
        RelicInfoDO relic = relicInfoMapper.selectById(id);
        if (relic == null) {
            throw new BusinessException("藏品不存在或已被删除");
        }
        RelicVO vo = toVO(relic);

        // 填充溯源记录
        List<RelicTraceRecordDO> traces = relicTraceRecordMapper.selectList(
                new LambdaQueryWrapper<RelicTraceRecordDO>()
                        .eq(RelicTraceRecordDO::getFkRelicId, id)
                        .orderByAsc(RelicTraceRecordDO::getTraceTime)
        );
        vo.setTraceList(traces.stream().map(this::toTraceVO).collect(Collectors.toList()));

        // 填充附件列表
        List<RelicAttachmentDO> attachments = relicAttachmentMapper.selectList(
                new LambdaQueryWrapper<RelicAttachmentDO>()
                        .eq(RelicAttachmentDO::getFkRelicId, id)
                        .orderByDesc(RelicAttachmentDO::getUploadTime)
        );
        vo.setAttachmentList(attachments.stream().map(this::toAttachmentVO).collect(Collectors.toList()));

        // 详情页封面：取首张藏品影像（file_type = 1）
        attachments.stream()
                .filter(a -> a.getFileType() != null && a.getFileType() == 1)
                .findFirst()
                .ifPresent(a -> vo.setCover(toUploadUrl(a.getFilePath())));

        // 溯源二维码：缺失时自动补建，保证详情页与档案抽屉始终有可下载的二维码
        vo.setQrcode(qrCodeService.getOrCreate(relic.getId()));

        return vo;
    }

    @Override
    public RelicVO getPublicRelic(String id) {
        RelicVO vo = getRelic(id);
        if (vo.getIsPublic() == null || vo.getIsPublic() != 1) {
            throw new BusinessException("该藏品为保密藏品，暂不对外公开展示");
        }
        return vo;
    }

    @Override
    public String createRelic(RelicInfoDO relic) {
        // 藏品编号：前端可留空，由系统按「系统参数 relic_code.prefix + 4 位流水」生成
        if (!StringUtils.hasText(relic.getRelicCode())) {
            relic.setRelicCode(generateRelicCode());
        }
        // 藏品编号唯一性校验
        if (StringUtils.hasText(relic.getRelicCode())) {
            Long count = relicInfoMapper.selectCount(
                    new LambdaQueryWrapper<RelicInfoDO>().eq(RelicInfoDO::getRelicCode, relic.getRelicCode())
            );
            if (count != null && count > 0) {
                throw new BusinessException("藏品编号已存在：" + relic.getRelicCode());
            }
        }
        // 默认数量为 1
        if (relic.getQuantity() == null) {
            relic.setQuantity(1);
        }
        // 默认库存状态为在库
        if (relic.getStockStatus() == null) {
            relic.setStockStatus(1);
        }
        // 默认保密；表结构未给 is_public 设默认值，缺省会导致插入失败
        if (relic.getIsPublic() == null) {
            relic.setIsPublic(0);
        }
        relicInfoMapper.insert(relic);

        // 入藏溯源记录
        addTrace(relic.getId(), 2, null, "藏品编目入藏：" + relic.getRelicName());

        // 编目建档即生成溯源二维码（内容内嵌藏品编号，可下载打印后贴附）
        if (StringUtils.hasText(relic.getRelicCode())) {
            qrCodeService.rebuild(relic.getId());
        }
        return relic.getId();
    }

    /**
     * 生成藏品编号。
     *
     * <p>前缀读取系统参数 {@code relic_code.prefix}（缺省 RW），流水号取同前缀最大值加一，
     * 补足 4 位，形如 {@code RW-0001}。前端编目表单允许留空，留空即走此规则。</p>
     */
    private String generateRelicCode() {
        String prefixValue = "RW";
        SysConfigDO config = configMapper.selectOne(new LambdaQueryWrapper<SysConfigDO>()
                .eq(SysConfigDO::getConfigKey, "relic_code.prefix")
                .last("limit 1"));
        if (config != null && StringUtils.hasText(config.getConfigValue())) {
            prefixValue = config.getConfigValue().trim();
        }
        String prefix = prefixValue + "-";
        return BizCodeUtils.nextCode(relicInfoMapper.selectMaxRelicCode(prefix), prefix, 4);
    }

    @Override
    public void updateRelic(String id, RelicInfoDO relic) {
        RelicInfoDO exist = relicInfoMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("藏品不存在或已被删除");
        }
        relic.setId(id);
        relicInfoMapper.updateById(relic);
    }

    @Override
    public void deleteRelic(String id) {
        RelicInfoDO exist = relicInfoMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("藏品不存在或已被删除");
        }
        relicInfoMapper.deleteById(id);
    }

    @Override
    public void batchDeleteRelic(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的藏品");
        }
        relicInfoMapper.deleteBatchIds(ids);
    }

    @Override
    public List<TraceVO> getRelicTrace(String id) {
        List<RelicTraceRecordDO> traces = relicTraceRecordMapper.selectList(
                new LambdaQueryWrapper<RelicTraceRecordDO>()
                        .eq(RelicTraceRecordDO::getFkRelicId, id)
                        .orderByAsc(RelicTraceRecordDO::getTraceTime)
        );
        return traces.stream().map(this::toTraceVO).collect(Collectors.toList());
    }

    @Override
    public RelicVO getRelicByCode(String code) {
        // 按藏品编号或二维码内容查询
        RelicInfoDO relic = relicInfoMapper.selectOne(
                new LambdaQueryWrapper<RelicInfoDO>()
                        .eq(RelicInfoDO::getRelicCode, code)
        );
        if (relic == null) {
            // 尝试按二维码内容匹配
            relic = relicInfoMapper.selectOne(
                    new LambdaQueryWrapper<RelicInfoDO>().eq(RelicInfoDO::getQrcodeContent, code)
            );
        }
        if (relic == null) {
            throw new BusinessException("未查询到对应藏品");
        }

        // 更新扫码次数
        RelicQrCodeDO qrCode = relicQrCodeMapper.selectOne(
                new LambdaQueryWrapper<RelicQrCodeDO>().eq(RelicQrCodeDO::getFkRelicId, relic.getId())
        );
        if (qrCode != null) {
            qrCode.setScanCount((qrCode.getScanCount() == null ? 0 : qrCode.getScanCount()) + 1);
            qrCode.setLastScanTime(LocalDateTime.now());
            relicQrCodeMapper.updateById(qrCode);
        }

        RelicVO vo = toVO(relic);
        if (qrCode != null) {
            vo.setScanCount(qrCode.getScanCount());
        }
        return vo;
    }

    @Override
    public List<RelicVO> listHighlights() {
        // 精选：公开 + 在库，按创建时间倒序取前 N 条
        List<RelicInfoDO> list = relicInfoMapper.selectList(
                new LambdaQueryWrapper<RelicInfoDO>()
                        .eq(RelicInfoDO::getIsPublic, 1)
                        .eq(RelicInfoDO::getStockStatus, 1)
                        .orderByDesc(RelicInfoDO::getCreateTime)
                        .last("limit 12")
        );
        List<RelicVO> voList = list.stream().map(this::toVO).collect(Collectors.toList());
        fillCovers(voList);
        return voList;
    }

    @Override
    public PageResult<RelicVO> searchPublic(RelicQuery query) {
        // 公开检索：仅返回 is_public = 1 的藏品
        LambdaQueryWrapper<RelicInfoDO> wrapper = buildQueryWrapper(query, true);
        IPage<RelicInfoDO> page = relicInfoMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<RelicVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        fillCovers(voList);
        return new PageResult<>(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public List<RelicCategoryDO> listCategories() {
        return relicCategoryMapper.selectList(
                new LambdaQueryWrapper<RelicCategoryDO>().orderByAsc(RelicCategoryDO::getSortNo)
        );
    }

    @Override
    public AttachmentVO uploadAttachment(String relicId, MultipartFile file, Integer fileType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        RelicInfoDO relic = relicInfoMapper.selectById(relicId);
        if (relic == null) {
            throw new BusinessException("藏品不存在或已被删除");
        }
        long maxBytes = uploadMaxSize * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException("单个附件不能超过 " + uploadMaxSize + " MB");
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalName);
        extension = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的文件类型，仅允许 jpg / jpeg / png / webp / gif / bmp / pdf");
        }

        // 落盘文件名采用「藏品编号_时间戳.扩展名」，规避中文名、重名与路径穿越问题
        String baseName = StringUtils.hasText(relic.getRelicCode()) ? relic.getRelicCode() : "relic";
        String storedName = baseName + "_" + LocalDateTime.now().format(FILE_STAMP) + "." + extension;
        String relativePath = "relics/" + storedName;

        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException("非法的文件路径");
        }
        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("附件落盘失败：{}", target, e);
            throw new BusinessException("文件保存失败，请稍后重试");
        }

        RelicAttachmentDO attachment = new RelicAttachmentDO();
        attachment.setFkRelicId(relicId);
        attachment.setFileName(originalName);
        attachment.setFilePath(relativePath);
        attachment.setFileType(fileType == null ? 1 : fileType);
        attachment.setFileSize(file.getSize());
        attachment.setUploadUserId(LoginUser.currentUserId());
        attachment.setUploadUserName(LoginUser.currentUserName());
        attachment.setUploadTime(LocalDateTime.now());
        attachment.setIsDeleted(0);
        relicAttachmentMapper.insert(attachment);
        return toAttachmentVO(attachment);
    }

    @Override
    public void deleteAttachment(String attachmentId) {
        RelicAttachmentDO exist = relicAttachmentMapper.selectById(attachmentId);
        if (exist == null) {
            throw new BusinessException("附件不存在或已被删除");
        }
        relicAttachmentMapper.deleteById(attachmentId);

        // 物理文件清理：校验路径必须位于 uploads 根目录内，防止越权删除
        try {
            Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target = root.resolve(exist.getFilePath()).normalize();
            if (target.startsWith(root)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException e) {
            log.warn("附件文件清理失败：{}", exist.getFilePath(), e);
        }
    }

    /**
     * 构造查询条件。
     *
     * @param query     查询对象
     * @param onlyPublic 是否仅查询公开藏品（前台检索）
     * @return 查询包装器
     */
    private LambdaQueryWrapper<RelicInfoDO> buildQueryWrapper(RelicQuery query, boolean onlyPublic) {
        LambdaQueryWrapper<RelicInfoDO> wrapper = new LambdaQueryWrapper<>();
        // 关键字：名称 / 编号模糊匹配
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(RelicInfoDO::getRelicName, query.getKeyword())
                    .or().like(RelicInfoDO::getRelicCode, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getFkCategoryId())) {
            wrapper.eq(RelicInfoDO::getFkCategoryId, query.getFkCategoryId());
        }
        if (StringUtils.hasText(query.getEra())) {
            wrapper.eq(RelicInfoDO::getEra, query.getEra());
        }
        if (query.getRelicLevel() != null) {
            wrapper.eq(RelicInfoDO::getRelicLevel, query.getRelicLevel());
        }
        if (query.getStockStatus() != null) {
            wrapper.eq(RelicInfoDO::getStockStatus, query.getStockStatus());
        }
        if (query.getIsPublic() != null) {
            wrapper.eq(RelicInfoDO::getIsPublic, query.getIsPublic());
        }
        if (onlyPublic) {
            wrapper.eq(RelicInfoDO::getIsPublic, 1);
        }
        // 排序
        String sort = query.getSort();
        if ("level".equals(sort)) {
            wrapper.orderByAsc(RelicInfoDO::getRelicLevel);
        } else if ("oldest".equals(sort)) {
            wrapper.orderByAsc(RelicInfoDO::getCreateTime);
        } else {
            wrapper.orderByDesc(RelicInfoDO::getCreateTime);
        }
        return wrapper;
    }

    /** 实体转展示对象（填充关联名称） */
    /**
     * 批量填充封面影像地址。
     *
     * <p>一次查询取回当前列表中全部藏品的影像附件，避免逐条查询造成的 N+1 问题；
     * 同一藏品多张影像时取上传时间最早的一张作为封面。</p>
     *
     * @param voList 藏品展示对象列表
     */
    private void fillCovers(List<RelicVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        List<String> relicIds = voList.stream()
                .map(RelicVO::getId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        if (relicIds.isEmpty()) {
            return;
        }
        List<RelicAttachmentDO> images = relicAttachmentMapper.selectList(
                new LambdaQueryWrapper<RelicAttachmentDO>()
                        .in(RelicAttachmentDO::getFkRelicId, relicIds)
                        .eq(RelicAttachmentDO::getFileType, 1)
                        .orderByAsc(RelicAttachmentDO::getUploadTime)
        );
        Map<String, String> coverMap = images.stream()
                .collect(Collectors.toMap(
                        RelicAttachmentDO::getFkRelicId,
                        attachment -> toUploadUrl(attachment.getFilePath()),
                        (first, second) -> first
                ));
        voList.forEach(vo -> vo.setCover(coverMap.get(vo.getId())));
    }

    /**
     * 附件相对路径转可访问地址。
     *
     * @param filePath 数据库中存储的相对路径，如 relics/TC-0001.jpg
     * @return 形如 /uploads/relics/TC-0001.jpg 的地址；路径为空时返回 null
     */
    private String toUploadUrl(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        return filePath.startsWith("/") ? filePath : "/uploads/" + filePath;
    }

    private RelicVO toVO(RelicInfoDO relic) {
        RelicVO vo = new RelicVO();
        BeanUtils.copyProperties(relic, vo);

        // 分类名称
        if (StringUtils.hasText(relic.getFkCategoryId())) {
            RelicCategoryDO category = relicCategoryMapper.selectById(relic.getFkCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }
        // 库房名称
        if (StringUtils.hasText(relic.getFkWarehouseId())) {
            RelicWarehouseDO warehouse = relicWarehouseMapper.selectById(relic.getFkWarehouseId());
            if (warehouse != null) {
                vo.setWarehouseName(warehouse.getWarehouseName());
            }
        }
        // 仓位名称
        if (StringUtils.hasText(relic.getFkPositionId())) {
            RelicStoragePositionDO position = relicStoragePositionMapper.selectById(relic.getFkPositionId());
            if (position != null) {
                vo.setPositionName(position.getPositionName());
            }
        }
        return vo;
    }

    /** 溯源实体转展示对象 */
    private TraceVO toTraceVO(RelicTraceRecordDO trace) {
        TraceVO vo = new TraceVO();
        BeanUtils.copyProperties(trace, vo);
        return vo;
    }

    /** 附件实体转展示对象 */
    private AttachmentVO toAttachmentVO(RelicAttachmentDO attachment) {
        AttachmentVO vo = new AttachmentVO();
        BeanUtils.copyProperties(attachment, vo);
        return vo;
    }

    /**
     * 添加溯源记录（供各业务模块调用）。
     *
     * @param relicId 藏品 id
     * @param traceType 溯源类型
     * @param bizId 关联业务 id
     * @param content 溯源内容
     */
    public void addTrace(String relicId, Integer traceType, String bizId, String content) {
        RelicTraceRecordDO trace = new RelicTraceRecordDO();
        trace.setFkRelicId(relicId);
        trace.setTraceType(traceType);
        trace.setFkBizId(bizId);
        trace.setTraceTime(LocalDateTime.now());
        trace.setOperatorName(LoginUser.currentUserName());
        trace.setTraceContent(content);
        trace.setCreateUserId(LoginUser.currentUserId());
        trace.setCreateUserName(LoginUser.currentUserName());
        trace.setCreateTime(LocalDateTime.now());
        trace.setIsDeleted(0);
        relicTraceRecordMapper.insert(trace);
    }
}
