package com.relictrace.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.relictrace.common.PageResult;
import com.relictrace.po.RelicCategoryDO;
import com.relictrace.po.RelicInfoDO;
import com.relictrace.query.RelicQuery;
import com.relictrace.vo.AttachmentVO;
import com.relictrace.vo.RelicVO;
import com.relictrace.vo.TraceVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 藏品信息服务接口。
 *
 * @author 李丰
 */
public interface RelicService {

    /**
     * 分页查询藏品档案。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<RelicVO> pageRelic(RelicQuery query);

    /**
     * 查询藏品详情（含附件、二维码、溯源、征集来源）。
     *
     * @param id 藏品 id
     * @return 藏品详情
     */
    RelicVO getRelic(String id);

    /**
     * 查询公开藏品详情（前台使用）。
     *
     * <p>在 {@link #getRelic(String)} 基础上增加保密校验，非公开藏品直接拒绝访问，
     * 避免前台通过猜测 id 读取保密档案。</p>
     *
     * @param id 藏品 id
     * @return 藏品详情
     */
    RelicVO getPublicRelic(String id);

    /**
     * 编目建档（新增藏品）。
     *
     * @param relic 藏品实体
     * @return 藏品 id
     */
    String createRelic(RelicInfoDO relic);

    /**
     * 修改藏品档案。
     *
     * @param id    藏品 id
     * @param relic 藏品实体
     */
    void updateRelic(String id, RelicInfoDO relic);

    /**
     * 逻辑删除藏品。
     *
     * @param id 藏品 id
     */
    void deleteRelic(String id);

    /**
     * 批量逻辑删除藏品。
     *
     * @param ids 藏品 id 列表
     */
    void batchDeleteRelic(List<String> ids);

    /**
     * 查询藏品溯源时间线。
     *
     * @param id 藏品 id
     * @return 溯源记录列表
     */
    List<TraceVO> getRelicTrace(String id);

    /**
     * 按藏品编号 / 二维码内容查询（扫码溯源，公开接口）。
     *
     * @param code 藏品编号或二维码内容
     * @return 藏品公开信息
     */
    RelicVO getRelicByCode(String code);

    /**
     * 查询精选藏品（公开接口）。
     *
     * @return 精选藏品列表
     */
    List<RelicVO> listHighlights();

    /**
     * 公开藏品检索（公开接口）。
     *
     * @param query 检索条件
     * @return 分页结果
     */
    PageResult<RelicVO> searchPublic(RelicQuery query);

    /**
     * 查询藏品分类列表。
     *
     * @return 分类列表
     */
    List<RelicCategoryDO> listCategories();

    /**
     * 上传藏品附件（影像资料、修复照片、协议等）。
     *
     * <p>文件落盘到 {@code relic.upload-dir/relics/} 下，数据库仅保存相对路径，
     * 由静态资源映射转换为可访问地址。</p>
     *
     * @param relicId  藏品 id
     * @param file     上传文件
     * @param fileType 附件类型：1 藏品影像 / 2 修复前 / 3 修复后 / 4 借调协议 / 5 其他
     * @return 附件信息（含相对路径）
     */
    AttachmentVO uploadAttachment(String relicId, MultipartFile file, Integer fileType);

    /**
     * 删除藏品附件（逻辑删除，同时清理磁盘文件）。
     *
     * @param attachmentId 附件 id
     */
    void deleteAttachment(String attachmentId);
}
