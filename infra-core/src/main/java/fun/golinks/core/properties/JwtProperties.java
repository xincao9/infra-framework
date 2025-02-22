package fun.golinks.core.properties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

@Slf4j
@ConfigurationProperties(prefix = "infra.web.jwt")
public class JwtProperties {

    private String secretKey = "Secret key is too short, it must be at least 32 bytes long";
    private byte[] keyBytes = Arrays.copyOf(secretKey.getBytes(), 32);

    private SecretKey key = new SecretKeySpec(keyBytes, "AES");

    public void setSecretKey(String secretKey) {
        if (secretKey.getBytes().length < 32) {
            log.error("Secret key is too short, it must be at least 32 bytes long");
            return;
        }
        this.secretKey = secretKey;
        this.keyBytes = Arrays.copyOf(secretKey.getBytes(), 32);
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser().decryptWith(key).build().parseEncryptedClaims(token).getPayload();
        } catch (Throwable e) {
            log.error("Invalid JWT token", e);
            return null;
        }
    }

    public boolean isInvalid(String token) {
        return parseToken(token) == null;
    }

    public String generatorToken(String subject, Map<String, Object> claims, long expirationMillis) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        // 过期时间
        Date expiryDate = new Date(nowMillis + expirationMillis);
        return Jwts.builder().claims(claims).subject(subject) // 设置 subject（如用户 ID）
                .issuedAt(now) // 签发时间
                .expiration(expiryDate) // 过期时间
                .encryptWith(key, Jwts.ENC.A256GCM) // 使用 AES-256-GCM 加密
                .compact();
    }
}
