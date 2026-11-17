package com.relictrace.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Jackson 日期时间配置。
 *
 * <p>前端日期选择器存在两种取值形态：带时间的 {@code 2026-11-12 09:30:00} 与
 * 仅到日的 {@code 2026-11-12}。默认的 JSR-310 反序列化器只接受注解中声明的
 * 单一格式，传纯日期会抛 {@code DateTimeParseException}，接口直接返回 500。</p>
 *
 * <p>这里注册统一的 LocalDateTime 反序列化器：依次尝试多种常见格式，
 * 纯日期按当日 00:00:00 处理；序列化仍固定输出 {@code yyyy-MM-dd HH:mm:ss}，
 * 保证前端展示格式不变。</p>
 *
 * @author 李丰
 */
@Configuration
public class JacksonConfig {

    /** 统一输出格式 */
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 依次尝试的输入格式（覆盖带时间、ISO、精确到分钟三种写法） */
    private static final List<DateTimeFormatter> INPUT_FORMATTERS = List.of(
            DATE_TIME_FORMATTER,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    );

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer relicDateTimeCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());
            module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
            builder.modulesToInstall(module);
        };
    }

    /**
     * 容错的 LocalDateTime 反序列化器。
     *
     * <p>不使用字段上的 {@code @JsonFormat} 模式，因此对全部实体字段统一生效。</p>
     */
    static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String text = parser.getText();
            if (text == null || text.isBlank()) {
                return null;
            }
            String value = text.trim();
            for (DateTimeFormatter formatter : INPUT_FORMATTERS) {
                try {
                    return LocalDateTime.parse(value, formatter);
                } catch (DateTimeParseException ignored) {
                    // 继续尝试下一种格式
                }
            }
            try {
                // 仅到日的写法：补 00:00:00，避免前端日期选择器直接报 500
                return LocalDate.parse(value).atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new IOException("日期时间格式不正确：" + value, e);
            }
        }
    }
}
