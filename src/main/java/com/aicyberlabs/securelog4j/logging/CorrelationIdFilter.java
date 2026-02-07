package com.aicyberlabs.securelog4j.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final SecureRandom RNG = new SecureRandom();
    private static final Base64.Encoder B64 =
            Base64.getUrlEncoder().withoutPadding();

    private static String newCid() {
        byte[] b = new byte[18];
        RNG.nextBytes(b);
        return B64.encodeToString(b);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain)
            throws IOException, ServletException {

        String incoming = req.getHeader("X-Correlation-Id");
        String cid = (incoming != null && incoming.matches("^[A-Za-z0-9_-]{16,128}$"))
                ? incoming
                : newCid();

        // Set header early (critical)
        res.setHeader("X-Correlation-Id", cid);

        MDC.put("cid", cid);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove("cid");
        }
    }
}
