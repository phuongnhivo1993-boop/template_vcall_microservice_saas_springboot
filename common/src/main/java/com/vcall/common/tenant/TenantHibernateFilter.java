package com.vcall.common.tenant;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@ConditionalOnClass(Session.class)
public class TenantHibernateFilter {

    public TenantHibernateFilter(EntityManagerFactory entityManagerFactory) {
        if (entityManagerFactory instanceof SessionFactoryImplementor sessionFactory) {
            EventListenerRegistry registry = sessionFactory.getServiceRegistry()
                    .getService(EventListenerRegistry.class);
            if (registry != null) {
                registry.appendListeners(EventType.PRE_INSERT, new TenantSessionEventListener());
            }
        }
    }
}
