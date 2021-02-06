package com.usermanagement.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.domain.HttpResponse;
import com.usermanagement.utility.SecurityConstant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
	/**
	 * Handles an access denied failure.
	 *
	 * @param request               that resulted in an <code>AccessDeniedException</code>
	 * @param response              so that the user agent can be advised of the failure
	 * @param accessDeniedException that caused the invocation
	 * @throws IOException in the event of an IOException
	 */
	@Override
	public void handle(HttpServletRequest request,
										 HttpServletResponse response,
										 AccessDeniedException accessDeniedException) throws IOException {

		HttpResponse httpResponse = new HttpResponse(HttpStatus.UNAUTHORIZED.value(),
																								 HttpStatus.UNAUTHORIZED,
																								 HttpStatus.UNAUTHORIZED.getReasonPhrase().toLowerCase(),
																								 SecurityConstant.FORBIDDEN_MESSAGE);

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpStatus.UNAUTHORIZED.value());

		// add http response to http servlet response
		OutputStream outputStream = response.getOutputStream();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(outputStream, httpResponse);
		outputStream.flush();
	}
}
