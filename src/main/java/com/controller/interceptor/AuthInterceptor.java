package com.controller.interceptor;

import com.model.user.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/users/login"
    );
    private final static Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        //Preflight Cors
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        String path = request.getRequestURI();

        //Public Endpoint
        for (String publicPath : PUBLIC_PATHS) {
            if (matcher.match(publicPath, path)) {
                return true;
            }
        }

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            log.warn("Unauthorized access to {} from {} (User Agent: {})", request.getRequestURI(), request.getRemoteAddr(),  request.getHeader("User-Agent"));
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "no active session found"
            );
        }

        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        request.setAttribute("user", sessionUser);
        return true;
    }
}
