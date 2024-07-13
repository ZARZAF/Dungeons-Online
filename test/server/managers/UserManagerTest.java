package server.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.managers.user.User;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class UserManagerTest {

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void init() throws IOException {
        user1 = User.create("user1", SocketChannel.open());
        user2 = User.create("user2", SocketChannel.open());
        user3 = User.create("user3", SocketChannel.open());
        UserManager.addUser(user1);
        UserManager.addUser(user2);
    }

    @Test
    public void testAddUser() {
        UserManager.addUser(user3);
        assertTrue(UserManager.getUser("user3").isPresent());
    }

    @Test
    public void testGetUserByUsername() {
        Optional<User> foundUser = UserManager.getUser("user1");
        assertTrue(foundUser.isPresent());
        assertEquals(user1.getUsername(), foundUser.get().getUsername());
    }

    @Test
    public void testGetUserBySocketChannel() {
        Optional<User> foundUser = UserManager.getUser(user2.getSocketChannel());
        assertTrue(foundUser.isPresent());
        assertEquals(user2, foundUser.get());
    }

    @Test
    public void testGetUserByUsernameNotFound() {
        Optional<User> foundUser = UserManager.getUser("nonexistent_user");
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testGetUserBySocketChannelNotFound() throws IOException {
        Optional<User> foundUser = UserManager.getUser(SocketChannel.open());
        assertFalse(foundUser.isPresent());
    }
}
