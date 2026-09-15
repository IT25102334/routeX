package com.routex.filter;

import com.routex.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Guards every /rider/*, /driver/* and /admin/* URL: no session -> bounce
 * to the login page. Role mismatch (e.g. a Rider hitting an /admin/* URL)
 * is rejected with 403 inside each servlet, since that decision depends
 * on which role owns that path.
 */
@WebFilter(urlPatterns = {"/rider/*", "/driver/*", "/admin/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("user");

        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        chain.doFilter(request, response);
    }
}
