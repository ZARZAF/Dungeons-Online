package server.managers.lobby;

import server.managers.user.User;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private final List<User> users;
    private final String mapName;

    public Lobby(User host, String mapName) {
        users = new ArrayList<>(List.of(host));
        this.mapName = mapName;
    }

    public void addPlayer(User player) {
        users.add(player);
    }

    public void removePlayer(User player) {
        users.remove(player);
    }

    public List<User> getUsers() {
        return users;
    }

    public String getMapName() {
        return mapName;
    }
}
