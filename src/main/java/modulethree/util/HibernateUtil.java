package modulethree.util;

import modulethree.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    private static String jdbcUrl;
    private static String username;
    private static String password;

    public static void setConfig(String url, String user, String pwd) {
        jdbcUrl = url;
        username = user;
        password = pwd;
        rebuildSessionFactory();
    }

    private static void rebuildSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }

        try {
            Configuration configuration = new Configuration();
            configuration.setProperty("hibernate.connection.url", jdbcUrl);
            configuration.setProperty("hibernate.connection.username", username);
            configuration.setProperty("hibernate.connection.password", password);
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            configuration.setProperty("hibernate.hbm2ddl.auto", "update");

            configuration.addAnnotatedClass(User.class);

            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            sessionFactory = configuration.buildSessionFactory(registry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to rebuild SessionFactory", e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}