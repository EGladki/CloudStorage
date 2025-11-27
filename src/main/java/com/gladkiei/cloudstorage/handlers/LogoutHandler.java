package com.gladkiei.cloudstorage.handlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LogoutHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        if (authentication == null || !authentication.isAuthenticated()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("""
                    {"message": "Response from unauthorized user"}
                    """);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("""
                    {"message": "Logout successfully"}
                    """);
        }
        response.getWriter().flush();
    }
}
