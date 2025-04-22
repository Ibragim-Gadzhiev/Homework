package Module_2;

import Module_2.dao.UserDao;
import Module_2.dao.UserDaoImpl;
import Module_2.model.User;
import Module_2.util.HibernateUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final UserDao userDao = new UserDaoImpl();
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HibernateUtil.shutdown();
            scanner.close();
        }));

        try {
            runApplication();
        } finally {
            scanner.close();
        }
    }

    private static void runApplication() {
        while (true) {
            printMenu();
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "create" -> createUser();
                case "read" -> readUser();
                case "read all" -> readAllUsers();
                case "update" -> updateUser();
                case "delete" -> deleteUser();
                case "exit" -> {
                    System.out.println("Завершение работы...");
                    return;
                }
                default -> System.out.println("Неизвестная команда!");
            }
        }
    }

    private static void printMenu() {
        System.out.print("""
            \n=== Управление пользователями ===
            create     - Создать пользователя
            read       - Найти по ID
            read all   - Все пользователи
            update     - Обновить данные
            delete     - Удалить пользователя
            exit       - Выход
            
            Введите команду:\s""");
    }

    private static void createUser() {
        try {
            User user = new User();
            System.out.print("Введите имя: ");
            user.setName(readNonEmptyInput());

            System.out.print("Введите email: ");
            user.setEmail(readValidEmail());

            System.out.print("Введите возраст: ");
            user.setAge(readValidAge());

            if (userDao.existsByEmail(user.getEmail())) {
                System.out.println("Ошибка: Email уже существует!");
                return;
            }

            userDao.create(user);
            System.out.printf("Пользователь создан! ID: %d%n", user.getId());
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void readUser() {
        try {
            System.out.print("Введите ID пользователя: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            Optional<User> user = userDao.read(id);
            user.ifPresentOrElse(
                    Main::printUser,
                    () -> System.out.println("Пользователь не найден!")
            );

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Некорректный формат ID!");
        }
    }

    private static void readAllUsers() {
        List<User> users = userDao.readAll();
        if (users.isEmpty()) {
            System.out.println("Список пользователей пуст!");
            return;
        }

        System.out.println("\n=== Список пользователей ===");
        users.forEach(Main::printUser);
        System.out.println("=== Всего: " + users.size() + " ===");
    }

    private static void updateUser() {
        try {
            System.out.print("Введите ID пользователя: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            Optional<User> userOpt = userDao.read(id);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь не найден!");
                return;
            }

            User user = userOpt.get();
            if (updateUserFields(user)) {
                boolean isUpdated = userDao.update(user);
                System.out.println(isUpdated
                        ? "Данные обновлены успешно!"
                        : "Ошибка обновления!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Некорректный формат ID!");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static boolean updateUserFields(User user) {
        System.out.print("Введите новое имя [" + user.getName() + "]: ");
        String newName = scanner.nextLine().trim();
        if (!newName.isEmpty()) user.setName(newName);

        System.out.print("Введите новый email [" + user.getEmail() + "]: ");
        String newEmail = scanner.nextLine().trim();
        if (!newEmail.isEmpty()) {
            if (!newEmail.equals(user.getEmail()) && userDao.existsByEmail(newEmail)) {
                System.out.println("Ошибка: Email уже используется другим пользователем!");
                return false;
            }
            user.setEmail(newEmail);
        }

        System.out.print("Введите новый возраст [" + user.getAge() + "]: ");
        String ageInput = scanner.nextLine().trim();
        if (!ageInput.isEmpty()) {
            try {
                int newAge = Integer.parseInt(ageInput);
                if (newAge < 0 || newAge > 150) {
                    System.out.println("Возраст должен быть от 0 до 150!");
                    return false;
                }
                user.setAge(newAge);
            } catch (NumberFormatException e) {
                System.out.println("Некорректный формат возраста!");
                return false;
            }
        }
        return true;
    }


    private static void deleteUser() {
        try {
            System.out.print("Введите ID для удаления: ");
            Long id = Long.parseLong(scanner.nextLine().trim());

            boolean isDeleted = userDao.delete(id);
            System.out.println(isDeleted
                    ? "Пользователь удален!"
                    : "Пользователь не найден!");

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Некорректный формат ID!");
        }
    }

    private static String readNonEmptyInput() {
        while (true) {
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Имя не может быть пустым!");
        }
    }

    private static String readValidEmail() {
        while (true) {
            String email = scanner.nextLine().trim();
            if (email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return email;
            }
            System.out.print("Некорректный email! Повторите: ");
        }
    }

    private static int readValidAge() {
        while (true) {
            try {
                int age = Integer.parseInt(scanner.nextLine().trim());
                if (age >= 0 && age <= 150) return age;
                System.out.print("Возраст должен быть 0-150! Повторите: ");
            } catch (NumberFormatException e) {
                System.out.print("Некорректный формат! Повторите: ");
            }
        }
    }

    private static void printUser(User user) {
        System.out.printf(
                "ID: %-3d | Имя: %-15s | Email: %-20s | Возраст: %3d | Создан: %s%n",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt().format(DATE_FORMATTER)
        );
    }
}