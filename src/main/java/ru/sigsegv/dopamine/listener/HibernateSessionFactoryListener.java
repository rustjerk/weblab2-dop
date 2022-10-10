package ru.sigsegv.dopamine.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

@WebListener
public class HibernateSessionFactoryListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        var standardRegistry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml")
                .applySetting("hibernate.connection.datasource", "java:/comp/env/jdbc/db")
                .build();
        var metadata = new MetadataSources(standardRegistry).getMetadataBuilder().build();
        var sessionFactory = metadata.getSessionFactoryBuilder().build();

        servletContextEvent.getServletContext().setAttribute("SessionFactory", sessionFactory);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        var sessionFactory = (SessionFactory) servletContextEvent.getServletContext().getAttribute("SessionFactory");
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
