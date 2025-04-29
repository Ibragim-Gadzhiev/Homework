package modulethree;

import modulethree.dao.UserDao;
import modulethree.model.User;
import modulethree.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class UserServiceTest {
    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllUsers(){
        User user1 = new User();
        user1.setAge(12);
        user1.setName("FirstName");
        user1.setEmail("first.user@email.com");

        User user2 = new User();
        user2.setAge(13);
        user2.setName("SecondName");
        user2.setEmail("second.user@email.com");

        List<User> mockUsers = Arrays.asList(user1, user2);

        when(userDao.readAll()).thenReturn(mockUsers);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("FirstName", result.get(0).getName());
        assertEquals("SecondName", result.get(1).getName());
    }

    @Test
    public void testGetAllUsersReturnEmptyListIfNoUsersNotFound(){
        when(userDao.readAll()).thenReturn(List.of());
        List<User> result = userService.getAllUsers();
        assertEquals(0, result.size());
    }
}