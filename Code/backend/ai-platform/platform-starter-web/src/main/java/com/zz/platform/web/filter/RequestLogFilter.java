package com.zz.platform.web.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class RequestLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.info("HTTP request finished, method={}, uri={}, status={}, costMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    cost);
        }
    }
}
