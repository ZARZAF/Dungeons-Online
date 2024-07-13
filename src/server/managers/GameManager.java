package server.managers;

import game.Game;
import game.ValidateAction;
import game.entities.Position;
import game.entities.Treasure;
import game.entities.actors.Actor;
import game.entities.actors.Hero;
import game.entities.actors.Minion;
import game.entities.actors.constants.Direction;
import game.entities.items.Item;
import logger.Logger;
import server.communication.ClientCommunication;
import server.managers.lobby.Lobby;
import server.managers.message.Message;
import server.managers.user.User;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class GameManager {

    private static final Map<User, Game> GAMES = new HashMap<>();
    private static final Map<Game, List<User>> USERS = new HashMap<>();
    private static final Map<User, Lobby> LOBBIES = new HashMap<>();
    private static final Integer MAX_PLAYER_COUNT = 9;

    public static void startGame(User host) {
        Lobby lobby = LOBBIES.get(host);
        if (lobby != null) {
            try {
                Game newGame = Game.create(lobby.getMapName(), lobby.getUsers().size());
                USERS.put(newGame, lobby.getUsers());

                int id = 0;
                for (User u : lobby.getUsers()) {
                    GAMES.put(u, newGame);
                    u.setInGameId(id++);
                }

                sendGameData(newGame);
            } catch (FileNotFoundException e) {
                System.out.println("Cannot fine map file.");
                Logger.logError(Logger.error(), Level.SEVERE
                        , "Cannot fine map file.", e);
                ClientCommunication.sendMessage(new Message().setRecipient(host)
                        .append(Commands.START)
                        .append(Commands.STATUS_DENIED));
            }
        } else {
            ClientCommunication.sendMessage(new Message().setRecipient(host)
                    .append(Commands.START)
                    .append(Commands.STATUS_DENIED));
        }
    }

    private static void sendGameData(Game game) {
        Message gameInfo = new Message();
        gameInfo.setRecipient(USERS.get(game)).append(Commands.START);

        for (Hero h : game.getHeroes()) {
            gameInfo.append(h.getPosition().x).append(h.getPosition().y);
        }

        gameInfo.append(Commands.MINION);
        for (Minion m : game.getMinions()) {
            gameInfo.append(m.getPosition().x).append(m.getPosition().y);
        }

        gameInfo.append(Commands.TREASURE);
        for (Treasure t : game.getTreasures()) {
            gameInfo.append(t.position().x).append(t.position().y);
            for (Item i : t.items()) {
                gameInfo.append(i.getName());
            }
            gameInfo.append(Commands.TREASURE);
        }

        ClientCommunication.sendMessage(gameInfo);
    }

    public static void createLobby(User host, String mapName) {
        if (LOBBIES.get(host) != null) {
            ClientCommunication.sendMessage(new Message().setRecipient(host)
                    .append(Commands.HOST)
                    .append(Commands.STATUS_DENIED));
        } else {
            LOBBIES.put(host, new Lobby(host, mapName));
            ClientCommunication.sendMessage(new Message().setRecipient(host)
                    .append(Commands.HOST)
                    .append(Commands.STATUS_OK));
        }
    }

    private static void sendLobbyInfo(Lobby lobby, User requester) {
        Message lobbyInfo = new Message();
        lobbyInfo.setRecipient(requester)
                .append(Commands.SHOW_LOBBIES)
                .append(lobby.getMapName());

        for (User u : lobby.getUsers()) {
            lobbyInfo.append(u.getUsername());
        }

        ClientCommunication.sendMessage(lobbyInfo);
    }

    public static void showLobbies(User requester) {
        for (Lobby l : LOBBIES.values()) {
            if (getGame(l.getUsers().getFirst()) == null) {
                sendLobbyInfo(l, requester);
            }
        }
    }

    public static void joinLobby(User player, User host) {
        if (LOBBIES.get(host) == null || LOBBIES.get(host).getUsers().size() == MAX_PLAYER_COUNT) {
            ClientCommunication.sendMessage(new Message().setRecipient(player)
                    .append(Commands.JOIN)
                    .append(Commands.STATUS_DENIED));
        } else {
            LOBBIES.get(host).addPlayer(player);
            ClientCommunication.sendMessage(new Message().setRecipient(LOBBIES.get(host).getUsers())
                    .append(Commands.JOIN)
                    .append(player.getUsername()));
        }
    }

    public static void leave(User player, User host) {
        ClientCommunication.sendMessage(new Message().setRecipient(LOBBIES.get(host).getUsers())
                .append(Commands.LEAVE)
                .append(host.getUsername())
                .append(player.getUsername()));
        LOBBIES.get(host).removePlayer(player);

        if (player.equals(host)) {
            LOBBIES.remove(player);
        }
    }

    public static void attack(User user, Actor attacked) {
        Actor attacker = getHero(user);

        if (ValidateAction.attack(attacker, attacked)) {
            executeAttack(user, attacker, attacked);

            if (!attacked.isAlive()) {
                handleDeadAttacked(user, attacker, attacked);
            } else if (attacked instanceof Minion minion) {
                executeAttack(user, minion, attacker);
                if (!attacker.isAlive()) {
                    handleDeadAttacked(user, minion, attacker);
                }
            }
        } else {
            ClientCommunication.sendMessage(new Message().setRecipient(user)
                    .append(Commands.ATTACK)
                    .append(Commands.STATUS_DENIED));
        }

    }

    private static void executeAttack(User user, Actor attacker, Actor attacked) {
        Message response = new Message();
        attacked.damage(attacker.attack());

        Game game = getGame(user);
        response.setRecipient(getUsers(game));

        if (attacked instanceof Hero hero) {
            response.append(Commands.ATTACK)
                    .append(Commands.HERO)
                    .append(game.getHeroes().indexOf(hero));
        } else if (attacked instanceof Minion minion) {
            response.append(Commands.ATTACK)
                    .append(Commands.MINION)
                    .append(game.getMinions().indexOf(minion));
        } else {
            System.out.println("Unknown Actor class given.");
            Logger.logError(Logger.error(), Level.SEVERE
                    , "Unknown Actor class given." + user.getUsername());
        }

        response.append(attacked.getHp());

        ClientCommunication.sendMessage(response);
    }

    private static void handleDeadAttacked(User user, Actor attacker, Actor attacked) {
        Game game = getGame(user);

        addXp(game, attacker, attacked.dropXp());

        if (attacked instanceof Hero attackedHero) {
            attackDeadHero(game, attackedHero);
        } else if (attacked instanceof Minion attackedMinion) {
            attackDeadMinion(game, attackedMinion);
        } else {
            System.out.println("Unknown Actor class given.");
            Logger.logError(Logger.error(), Level.SEVERE, "Invalid instance Actor " + user.getUsername());
        }
    }

    private static void attackDeadHero(Game game, Hero attackedHero) {
        if (ValidateAction.drop(attackedHero.getEquippedItem(), attackedHero)) {
            Item droppedItem = attackedHero.dropItem(attackedHero.getEquippedItem());
            drop(droppedItem, getUsers(game).get(game.getHeroes().indexOf(attackedHero)));
            createTreasure(attackedHero.getPosition(), game, List.of(droppedItem));
        }
    }

    private static void attackDeadMinion(Game game, Minion attackedMinion) {
        Position respawnMinionPosition = game.getRandomMapCoordinates();
        attackedMinion.respawn(respawnMinionPosition);

        ClientCommunication.sendMessage(new Message().setRecipient(getUsers(game))
                .append(Commands.RESPAWN)
                .append(game.getMinions().indexOf(attackedMinion))
                .append(respawnMinionPosition.x)
                .append((respawnMinionPosition.y)));
    }

    public static void move(Direction direction, User user) {
        Game game = getGame(user);
        Hero actor = getHero(user);
        if (ValidateAction.move(direction, actor, game)) {
            actor.move(direction);
            ClientCommunication.sendMessage(new Message().setRecipient(getUsers(game))
                    .append(Commands.MOVE)
                    .append(user.getInGameId())
                    .append(direction.toString()));
        } else {
            ClientCommunication.sendMessage(new Message().setRecipient(user)
                    .append(Commands.MOVE)
                    .append(Commands.STATUS_DENIED));
        }
    }

    private static void addXp(Game game, Actor actor, int xp) {
        actor.addXp(xp);

        if (actor instanceof Hero hero) {
            ClientCommunication.sendMessage(new Message().setRecipient(getUsers(game))
                    .append(Commands.LEVEL)
                    .append(Commands.HERO)
                    .append(game.getHeroes().indexOf(hero))
                    .append(actor.getLevel())
                    .append(xp));
        } else if (actor instanceof Minion minion) {
            ClientCommunication.sendMessage(new Message().setRecipient(getUsers(game))
                    .append(Commands.LEVEL)
                    .append(Commands.MINION)
                    .append(game.getMinions().indexOf(minion))
                    .append(actor.getLevel()));
        }
    }

    public static Item getItem(String itemName, User actor) {
        Optional<Item> item = getHero(actor).getBackpack().stream()
                .filter(i -> i.getName().equals(itemName))
                .findFirst();

        return item.orElse(null);
    }

    public static void equip(Item item, User user) {
        Hero actor = getHero(user);
        if (ValidateAction.equip(item, actor)) {
            if (actor.getEquippedItem() == null) {
                actor.equipItem(item);
            } else if (actor.getEquippedItem().equals(item)) {
                actor.equipItem(null);
            } else {
                actor.equipItem(item);
            }
            ClientCommunication.sendMessage(new Message().setRecipient(user)
                    .append(Commands.EQUIP)
                    .append(Commands.STATUS_OK));
        } else {
            ClientCommunication.sendMessage(new Message().setRecipient(user)
                    .append(Commands.EQUIP)
                    .append(Commands.STATUS_DENIED));
        }
    }

    public static void pickUp(User user) {
        Game game = getGame(user);
        Hero actor = getHero(user);
        List<Treasure> treasures = game.getTreasures(actor.getPosition());

        if (treasures.isEmpty()) {
            return;
        }
        Treasure treasure = treasures.getFirst();
        addXp(game, actor, treasure.xp());

        for (Item i : treasure.items()) {
            if (actor.getBackpackItemsCount() <= actor.getBackpackCapacity()) {
                pickUp(i, actor, user);
            } else {
                removeTreasure(game, treasure);
                createTreasure(treasure.position(), game,
                        treasure.items().subList(treasure.items().indexOf(i), treasure.items().size()));
                return;
            }
        }

        removeTreasure(game, treasure);
    }

    private static void pickUp(Item item, Hero hero, User user) {
        hero.pickUp(item);
        ClientCommunication.sendMessage(new Message().setRecipient(user)
                .append(Commands.PICK)
                .append(item.getName()));
    }

    private static void removeTreasure(Game game, Treasure treasure) {
        ClientCommunication.sendMessage(new Message().setRecipient(getUsers(game))
                .append(Commands.TREASURE)
                .append(Commands.STATUS_DENIED)
                .append(treasure.position().x)
                .append(treasure.position().y));
        game.removeTreasure(treasure);
    }

    public static void drop(Item item, User user) {
        Game game = getGame(user);
        Hero actor = getHero(user);
        if (ValidateAction.drop(item, actor)) {
            Item droppedItem = actor.dropItem(item);
            createTreasure(getHero(user).getPosition(), game, List.of(droppedItem));
            ClientCommunication.sendMessage(new Message().setRecipient(user)
                    .append(Commands.DROP)
                    .append(Commands.STATUS_OK));
        } else {
            ClientCommunication.sendMessage(new Message().setRecipient(user)
                    .append(Commands.DROP)
                    .append(Commands.STATUS_DENIED));
        }
    }

    private static void createTreasure(Position position, Game game, List<Item> items) {
        Treasure newTreasure = Treasure.create(position, items, Treasure.emptyXp());
        game.addTreasure(newTreasure);

        Message response = new Message().setRecipient(getUsers(game))
                .append(Commands.TREASURE)
                .append(Commands.STATUS_OK)
                .append(newTreasure.position().x)
                .append(newTreasure.position().y);

        for (Item i : newTreasure.items()) {
            response.append(i.getName());
        }

        response.append(Commands.TREASURE);

        ClientCommunication.sendMessage(response);
    }

    public static Hero getHero(User user) {
        Game gameOfUser = getGame(user);
        return gameOfUser.getHeroes().get(USERS.get(gameOfUser).indexOf(user));
    }

    public static Game getGame(User user) {
        return GAMES.get(user);
    }

    public static List<User> getUsers(Game game) {
        return USERS.get(game);
    }

    public static Actor getMinion(Game game, int index) {
        return game.getMinions().get(index);
    }

    public static Actor getHero(Game game, int index) {
        return game.getHeroes().get(index);
    }

}
