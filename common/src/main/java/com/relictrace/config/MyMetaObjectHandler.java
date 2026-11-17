package com.relictrace.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.relictrace.common.LoginUser;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器。
 *
 * <p>在插入 / 更新时自动填充审计字段：
 * 创建人、创建时间、修改人、修改时间。逻辑删除字段 is_deleted 默认 0。</p>
 *
 * @author 李丰
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        String userId = LoginUser.currentUserId();
        String userName = LoginUser.currentUserName();
        LocalDateTime now = LocalDateTime.now();

        this.strictInsertFill(metaObject, "createUserId", String.class, userId);
        this.strictInsertFill(metaObject, "createUserName", String.class, userName);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateUserId", String.class, userId);
        this.strictInsertFill(metaObject, "updateUserName", String.class, userName);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        // 逻辑删除字段默认 0
        this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = LoginUser.currentUserId();
        String userName = LoginUser.currentUserName();

        this.strictUpdateFill(metaObject, "updateUserId", String.class, userId);
        this.strictUpdateFill(metaObject, "updateUserName", String.class, userName);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
