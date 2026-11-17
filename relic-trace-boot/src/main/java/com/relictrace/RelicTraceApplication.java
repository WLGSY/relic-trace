package com.relictrace;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 文物藏品管理与溯源系统 · 后端启动类。
 *
 * <p>技术栈：Spring Boot 3 + MyBatis-Plus + MySQL 8.0。</p>
 * <p>通过 {@code @MapperScan} 扫描数据访问层接口（com.relictrace.mapper）。</p>


 * @author 李丰
 */
@SpringBootApplication
@MapperScan("com.relictrace.mapper")
public class RelicTraceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RelicTraceApplication.class, args);
    }
}
