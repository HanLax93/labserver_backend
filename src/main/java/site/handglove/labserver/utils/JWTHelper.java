package site.handglove.labserver.utils;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;
import java.util.Date;
import javax.crypto.SecretKey;

public class JWTHelper {

    private static long tokenExpiration = 365 * 24 * 60 * 60 * 1000;
    private static SecretKey secretKey = Jwts.SIG.HS256.key().build();

    public static String createToken(String username) {
        String token = Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .subject(username)
                .signWith(secretKey)
                .compressWith(Jwts.ZIP.GZIP)
                .compact();
        return token;
    }

    public static String getUsername(String token) {
        try {
            if (!StringUtils.hasLength(token))
                return "";

            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(secretKey).build()
                    .parseSignedClaims(token);
            Claims claims = claimsJws.getPayload();
            return (String) claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String token = JWTHelper.createToken("wangjingli");
        System.out.println(token);
        System.out.println(JWTHelper.getUsername(token));
    }
}
