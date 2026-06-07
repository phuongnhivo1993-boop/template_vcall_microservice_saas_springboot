package com.vcall.common.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Aspect
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Around("execution(* org.springframework.data.repository.Repository+.*(..))")
    public Object enableTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        String tenantId = TenantContext.getTenantId();

        if (StringUtils.hasText(tenantId)) {
            try {
                Session session = entityManager.unwrap(Session.class);
                Filter filter = session.enableFilter("tenantFilter");
                filter.setParameter("tenantId", tenantId);
                log.trace("Enabled tenant filter for tenantId: {}", tenantId);
            } catch (Exception e) {
                log.warn("Failed to enable tenant filter: {}", e.getMessage());
            }
        }

        try {
            return joinPoint.proceed();
        } finally {
            try {
                Session session = entityManager.unwrap(Session.class);
                session.disableFilter("tenantFilter");
                log.trace("Disabled tenant filter after repository call");
            } catch (Exception e) {
                log.trace("Failed to disable tenant filter: {}", e.getMessage());
            }
        }
    }
}
