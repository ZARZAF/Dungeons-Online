package server.managers;

import game.entities.actors.Actor;
import game.entities.actors.Hero;
import game.entities.actors.Minion;
import game.entities.items.Item;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import server.communication.ClientCommunication;
import server.managers.message.Message;
import server.managers.user.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class CommandManagerTest {

    private User user = User.create("actor", null);
    private Hero hero = mock();
    private Minion minion = mock();
    private Item item = mock();


    @Test
    void testAttackHero() {
        try (MockedStatic<GameManager> classBMockedStatic = mockStatic(GameManager.class)) {
            ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<Actor> heroCaptor = ArgumentCaptor.forClass(Actor.class);
            when(GameManager.getGame(any())).thenReturn(null);
            when(GameManager.getHero(null, 0)).thenReturn(hero);

            CommandManager.executeCommand("a-H-0", user);

            classBMockedStatic.verify(() -> GameManager.attack(actorCaptor.capture(), heroCaptor.capture()));

            assertEquals(user.getUsername(), actorCaptor.getValue().getUsername(), "Gives the user given to GameManager.");
            assertEquals(hero, heroCaptor.getValue(), "Gives the actor given to GameManager.");
        }
    }

    @Test
    void testAttackMinion() {
        try (MockedStatic<GameManager> classBMockedStatic = mockStatic(GameManager.class)) {
            ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<Actor> heroCaptor = ArgumentCaptor.forClass(Actor.class);
            when(GameManager.getGame(any())).thenReturn(null);
            when(GameManager.getMinion(null, 0)).thenReturn(minion);

            CommandManager.executeCommand("a-M-0", user);

            classBMockedStatic.verify(() -> GameManager.attack(actorCaptor.capture(), heroCaptor.capture()));

            assertEquals(user.getUsername(), actorCaptor.getValue().getUsername(), "Gives the user given to GameManager.");
            assertEquals(minion, heroCaptor.getValue(), "Gives the actor given to GameManager.");
        }
    }

    @Test
    void testAttackInvalid() {
        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            CommandManager.executeCommand("a-INVALID-0", user);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("a-denied-", parameterCaptor.getValue().toString(), "Denies the invalid request.");
        }
    }

    @Test
    void testMoveValid() {
        try (MockedStatic<GameManager> classBMockedStatic = mockStatic(GameManager.class)) {
            ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);
            CommandManager.executeCommand("m-u-", user);

            classBMockedStatic.verify(() -> GameManager.move(any(), actorCaptor.capture()));

            assertEquals(user.getUsername(), actorCaptor.getValue().getUsername(), "Gives the user given to GameManager.");
        }
    }

    @Test
    void testEquip() {
        try (MockedStatic<GameManager> classBMockedStatic = mockStatic(GameManager.class)) {
            ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);

            when(GameManager.getItem("item", user)).thenReturn(item);

            CommandManager.executeCommand("e-item", user);


            classBMockedStatic.verify(() -> GameManager.equip(itemCaptor.capture(), actorCaptor.capture()));

            assertEquals(user.getUsername(), actorCaptor.getValue().getUsername(), "Gives the user given to GameManager.");
            assertEquals(item, itemCaptor.getValue(), "Gives the item given to GameManager.");
        }
    }

    @Test
    void testDrop() {
        try (MockedStatic<GameManager> classBMockedStatic = mockStatic(GameManager.class)) {
            ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);

            when(GameManager.getItem("item", user)).thenReturn(item);

            CommandManager.executeCommand("d-item", user);


            classBMockedStatic.verify(() -> GameManager.drop(itemCaptor.capture(), actorCaptor.capture()));

            assertEquals(user.getUsername(), actorCaptor.getValue().getUsername(), "Gives the user given to GameManager.");
            assertEquals(item, itemCaptor.getValue(), "Gives the item given to GameManager.");
        }
    }

    @Test
    void testPick() {
        try (MockedStatic<GameManager> classBMockedStatic = mockStatic(GameManager.class)) {
            ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);

            CommandManager.executeCommand("p", user);

            classBMockedStatic.verify(() -> GameManager.pickUp(actorCaptor.capture()));

            assertEquals(user.getUsername(), actorCaptor.getValue().getUsername(), "Gives the user given to GameManager.");
        }
    }

    @Test
    void testHost() {
        try (MockedStatic<GameManager> classBMockedStatic = mockStatic(GameManager.class)) {
            ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<String> mapCaptor = ArgumentCaptor.forClass(String.class);

            CommandManager.executeCommand("h-map", user);

            classBMockedStatic.verify(() -> GameManager.createLobby(actorCaptor.capture(), mapCaptor.capture()));

            assertEquals(user.getUsername(), actorCaptor.getValue().getUsername(), "Gives the user given to GameManager.");
            assertEquals("map", mapCaptor.getValue(), "Gives the mapName given to GameManager.");
        }
    }

    @Test
    void testJoin() {
        try (MockedStatic<GameManager> classBMockedStatic = mockStatic(GameManager.class)) {
            ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<User> hostCaptor = ArgumentCaptor.forClass(User.class);

            try (MockedStatic<UserManager> mockedUserManager = mockStatic(UserManager.class)) {
                when(UserManager.getUser("host")).thenReturn(Optional.ofNullable(user));

            CommandManager.executeCommand("j-host", user);

            classBMockedStatic.verify(() -> GameManager.joinLobby(actorCaptor.capture(), hostCaptor.capture()));

            assertEquals(user.getUsername(), actorCaptor.getValue().getUsername(), "Gives the user given to GameManager.");
            assertEquals(hostCaptor.getValue().getUsername(), hostCaptor.getValue().getUsername(), "Gives the host given to GameManager.");
            }
        }
    }

    @Test
    void testLeave() {
        try (MockedStatic<GameManager> classBMockedStatic = mockStatic(GameManager.class)) {
            ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<User> hostCaptor = ArgumentCaptor.forClass(User.class);

            try (MockedStatic<UserManager> mockedUserManager = mockStatic(UserManager.class)) {
                when(UserManager.getUser("host")).thenReturn(Optional.ofNullable(user));

                CommandManager.executeCommand("l-host", user);

                classBMockedStatic.verify(() -> GameManager.leave(actorCaptor.capture(), hostCaptor.capture()));

                assertEquals(user.getUsername(), actorCaptor.getValue().getUsername(), "Gives the user given to GameManager.");
                assertEquals(hostCaptor.getValue().getUsername(), hostCaptor.getValue().getUsername(), "Gives the host given to GameManager.");
            }
        }
    }

    @Test
    void testStart() {
        try (MockedStatic<GameManager> classBMockedStatic = mockStatic(GameManager.class)) {
            ArgumentCaptor<User> actorCaptor = ArgumentCaptor.forClass(User.class);

            CommandManager.executeCommand("s", user);

            classBMockedStatic.verify(() -> GameManager.startGame(actorCaptor.capture()));

            assertEquals(user.getUsername(), actorCaptor.getValue().getUsername(), "Gives the user given to GameManager.");
        }
    }

}
