package Module_2.util;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

public class TransactionUtil {
    private static final Logger logger = LoggerFactory.getLogger(TransactionUtil.class);

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

    public static void doInTransaction(Consumer<Session> consumer) {
        doInTransaction(session -> {
            consumer.accept(session);
            return null;
        });
    }
}