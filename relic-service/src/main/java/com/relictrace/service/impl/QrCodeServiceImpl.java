package com.relictrace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.relictrace.common.BusinessException;
import com.relictrace.common.LoginUser;
import com.relictrace.mapper.RelicInfoMapper;
import com.relictrace.mapper.RelicQrCodeMapper;
import com.relictrace.po.RelicInfoDO;
import com.relictrace.po.RelicQrCodeDO;
import com.relictrace.service.QrCodeService;
import com.relictrace.vo.QrCodeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 溯源二维码服务实现。
 *
 * <p>二维码内容为一段可被手机相机直接识别的 URL：
 * <pre>{base-url}/#/scan?code={藏品编号}</pre>
 * 扫码后进入前台“扫码溯源”页并自动跳转到该藏品详情。
 * 图片以 PNG 落盘到 {@code uploads/qrcodes/} 下，经 {@code /uploads/**} 静态映射对外提供。</p>
 *
 * @author 李丰
 */
@Service
public class QrCodeServiceImpl implements QrCodeService {

    private static final Logger log = LoggerFactory.getLogger(QrCodeServiceImpl.class);

    /** 二维码图片相对上传根目录的子目录 */
    private static final String QR_SUBDIR = "qrcodes";

    /** 对外访问前缀（与 WebConfig 的 /uploads/** 映射保持一致） */
    private static final String UPLOAD_URL_PREFIX = "/uploads/";

    @Autowired
    private RelicInfoMapper relicInfoMapper;

    @Autowired
    private RelicQrCodeMapper relicQrCodeMapper;

    /** 附件根目录，与藏品影像共用同一目录 */
    @Value("${relic.upload-dir:uploads}")
    private String uploadDir;

    /**
     * 扫码后跳转的前端地址前缀。
     *
     * <p>默认 http://localhost 只适合本机演示；用手机扫码时需改为电脑的局域网地址，
     * 例如 http://192.168.1.10，然后调用批量重建接口刷新二维码内容。</p>
     */
    @Value("${relic.qr.base-url:http://localhost}")
    private String qrBaseUrl;

    /** 二维码图片边长（像素） */
    @Value("${relic.qr.size:360}")
    private int qrSize;

    @Override
    public QrCodeVO getOrCreate(String relicId) {
        RelicInfoDO relic = relicInfoMapper.selectById(relicId);
        if (relic == null) {
            throw new BusinessException("藏品不存在或已被删除");
        }
        return toVO(ensure(relic, false));
    }

    @Override
    public QrCodeVO rebuild(String relicId) {
        RelicInfoDO relic = relicInfoMapper.selectById(relicId);
        if (relic == null) {
            throw new BusinessException("藏品不存在或已被删除");
        }
        return toVO(ensure(relic, true));
    }

    @Override
    public int rebuildAll() {
        List<RelicInfoDO> relics = relicInfoMapper.selectList(null);
        int count = 0;
        for (RelicInfoDO relic : relics) {
            if (!StringUtils.hasText(relic.getRelicCode())) {
                continue;
            }
            try {
                ensure(relic, true);
                count++;
            } catch (Exception e) {
                // 单件失败不影响其余藏品，记录后继续
                log.warn("藏品 {} 二维码生成失败：{}", relic.getRelicCode(), e.getMessage());
            }
        }
        log.info("批量重建溯源二维码完成，共 {} 件", count);
        return count;
    }

    /**
     * 确保某件藏品存在有效二维码：记录缺失、内容变更或图片文件丢失时重新生成。
     *
     * @param relic 藏品实体
     * @param force 是否强制重写
     * @return 二维码记录
     */
    private RelicQrCodeDO ensure(RelicInfoDO relic, boolean force) {
        String content = buildContent(relic);
        RelicQrCodeDO record = relicQrCodeMapper.selectOne(
                new LambdaQueryWrapper<RelicQrCodeDO>().eq(RelicQrCodeDO::getFkRelicId, relic.getId())
        );

        boolean contentChanged = record != null && !content.equals(record.getQrContent());
        boolean fileMissing = record == null || !Files.exists(resolveFile(record.getQrPath()));
        if (!force && record != null && !contentChanged && !fileMissing) {
            return record;
        }

        String relativePath = QR_SUBDIR + "/" + safeFileName(relic.getRelicCode()) + ".png";
        Path target = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(relativePath);
        writePng(content, target);

        LocalDateTime now = LocalDateTime.now();
        String operatorId = defaultIfBlank(LoginUser.currentUserId(), "system");
        String operatorName = defaultIfBlank(LoginUser.currentUserName(), "系统");

        if (record == null) {
            record = new RelicQrCodeDO();
            record.setFkRelicId(relic.getId());
            record.setScanCount(0);
            record.setCreateUserId(operatorId);
            record.setCreateUserName(operatorName);
            record.setCreateTime(now);
            record.setIsDeleted(0);
        }
        record.setQrContent(content);
        record.setQrPath(UPLOAD_URL_PREFIX + relativePath);
        record.setGenerateUserName(operatorName);
        record.setGenerateTime(now);

        if (record.getId() == null) {
            relicQrCodeMapper.insert(record);
        } else {
            relicQrCodeMapper.updateById(record);
        }
        log.info("已生成溯源二维码：{} -> {}", relic.getRelicCode(), record.getQrPath());
        return record;
    }

    /**
     * 组装二维码内容：指向前台扫码溯源页，并带上藏品编号。
     */
    private String buildContent(RelicInfoDO relic) {
        String base = resolveBaseUrl();
        return base + "/#/scan?code=" + relic.getRelicCode();
    }

    /**
     * 解析扫码地址前缀。
     *
     * <p>配置为空时退化为“当前请求的协议 + 主机”，再退化为 http://localhost，
     * 保证未做任何配置时也能生成可用的二维码。</p>
     */
    private String resolveBaseUrl() {
        if (StringUtils.hasText(qrBaseUrl)) {
            return trimTrailingSlash(qrBaseUrl.trim());
        }
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String scheme = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();
            boolean defaultPort = ("http".equals(scheme) && port == 80)
                    || ("https".equals(scheme) && port == 443);
            return scheme + "://" + host + (defaultPort ? "" : ":" + port);
        }
        return "http://localhost";
    }

    /**
     * 用 ZXing 生成二维码矩阵并写出 PNG。
     *
     * <p>使用 M 级纠错（约 15% 容错），即使二维码表面有轻微磨损或反光仍可识别。</p>
     */
    private void writePng(String content, Path target) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0xFF14100C : 0xFFFFFFFF);
                }
            }

            Files.createDirectories(target.getParent());
            ImageIO.write(image, "PNG", target.toFile());
        } catch (Exception e) {
            throw new BusinessException("二维码图片生成失败：" + e.getMessage());
        }
    }

    /** 相对访问路径（/uploads/qrcodes/x.png）转换为磁盘绝对路径 */
    private Path resolveFile(String qrPath) {
        String relative = qrPath == null ? "" : qrPath;
        if (relative.startsWith(UPLOAD_URL_PREFIX)) {
            relative = relative.substring(UPLOAD_URL_PREFIX.length());
        } else if (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(relative);
    }

    /** 文件名只保留字母、数字、下划线与连字符，避免编号中的特殊字符影响路径 */
    private String safeFileName(String relicCode) {
        return relicCode.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private String trimTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private QrCodeVO toVO(RelicQrCodeDO record) {
        QrCodeVO vo = new QrCodeVO();
        vo.setId(record.getId());
        vo.setFkRelicId(record.getFkRelicId());
        vo.setContent(record.getQrContent());
        vo.setImageUrl(record.getQrPath());
        vo.setGenerateUserName(record.getGenerateUserName());
        vo.setGenerateTime(record.getGenerateTime());
        vo.setScanCount(record.getScanCount() == null ? 0 : record.getScanCount());
        vo.setLastScanTime(record.getLastScanTime());
        return vo;
    }
}
