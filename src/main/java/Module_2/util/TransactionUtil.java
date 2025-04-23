package Module_2.util;

import java.util.function.Consumer;
import java.util.function.Function;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Утилитарный класс для управления транзакциями Hibernate.
 * Предоставляет безопасное выполнение операций в транзакционном контексте.
 */
public class TransactionUtil {
    private static final Logger logger = LoggerFactory.getLogger(TransactionUtil.class);

    /**
     * Выполняет операцию с возвращаемым значением в транзакционном контексте.
     *
     * @param function Функция, принимающая сессию Hibernate и возвращающая результат типа T
     * @param <T> Тип возвращаемого значения
     * @return Результат выполнения функции
     * @throws DatabaseException Если возникает ошибка при работе с базой данных
     *
     * @apiNote Автоматически управляет жизненным циклом транзакции:
     *     1. Открывает сессию
     *     2. Начинает транзакцию
     *     3. Выполняет переданную функцию
     *     4. Коммитит транзакцию при успехе
     *     5. Откатывает транзакцию при ошибке
     */
    public static <T> T doInTransaction(Function<Session, T> function) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            T result = function.apply(session);
            transaction.commit();
            return result;
        } catch (HibernateException e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Transaction rollback failed", rollbackEx);
                }
            }
            logger.error("Transaction error: {}", e.getMessage(), e);
            throw new DatabaseException("Database operation failed", e);
        }
    }

    /**
     * Выполняет операцию без возвращаемого значения в транзакционном контексте.
     *
     * @param consumer Операция, принимающая сессию Hibernate
     *
     * @see #doInTransaction(Function)
     * @implNote Реализация преобразует Consumer в Function, возвращающую null
     */
    public static void doInTransaction(Consumer<Session> consumer) {
        doInTransaction(session -> {
            consumer.accept(session);
            return null;
        });
    }
}