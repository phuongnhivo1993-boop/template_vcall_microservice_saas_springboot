package com.vcall.common.tenant;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@ConditionalOnClass(Session.class)
public class TenantHibernateFilter {

    public TenantHibernateFilter(EntityManagerFactory entityManagerFactory) {
        if (entityManagerFactory != null) {
            entityManagerFactory.addSessionFactoryObservers(registry -> {
                registry.getSessionFactory().getSessionBuilder()
                        .eventListeners(new TenantSessionEventListener());
            });
        }
    }
}
