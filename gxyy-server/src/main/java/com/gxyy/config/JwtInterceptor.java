package com.gxyy.config;

import com.gxyy.common.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 公开访问的端点：物品列表、物品详情（仅 GET 请求）
        boolean isPublicGet = "GET".equalsIgnoreCase(method) && (
                path.matches("/api/items/\\d+") ||   // GET /api/items/{id}
                path.equals("/api/items")              // GET /api/items
        );

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                request.setAttribute("userId", jwtUtils.getUserIdFromToken(token));
                request.setAttribute("username", jwtUtils.getUsernameFromToken(token));
                return true;
            }
        }

        // 公开端点：无 token 也放行
        if (isPublicGet) {
            return true;
        }

        // 需要认证的端点：无 token 返回 401
        response.setStatus(401);
        return false;
    }
}
