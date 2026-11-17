package com.relictrace.service;

import com.relictrace.common.PageResult;
import com.relictrace.po.RelicCollectionApplyDO;
import com.relictrace.query.CollectionQuery;

/**
 * 藏品征集申请服务接口。
 *
 * @author 李丰
 */
public interface CollectionService {

    /**
     * 分页查询征集申请（支持关键字、状态、分类、年代、征集方式与时间区间筛选）。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<RelicCollectionApplyDO> page(CollectionQuery query);

    /**
     * 查询征集申请详情。
     *
     * @param id 申请 id
     * @return 申请实体
     */
    RelicCollectionApplyDO get(String id);

    /**
     * 新增征集申请。
     *
     * @param apply 申请实体
     * @return 申请 id
     */
    String create(RelicCollectionApplyDO apply);

    /**
     * 修改征集申请。
     *
     * @param id    申请 id
     * @param apply 申请实体
     */
    void update(String id, RelicCollectionApplyDO apply);

    /**
     * 提交审核（草稿 -> 待审核）。
     *
     * @param id 申请 id
     */
    void submit(String id);

    /**
     * 删除征集申请。
     *
     * @param id 申请 id
     */
    void delete(String id);

    /**
     * 批量删除征集申请。
     *
     * @param ids 申请 id 列表
     */
    void batchDelete(java.util.List<String> ids);
}
