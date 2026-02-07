package com.aicyberlabs.securelog4j.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class CorrelationIdFilter implements Filter {

    private static final SecureRandom RNG = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    private static String newCid() {
        byte[] b = new byte[18];
        RNG.nextBytes(b);
        return B64.encodeToString(b);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest r = (HttpServletRequest) req;

        String incoming = r.getHeader("X-Correlation-Id");
        String cid = (incoming != null && incoming.matches("^[A-Za-z0-9_-]{16,128}$"))
                ? incoming
                : newCid();

        MDC.put("cid", cid);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove("cid");
        }
    }
}