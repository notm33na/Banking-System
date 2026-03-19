package com.virtbank.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Global XSS filter — strips HTML/script tags from request parameters and headers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@Slf4j
public class XssFilter extends OncePerRequestFilter {

    private static final Pattern SCRIPT_TAG = Pattern.compile(
            "<\\s*script[^>]*>.*?</\\s*script\\s*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Wrap request to sanitise parameters
        filterChain.doFilter(new SanitisedRequest(request), response);
    }

    private static class SanitisedRequest extends HttpServletRequestWrapper {
        SanitisedRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            return sanitise(super.getParameter(name));
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] sanitised = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitised[i] = sanitise(values[i]);
            }
            return sanitised;
        }

        @Override
        public String getHeader(String name) {
            // Don't sanitise Authorization header
            if ("Authorization".equalsIgnoreCase(name)) return super.getHeader(name);
            return sanitise(super.getHeader(name));
        }
    }

    private static String sanitise(String value) {
        if (value == null) return null;
        // Remove script tags
        value = SCRIPT_TAG.matcher(value).replaceAll("");
        // Remove remaining HTML tags
        value = HTML_TAG.matcher(value).replaceAll("");
        // Escape HTML entities
        value = StringEscapeUtils.escapeHtml4(value);
        return value;
    }
}
