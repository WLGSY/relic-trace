package com.relictrace.config;

import com.relictrace.common.AuthInterceptor;
import com.relictrace.common.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 配置。
 *
 * <p>负责：CORS 跨域配置、认证拦截器注册、静态资源映射。</p>
 *
 * @author 李丰
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private RoleInterceptor roleInterceptor;

    /** 附件根目录，默认工程根下的 uploads（可通过 relic.upload-dir 覆盖） */
    @Value("${relic.upload-dir:uploads}")
    private String uploadDir;

    /**
     * CORS 配置。
     * 开发环境前端运行在 Vite（localhost:5173），此处显式放行；
     * 生产环境由 Nginx 同源反代，可关闭。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 注册认证与角色鉴权拦截器。
     *
     * <p>执行顺序：先 {@link AuthInterceptor} 解析 JWT 写入登录上下文（order = 1），
     * 再由 {@link RoleInterceptor} 依据 {@code @RequireRole} 校验角色（order = 2）。
     * 登录、公开溯源、健康检查等路径无需鉴权。</p>
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .order(1)
                .excludePathPatterns(
                        "/auth/login",
                        // 前台检索需要分类下拉框，分类属公开字典数据
                        "/categories",
                        "/relics/public/**",
                        "/relics/by-code/**",
                        "/stats/public",
                        // 前台数据看板的流转趋势同样是公开数据，匿名访客也需要
                        "/stats/stock-trend",
                        // 藏品影像等静态附件：前台展示无需登录
                        "/uploads/**",
                        "/error"
                );
        // 角色鉴权：仅拦截标注 @RequireRole 的接口，其余接口不受影响
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/**")
                .order(2);
    }

    /**
     * 静态资源映射：把 uploads 目录暴露为 /uploads/**。
     *
     * <p>藏品影像、修复前后照片等文件落盘在工程根目录的 uploads 下，
     * 数据库只存相对路径（如 relics/TC-0001.jpg），由本映射转换为可访问 URL。
     * 由于 context-path 为 /api，实际访问地址为 /api/uploads/relics/TC-0001.jpg。</p>
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize()
                .toString().replace("\\", "/");
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + location)
                .setCachePeriod(86400);
    }
}
