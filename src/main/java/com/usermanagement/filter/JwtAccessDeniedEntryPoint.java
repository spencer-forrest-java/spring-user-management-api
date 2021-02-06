package com.usermanagement.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.HttpResponse;
import com.usermanagement.utility.SecurityConstant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class JwtAccessDeniedEntryPoint extends Http403ForbiddenEntryPoint {

	/**
	 * Always returns a 403 error code to the client.
	 * Implementations should modify the headers on the ServletResponse as necessary to commence
	 * the authentication process.
	 *
	 * @param request  – that resulted in an AuthenticationException
	 * @param response – so that the user agent can begin authentication
	 * @param arg2     – that caused the invocation
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2)
			throws IOException {

		HttpResponse httpResponse = new HttpResponse(HttpStatus.FORBIDDEN.value(),
																								 HttpStatus.FORBIDDEN,
																								 HttpStatus.FORBIDDEN.getReasonPhrase().toUpperCase(),
																								 SecurityConstant.FORBIDDEN_MESSAGE);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpStatus.FORBIDDEN.value());

		// add http response to http servlet response
		OutputStream outputStream = response.getOutputStream();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(outputStream, httpResponse);
		outputStream.flush();
	}
}
