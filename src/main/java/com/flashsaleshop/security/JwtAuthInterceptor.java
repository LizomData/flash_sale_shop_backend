package com.flashsaleshop.security;

import com.flashsaleshop.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/api/auth")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        String token = authHeader.replace("Bearer", "").trim();
        try {
            Long userId = jwtUtil.parseUserId(token);
            request.setAttribute("userId", userId);
            return true;
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "登录已失效，请重新登录");
        }
    }
}
