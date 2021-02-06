package com.usermanagement.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.usermanagement.domain.UserPrincipal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTUtility {

	private final String secret;

	public JWTUtility(@Value("${jwt.secret}") String secret) {
		this.secret = secret;
	}

	public String generateJwtToken(UserPrincipal userPrincipal) {
		String[] claims = getClaimsFromUser(userPrincipal);

		return JWT.create().withIssuer(SecurityConstant.MY_COMPANY)
				.withAudience(SecurityConstant.MY_COMPANY_ADMINISTRATION)
				.withIssuedAt(new Date())
				.withSubject(userPrincipal.getUsername())
				.withArrayClaim(SecurityConstant.AUTHORITIES, claims)
				.withExpiresAt(new Date(System.currentTimeMillis() + SecurityConstant.EXPIRATION_TIME))
				.sign(Algorithm.HMAC512(this.secret.getBytes()));
	}

	public List<GrantedAuthority> getAuthorities(String token) {
		String[] claims = getClaimsFromToken(token);
		return Arrays.stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

	public Authentication getAuthentication(String username,
																					List<GrantedAuthority> authorities,
																					HttpServletRequest request) {

		WebAuthenticationDetails details = new WebAuthenticationDetailsSource().buildDetails(request);
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, null, authorities);
		token.setDetails(details);
		return token;
	}

	public String getSubject(String token) {
		JWTVerifier verifier = getVerifier();
		return verifier.verify(token).getSubject();
	}

	public boolean isValid(String username, String token) {
		JWTVerifier verifier = getVerifier();
		return !isTokenExpired(verifier, token) && StringUtils.isNotEmpty(username);
	}

	private String[] getClaimsFromToken(String token) {
		JWTVerifier verifier = getVerifier();
		return verifier.verify(token).getClaim(SecurityConstant.AUTHORITIES).asArray(String.class);
	}

	private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
		return userPrincipal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
	}

	private JWTVerifier getVerifier() {
		JWTVerifier verifier;
		try {
			Algorithm algorithm = Algorithm.HMAC512(this.secret);
			verifier = JWT.require(algorithm).withIssuer(SecurityConstant.MY_COMPANY).build();
		} catch (JWTVerificationException exception) {
			throw new JWTVerificationException(SecurityConstant.TOKEN_CANNOT_BE_VERIFIED);
		}
		return verifier;
	}

	private boolean isTokenExpired(JWTVerifier verifier, String token) {
		Date expiration = verifier.verify(token).getExpiresAt();
		return expiration.before(new Date());
	}
}
