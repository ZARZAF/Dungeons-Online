package server.managers.message;

import server.managers.Commands;
import server.managers.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Message {
    private List<User> users;
    private final StringBuilder message;

    public Message() {
        users = new ArrayList<>();
        this.message = new StringBuilder();
    }

    public Message setRecipient(User user) {
        this.users.add(user);
        return this;
    }

    public Message setRecipient(List<User> users) {
        this.users.addAll(users);
        return this;
    }

    public Message append(String message) {
        this.message.append(message).append(Commands.SEPARATOR);
        return this;
    }

    public Message append(int message) {
        this.message.append(message).append(Commands.SEPARATOR);
        return this;
    }

    public Message append(double message) {
        this.message.append(message).append(Commands.SEPARATOR);
        return this;
    }

    public void endMessage() {
        this.message.append(Commands.END_OF_MESSAGE);
    }

    public List<User> getRecipient() {
        return users;
    }

    @Override
    public String toString() {
        return message.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return Objects.equals(users, message1.users) &&
                Objects.equals(message, message1.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users, message);
    }
}
