package com.relictrace.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类。
 *
 * <p>签发与校验访问令牌。令牌载荷包含用户 id、登录账号、真实姓名、角色类型。</p>
 *
 * @author 李丰
 */
@Component
public class JwtUtil {

    /** 签名密钥 */
    @Value("${relic.jwt.secret}")
    private String secret;

    /** 令牌有效期（毫秒） */
    @Value("${relic.jwt.expire}")
    private long expire;

    /** 获取签名密钥 */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 签发访问令牌。
     *
     * @param userId   用户 id
     * @param loginName 登录账号
     * @param realName 真实姓名
     * @param roleType 角色类型
     * @return JWT 字符串
     */
    public String generateToken(String userId, String loginName, String realName, Integer roleType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expire);
        return Jwts.builder()
                .subject(userId)
                .claim("loginName", loginName)
                .claim("realName", realName)
                .claim("roleType", roleType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析令牌，返回载荷。令牌无效或过期时返回 null。
     *
     * @param token JWT 字符串
     * @return 载荷对象，失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
