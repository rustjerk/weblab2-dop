package ru.sigsegv.dopamine.util;

import jakarta.servlet.ServletContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class DbHelper {
    public static Session openSession(ServletContext context) {
        var sessionFactory = (SessionFactory) context.getAttribute("SessionFactory");
        return sessionFactory.openSession();
    }
}
