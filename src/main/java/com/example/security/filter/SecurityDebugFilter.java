package com.example.security.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Custom filter for detailed security debugging
 */
public class SecurityDebugFilter implements Filter {

	private static final org.slf4j.Logger logger =
		org.slf4j.LoggerFactory.getLogger(SecurityDebugFilter.class);

	@Override
	public void doFilter(
		ServletRequest request,
		ServletResponse response,
		FilterChain chain
	) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (logger.isDebugEnabled()) {
			logRequestDetails(httpRequest);
		}

		long startTime = System.currentTimeMillis();

		try {
			chain.doFilter(request, response);
		} finally {
			if (logger.isDebugEnabled()) {
				long duration = System.currentTimeMillis() - startTime;
				logResponseDetails(httpRequest, httpResponse, duration);
			}
		}
	}

	private void logRequestDetails(HttpServletRequest request) {
		logger.debug("=== SECURITY DEBUG: INCOMING REQUEST ===");
		logger.debug("Method: {}", request.getMethod());
		logger.debug("URI: {}", request.getRequestURI());
		logger.debug("Query: {}", request.getQueryString());
		logger.debug("Remote IP: {}", request.getRemoteAddr());
		logger.debug("Session ID: {}", request.getRequestedSessionId());

		// Log headers
		logger.debug("--- Headers ---");
		request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
			String headerValue = request.getHeader(headerName);
			// Mask sensitive headers
			if (headerName.toLowerCase().contains("authorization") ||
				headerName.toLowerCase().contains("cookie")) {
				headerValue = "***MASKED***";
			}
			logger.debug("{}: {}", headerName, headerValue);
		});

		// Log authentication header specifically
		String authHeader = request.getHeader("X_AUTH_USER");
		if (authHeader != null) {
			logger.debug("🔑 X_AUTH_USER Header Found: {}", authHeader);
		} else {
			logger.debug("❌ No X_AUTH_USER Header Found");
		}

		// Log security context
		org.springframework.security.core.context.SecurityContext securityContext =
			org.springframework.security.core.context.SecurityContextHolder.getContext();
		if (securityContext.getAuthentication() != null) {
			logger.debug("🔐 Current Authentication: {}",
				securityContext.getAuthentication().getClass().getSimpleName());
			logger.debug("👤 Principal: {}", securityContext.getAuthentication().getName());
			logger.debug("🏷️ Authorities: {}", securityContext.getAuthentication().getAuthorities());
			logger.debug("✅ Is Authenticated: {}", securityContext.getAuthentication().isAuthenticated());
		} else {
			logger.debug("❌ No Authentication in SecurityContext");
		}
	}

	private void logResponseDetails(HttpServletRequest request,
		HttpServletResponse response, long duration) {
		logger.debug("=== SECURITY DEBUG: RESPONSE ===");
		logger.debug("Status: {}", response.getStatus());
		logger.debug("Duration: {}ms", duration);

		// Log final security context
		org.springframework.security.core.context.SecurityContext securityContext =
			org.springframework.security.core.context.SecurityContextHolder.getContext();
		if (securityContext.getAuthentication() != null) {
			logger.debug("🔐 Final Authentication: {}",
				securityContext.getAuthentication().getClass().getSimpleName());
			logger.debug("👤 Final Principal: {}", securityContext.getAuthentication().getName());
			logger.debug("✅ Final Is Authenticated: {}", securityContext.getAuthentication().isAuthenticated());
		} else {
			logger.debug("❌ No Final Authentication");
		}
		logger.debug("=====================================");
	}
}
