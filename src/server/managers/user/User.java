package server.managers.user;

import java.nio.channels.SocketChannel;
import java.util.Objects;

public class User {
    private final String username;
    private final SocketChannel socketChannel;
    private int inGameId;

    private User(String username, SocketChannel socketChannel) {
        this.username = username;
        this.socketChannel = socketChannel;
        inGameId = -1;
    }

    public static User create(String username, SocketChannel socketChannel) {
        return new User(username, socketChannel);
    }

    public void setInGameId(int id) {
        inGameId = id;
    }

    public int getInGameId() {
        return inGameId;
    }

    public String getUsername() {
        return username;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
