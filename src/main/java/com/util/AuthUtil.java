package com.util;

import com.controller.POI_Controller;
import com.model.enums.AuthRole;
import com.model.user.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuthUtil {
    private final static Logger log = LoggerFactory.getLogger(AuthUtil.class);

    public static SessionUser getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session != null) {
            return (SessionUser) session.getAttribute("user");
        }
        log.warn("Unauthorized access attempt without valid session by {}", request.getRequestURI());
        return null;
    }

    public static SessionUser requireUser(HttpSession session) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        if (sessionUser == null) {
            log.warn("Unauthorized access attempt without valid session");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return sessionUser;
    }

    public static boolean isAdmin(SessionUser user) {
        return user.getRole() == AuthRole.ADMIN;
    }

    public static void requireAdmin(SessionUser user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No session found");
        }
        if (!isAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required!");
        }
    }
}
