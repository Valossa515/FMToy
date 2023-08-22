package br.com.felipe.FMToy.security;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import br.com.felipe.FMToy.kafka.KafkaProducerConfig;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Value("${fmtoy.app.jwtSecret}")
	private String jwtSecret;

	@Value("${fmtoy.app.jwtExpirationMs}")
	private int jwtExpirationMs;

	@Autowired
	private KafkaProducerConfig kafkaProducerConfig;
	
	@Autowired
    private InvalidTokenManager invalidTokenManager;

	
	public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();

        // Enviar mensagem ao Kafka quando um token JWT é gerado
        kafkaProducerConfig.sendMessage("Token JWT" + jwt + " gerado para o usuário: " + userPrincipal.getUsername());

        return jwt;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
    	
    	if(invalidTokenManager.isTokenInvalid(authToken)) {
    		logger.error("JWT token is invalid: {}", authToken);
    		kafkaProducerConfig.sendMessage("JWT token inválido: " + authToken);
            return false;
    	}
    	
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            kafkaProducerConfig.sendMessage("JWT token inválido: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            kafkaProducerConfig.sendMessage("JWT token expirado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            kafkaProducerConfig.sendMessage("JWT token não é suportado: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            kafkaProducerConfig.sendMessage("JWT claims string está vazia: " + e.getMessage());
        }

        return false;
    }
    
    public void invalidateToken(String token) {
    	invalidTokenManager.addInvalidToken(token);
    }
}
