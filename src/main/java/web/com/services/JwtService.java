package web.com.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, JWTClaimsSet::getSubject);
    }

    public <T> T extractClaim(String token, Function<JWTClaimsSet, T> claimsResolver) {
        final JWTClaimsSet claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        try {
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + expiration));

            if (extraClaims != null) {
                extraClaims.forEach(builder::claim);
            }

            JWTClaimsSet claimsSet = builder.build();

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            JWSSigner signer = new MACSigner(getSecretBytes());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Lỗi tạo và ký JWT với Nimbus JOSE: " + e.getMessage(), e);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            JWSVerifier verifier = new MACVerifier(getSecretBytes());
            if (!signedJWT.verify(verifier)) {
                return false;
            }

            final String username = signedJWT.getJWTClaimsSet().getSubject();
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean isExpired = expirationTime != null && expirationTime.before(new Date());

            return (username != null && username.equals(userDetails.getUsername())) && !isExpired;
        } catch (Exception e) {
            return false;
        }
    }

    private JWTClaimsSet extractAllClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(getSecretBytes());
            if (!signedJWT.verify(verifier)) {
                throw new JOSEException("Chữ ký JWT không hợp lệ (Nimbus verification failed)");
            }
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException("Lỗi giải mã/xác thực JWT với Nimbus: " + e.getMessage(), e);
        }
    }

    private byte[] getSecretBytes() {
        return secretKey.getBytes(StandardCharsets.UTF_8);
    }
}
