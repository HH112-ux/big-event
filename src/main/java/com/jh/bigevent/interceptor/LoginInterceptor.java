package com.jh.bigevent.interceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jh.bigevent.utils.JwtUtil;
import com.jh.bigevent.utils.Result;
import com.jh.bigevent.utils.ThreadLocalUtil;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            sendError(response, 401, Result.error(2, "未授权：请先登录"));
            return false;
        }

        try {
            DecodedJWT jwt = jwtUtil.parseToken(token);
            ThreadLocalUtil.set("id", jwt.getClaim("id").asLong());
            ThreadLocalUtil.set("username", jwt.getClaim("username").asString());
        } catch (TokenExpiredException e) {
            sendError(response, 401, Result.error(2, "令牌已过期，请重新登录"));
            return false;
        } catch (JWTVerificationException e) {
            sendError(response, 401, Result.error(2, "令牌无效"));
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ThreadLocalUtil.remove();
    }

    private void sendError(HttpServletResponse response, int status, Result<?> result) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
