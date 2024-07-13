package server.managers;

import server.managers.user.User;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserManager {

    private static List<User> users = new ArrayList<>();

    public static void addUser(User user) {
        users.add(user);
    }

    public static Optional<User> getUser(String username) {
        return users.stream().filter(user -> user.getUsername().equals(username)).findFirst();
    }

    public static Optional<User> getUser(SocketChannel socketChannel) {
        return users.stream().filter(user -> user.getSocketChannel().equals(socketChannel)).findFirst();
    }

}
