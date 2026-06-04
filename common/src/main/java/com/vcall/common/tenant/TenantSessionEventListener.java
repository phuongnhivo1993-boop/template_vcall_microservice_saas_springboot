package com.vcall.common.tenant;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.internal.SessionFactoryImpl;

@Slf4j
public class TenantSessionEventListener implements PreInsertEventListener {

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Object entity = event.getEntity();
        try {
            var method = entity.getClass().getMethod("getTenantId");
            Object tenantId = method.invoke(entity);
            if (tenantId == null) {
                var setter = entity.getClass().getMethod("setTenantId", String.class);
                setter.invoke(entity, TenantContext.getTenantId());
            }
        } catch (Exception e) {
            log.trace("Entity {} does not have tenantId field", entity.getClass().getSimpleName());
        }
        return false;
    }
}
