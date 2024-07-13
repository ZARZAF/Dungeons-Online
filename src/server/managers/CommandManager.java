package server.managers;

import game.entities.actors.constants.Direction;
import game.entities.actors.exceptions.InvalidDirectionException;
import game.entities.items.Item;
import logger.Logger;
import server.communication.ClientCommunication;
import server.managers.message.Message;
import server.managers.user.User;

import java.util.Optional;
import java.util.logging.Level;

import static server.managers.Commands.SEPARATOR;
import static server.managers.GameManager.getGame;
import static server.managers.GameManager.getHero;
import static server.managers.GameManager.showLobbies;

public class CommandManager {

    private static final int ACTION_INDEX = 0;
    private static final int ARGUMENT_1_INDEX = 1;
    private static final int ARGUMENT_2_INDEX = 2;

    public static void executeCommand(String command, User actor) {
        String[] commandArguments = command.split(SEPARATOR);
        if (getGame(actor) != null && !getHero(actor).isAlive()) {
            if (commandArguments[ACTION_INDEX].equals(Commands.LEAVE)) {
                leave(actor, commandArguments);
            }
            return;
        }

        switch (commandArguments[ACTION_INDEX]) {
            case Commands.ATTACK -> attack(actor, commandArguments);
            case Commands.MOVE -> move(actor, commandArguments);
            case Commands.EQUIP -> equip(actor, commandArguments);
            case Commands.DROP -> drop(actor, commandArguments);
            case Commands.PICK -> pick(actor);
            case Commands.HOST -> host(actor, commandArguments);
            case Commands.JOIN -> join(actor, commandArguments);
            case Commands.LEAVE -> leave(actor, commandArguments);
            case Commands.START -> start(actor);
            case Commands.SHOW_LOBBIES -> showLobbies(actor);
        }
    }

    private static void attack(User actor, String[] commandArguments) {
        String attacked = commandArguments[ARGUMENT_1_INDEX];
        int indexOfActor = Integer.parseInt(commandArguments[ARGUMENT_2_INDEX]);

        switch (attacked) {
            case Commands.HERO -> GameManager.attack(actor,
                    GameManager.getHero(GameManager.getGame(actor), indexOfActor));
            case Commands.MINION -> GameManager.attack(actor,
                    GameManager.getMinion(GameManager.getGame(actor), indexOfActor));
            default -> ClientCommunication.sendMessage(new Message().setRecipient(actor)
                    .append(Commands.ATTACK)
                    .append(Commands.STATUS_DENIED));
        }
    }

    private static void move(User actor, String[] commandArguments) {
        try {
            Direction direction = Direction.get(commandArguments[ARGUMENT_1_INDEX]);

            GameManager.move(direction, actor);
        } catch (InvalidDirectionException e) {
            System.out.println("Invalid direction of Actor " + actor.getUsername());
            Logger.logError(Logger.error(), Level.SEVERE
                    , "Invalid direction of Actor " + actor.getUsername(), e);
            System.exit(-1);
        }
    }

    private static void equip(User actor, String[] commandArguments) {
        String itemName = commandArguments[ARGUMENT_1_INDEX];
        Item item = GameManager.getItem(itemName, actor);
        GameManager.equip(item, actor);
    }

    private static void drop(User actor, String[] commandArguments) {
        String itemName = commandArguments[ARGUMENT_1_INDEX];
        Item item = GameManager.getItem(itemName, actor);
        GameManager.drop(item, actor);
    }

    private static void pick(User actor) {
        GameManager.pickUp(actor);
    }

    private static void host(User actor, String[] commandArguments) {
        String mapName = commandArguments[ARGUMENT_1_INDEX];
        GameManager.createLobby(actor, mapName);
    }

    private static void join(User actor, String[] commandArguments) {
        String hostUsername = commandArguments[ARGUMENT_1_INDEX];
        Optional<User> host = UserManager.getUser(hostUsername);
        GameManager.joinLobby(actor, host.get());
    }

    private static void leave(User actor, String[] commandArguments) {
        String hostUsername = commandArguments[ARGUMENT_1_INDEX];
        Optional<User> host = UserManager.getUser(hostUsername);
        GameManager.leave(actor, host.get());
    }

    private static void start(User actor) {
        GameManager.startGame(actor);
    }

}
