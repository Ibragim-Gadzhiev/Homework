package modulethree.util;

import modulethree.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * Утилитный класс для конфигурации и управления Hibernate {@link SessionFactory}.
 *
 * <p>Позволяет установить параметры подключения к базе данных и создать/пересоздать
 * фабрику сессий. Используется в приложении как единая точка доступа к Hibernate.</p>
 */
public class HibernateUtil {
    private static SessionFactory sessionFactory;
    private static String jdbcUrl;
    private static String username;
    private static String password;


    /**
     * Устанавливает параметры подключения к базе данных и пересоздаёт {@link SessionFactory}.
     *
     * @param url  JDBC URL базы данных
     * @param user имя пользователя
     * @param pwd  пароль пользователя
     */
    public static void setConfig(String url, String user, String pwd) {
        jdbcUrl = url;
        username = user;
        password = pwd;
        rebuildSessionFactory();
    }

    /**
     * Пересоздаёт Hibernate {@link SessionFactory} с текущими параметрами подключения.
     *
     * <p>Используется при инициализации или изменении конфигурации подключения.
     * При наличии существующей фабрики она будет закрыта.<p/>
     *
     * @throws RuntimeException если не удалось создать фабрику сессий
     */
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

    /**
     * Возвращает текущую Hibernate {@link SessionFactory}.
     *
     * @return объект {@link SessionFactory}, или {@code null}, если не была инициализирована
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Завершает работу {@link SessionFactory}, освобождая ресурсы.
     *
     * <p>Вызывается при завершении работы приложения.</p>
     */
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}