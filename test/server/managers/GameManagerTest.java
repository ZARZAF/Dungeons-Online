package server.managers;


import game.Entities;
import game.entities.Treasure;
import game.entities.actors.Hero;
import game.entities.actors.Minion;
import game.entities.actors.constants.Direction;
import game.entities.items.Item;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import server.communication.ClientCommunication;
import server.managers.message.Message;
import server.managers.user.User;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

public class GameManagerTest {

    @Test
    void testCreateLobbyNewLobby() {
        User user = User.create("newHost", null);

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.createLobby(user, "maze.txt");

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("h-ok-", parameterCaptor.getValue().toString()
                    , "Testing creating new lobby with host that doesn't host any other lobby");

        }
    }

    @Test
    void testCreateLobbyHostAlreadyHosting() {
        User user = User.create("DoubleHost", null);

        GameManager.createLobby(user, "maze.txt");

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.createLobby(user, "maze.txt");

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("h-denied-", parameterCaptor.getValue().toString()
                    , "Testing creating a lobby with host that already hosts other lobby");

        }
    }

    @Test
    void testStartGameExistingLobby() {
        User user = User.create("startGameHost", null);
        GameManager.createLobby(user, "maze.txt");

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.startGame(user);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            String[] commands = parameterCaptor.getValue().toString().split("-");

            assertEquals("s", commands[0], "Start");
            try {
                int x = Integer.parseInt(commands[1]); // x coordinate Hero
                int y = Integer.parseInt(commands[2]); // y coordinate Hero
            } catch (NumberFormatException e) {
                fail();
            }
            assertEquals("M", commands[3], "Minion");
            try {
                int x = Integer.parseInt(commands[4]); // x coordinate Hero
                int y = Integer.parseInt(commands[5]); // y coordinate Hero
            } catch (NumberFormatException e) {
                fail();
            }
            assertEquals("t", commands[6], "Treasure");
            try {
                int x = Integer.parseInt(commands[7]); // x coordinate Hero
                int y = Integer.parseInt(commands[8]); // y coordinate Hero
            } catch (NumberFormatException e) {
                fail();
            }

            for (int i = 9; i < commands.length - 1; i++) {
                int finalI = i;
                assertTrue(Entities.getItems().stream().anyMatch(item -> item.getName().equals(commands[finalI]))
                        , "Item names in the treasure");
            }

            assertEquals("t", commands[6], "Treasure");
        }
    }

    @Test
    void testStartGameNonExistentLobby() {
        User user = User.create("non-existentHost", null);
        //GameManager.createLobby(user, "maze.txt"); // non-existent lobby

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.startGame(user);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("s-denied-", parameterCaptor.getValue().toString()
                    , "Testing denying request to start game if the lobby doesnt exist");
        }
    }

    @Test
    @Order(1)
    void testShowLobbyInfo() {
        User host1 = User.create("showLobbyHost1", null);
        User host2 = User.create("showLobbyHost2", null);
        User requester = User.create("requester", null);
        GameManager.createLobby(host1, "maze.txt");
        GameManager.createLobby(host2, "maze.txt");

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.showLobbies(requester);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()), times(2));

            List<Message> capturedParameters = parameterCaptor.getAllValues();
            assertEquals("sl-maze.txt-showLobbyHost2-", capturedParameters.get(0).toString()
                    , "Lobby 2");
            assertEquals("sl-maze.txt-showLobbyHost1-", capturedParameters.get(1).toString()
                    , "Lobby 1");

            // from newest to oldest
        }
    }

    @Test
    void testJoinLobbyLobbyExists() {
        User host = User.create("joinHost", null);
        User joiner = User.create("joiner", null);

        GameManager.createLobby(host, "maze.txt");

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.joinLobby(joiner, host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals(2, parameterCaptor.getValue().getRecipient().size());
            assertEquals("j-joiner-", parameterCaptor.getValue().toString()
                    , "Join successful, sent to all Users in lobby");
        }
    }

    @Test
    void testJoinLobbyLobbyNonExistent() {
        User host = User.create("fakeJoinHost", null);
        User joiner = User.create("fakeJoiner", null);

        // GameManager.createLobby(host, "maze.txt"); // lobby non-existent

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.joinLobby(joiner, host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals(1, parameterCaptor.getValue().getRecipient().size());
            assertEquals("j-denied-", parameterCaptor.getValue().toString()
                    , "Join denied, sent only to requester.");
        }
    }

    @Test
    void testAttackSuccessful() {
        User host = User.create("SuccessAttacker", null);
        User joiner = User.create("SuccessAttacked", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.joinLobby(joiner, host);
        GameManager.startGame(host);
        GameManager.getGame(host).getHeroes().get(0).getPosition().x = 1; //setting them at SAME position
        GameManager.getGame(host).getHeroes().get(0).getPosition().y = 1; //setting them at SAME position
        GameManager.getGame(host).getHeroes().get(1).getPosition().x = 1; //setting them at SAME position
        GameManager.getGame(host).getHeroes().get(1).getPosition().y = 1; //setting them at SAME position

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.attack(joiner, GameManager.getHero(host));

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals(2, parameterCaptor.getValue().getRecipient().size());
            assertEquals("a-H-0-80.0-", parameterCaptor.getValue().toString()
                    , "Attack successful, sent to all players in game.");
        }
    }

    @Test
    void testAttackFailDisposition() {
        User host = User.create("FailDispositionAttacker", null);
        User joiner = User.create("FailDispositionAttacked", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.joinLobby(joiner, host);
        GameManager.startGame(host);
        GameManager.getGame(host).getHeroes().get(0).getPosition().x = 1; //setting them at DIFFERENT position
        GameManager.getGame(host).getHeroes().get(0).getPosition().y = 1; //setting them at DIFFERENT position
        GameManager.getGame(host).getHeroes().get(1).getPosition().x = 2; //setting them at DIFFERENT position
        GameManager.getGame(host).getHeroes().get(1).getPosition().y = 2; //setting them at DIFFERENT position

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.attack(joiner, GameManager.getHero(host));

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals(1, parameterCaptor.getValue().getRecipient().size());
            assertEquals("a-denied-", parameterCaptor.getValue().toString()
                    , "Attack denied, sent to attacker.");
        }
    }

    @Test
    void testAttackKillingHeroAndLevelingUp() {
        User host = User.create("KillingHeroAttacker", null);
        User joiner = User.create("KillingHeroAttacked", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.joinLobby(joiner, host);
        GameManager.startGame(host);
        GameManager.getGame(host).getHeroes().get(0).getPosition().x = 1; //setting them at SAME position
        GameManager.getGame(host).getHeroes().get(0).getPosition().y = 1; //setting them at SAME position
        GameManager.getGame(host).getHeroes().get(1).getPosition().x = 1; //setting them at SAME position
        GameManager.getGame(host).getHeroes().get(1).getPosition().y = 1; //setting them at SAME position

        GameManager.getGame(host).getHeroes().get(0).damage(120); // leaving attacked at one blow

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.attack(joiner, GameManager.getHero(host));

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()), times(2));

            assertEquals(2, parameterCaptor.getValue().getRecipient().size());
            List<Message> capturedParameters = parameterCaptor.getAllValues();
            assertEquals("a-H-0-0.0-", capturedParameters.get(0).toString()
                    , "Sending death news of the host");
            assertEquals("lvl-H-1-1-0-", capturedParameters.get(1).toString()
                    , "Level up after killing host");

        }
    }

    @Test
    void testAttackMinion() {
        User host = User.create("AttackingMinionAttacker", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);
        GameManager.getGame(host).getHeroes().get(0).getPosition().x = 1; //setting them at SAME position
        GameManager.getGame(host).getHeroes().get(0).getPosition().y = 1; //setting them at SAME position
        Minion minion = GameManager.getGame(host).getMinions().get(0);
        minion.getPosition().x = 1; //setting them at SAME position
        minion.getPosition().y = 1; //setting them at SAME position

        if (minion.getLevel() == 1) {
            minion.levelUp();
        }

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.attack(host, GameManager.getGame(host).getMinions().get(0));

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()), times(2));

            List<Message> capturedParameters = parameterCaptor.getAllValues();
            assertEquals("a-M-0-88.0-", capturedParameters.get(0).toString()
                    , "Sending hp status of the minion");
            assertEquals("a-H-0-70.0-", capturedParameters.get(1).toString()
                    , "Sending hp status of the attacker");

        }
    }

    @Test
    void testAttackKillingMinionAndLevelingUp() {
        User host = User.create("KillingMinionAttacker", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);
        GameManager.getGame(host).getHeroes().get(0).getPosition().x = 1; //setting them at SAME position
        GameManager.getGame(host).getHeroes().get(0).getPosition().y = 1; //setting them at SAME position
        Minion minion = GameManager.getGame(host).getMinions().get(0);
        minion.getPosition().x = 1; //setting them at SAME position
        minion.getPosition().y = 1; //setting them at SAME position

        if (minion.getLevel() == 1) {
            minion.levelUp();
        }
        minion.damage(110); // leaving attacked at one blow

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.attack(host, minion);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()), times(3));

            List<Message> capturedParameters = parameterCaptor.getAllValues();
            assertEquals("a-M-0-0.0-", capturedParameters.get(0).toString()
                    , "Sending death news of the minion");
            assertEquals("lvl-H-0-2-200-", capturedParameters.get(1).toString()
                    , "Level up after killing minion");
            String[] respawnCommands = capturedParameters.get(2).toString().split("-");
            assertEquals("r", respawnCommands[0]
                    , "Sending respawn information for hte minion");

        }
    }

    @Test
    void testLeaveHost() {
        User host = User.create("LeaverHost", null);

        GameManager.createLobby(host, "maze.txt");

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.leave(host, host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("l-LeaverHost-LeaverHost-", parameterCaptor.getValue().toString()
                    , "Sending leave message");
        }
    }

    @Test
    void testLeaveJoiner() {
        User host = User.create("leaveJoinerHost", null);
        User joiner = User.create("leaveJoiner", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.joinLobby(joiner, host);

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.leave(joiner, host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("l-leaveJoinerHost-leaveJoiner-", parameterCaptor.getValue().toString()
                    , "Sending leave message");
        }
    }

    @Test
    void testMoveDenied() {
        User host = User.create("moveDeniedHost", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);

        GameManager.getGame(host).getHeroes().get(0).getPosition().x = 1; //setting them at bottom next to wall | o
        GameManager.getGame(host).getHeroes().get(0).getPosition().y = 1; //setting them at bottom next to wall |__

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.move(Direction.DOWN, host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("m-denied-", parameterCaptor.getValue().toString()
                    , "Denying move because it it invalid");
        }
    }

    @Test
    void testMoveApproved() {
        User host = User.create("moveApprovedHost", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);

        GameManager.getGame(host).getHeroes().get(0).getPosition().x = 1; //setting them at bottom next to wall | o
        GameManager.getGame(host).getHeroes().get(0).getPosition().y = 1; //setting them at bottom next to wall |__

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.move(Direction.UP, host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("m-0-u-", parameterCaptor.getValue().toString()
                    , "Successful move index of host 0 direction up");
        }
    }

    @Test
    void testGetItemNull() {
        User host = User.create("getNullItemHost", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);

        assertNull(GameManager.getItem("non-existent item", host)
                , "Returning null because item not in backpack");
    }

    @Test
    void testGetItem() {
        User host = User.create("getItemHost", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);

        Item item = Entities.getItems().getFirst();
        GameManager.getGame(host).getHeroes().get(0).pickUp(item);

        assertEquals(item.getName(), GameManager.getItem(item.getName(), host).getName()
                , "Successful getting item from user backpack");

    }

    @Test
    void testEquipWhenUnequipped() {
        User host = User.create("equipWhenUnequipped", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);
        Item item = Entities.getItems().getFirst();
        GameManager.getGame(host).getHeroes().get(0).pickUp(item);
        while (GameManager.getGame(host).getHeroes().get(0).getLevel() < item.getLevel()) {
            GameManager.getGame(host).getHeroes().get(0).levelUp();
        }

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.equip(item, host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("e-ok-", parameterCaptor.getValue().toString()
                    , "Successful equip");
        }
    }

    @Test
    void testEquipWhenEquipped() {
        User host = User.create("equipWhenEquipped", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);
        Item item = Entities.getItems().getFirst();
        GameManager.getGame(host).getHeroes().get(0).pickUp(item);
        while (GameManager.getGame(host).getHeroes().get(0).getLevel() < item.getLevel()) {
            GameManager.getGame(host).getHeroes().get(0).levelUp();
        }
        GameManager.equip(item, host);

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.equip(item, host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("e-ok-", parameterCaptor.getValue().toString()
                    , "Successful equip");
        }
    }

    @Test
    void testEquipWhenItemNotInInventory() {
        User host = User.create("equipWhenItemNotInInventory", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);
        Item item = Entities.getItems().getFirst();
        //GameManager.getGame(host).getHeroes().get(0).pickUp(item);
        //while (GameManager.getGame(host).getHeroes().get(0).getLevel() < item.getLevel()) {
        //    GameManager.getGame(host).getHeroes().get(0).levelUp();
        //}
        //GameManager.equip(item, host);

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.equip(item, host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("e-denied-", parameterCaptor.getValue().toString()
                    , "Denied equip. Item not in inventory");
        }
    }

    @Test
    void testEquipWhenItemLevelHigher() {
        User host = User.create("equipWhenLevelHigher", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);
        Item item = Entities.getItems().getFirst();
        GameManager.getGame(host).getHeroes().get(0).pickUp(item);
        //while (GameManager.getGame(host).getHeroes().get(0).getLevel() < item.getLevel()) {
        //    GameManager.getGame(host).getHeroes().get(0).levelUp();
        //}

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.equip(item, host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("e-denied-", parameterCaptor.getValue().toString()
                    , "Denied equip. Item level " + item.getLevel()
                            + " higher than player's level " + GameManager.getGame(host).getHeroes().get(0).getLevel());
        }
    }

    @Test
    void testPickUpNoTreasureAtPlayerPosition() {
        User host = User.create("pickUpNoTreasureAtPlayerPosition", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.pickUp(host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()), times(0));
        }
    }

    @Test
    void testPickUpNonEmptyTreasure() {
        User host = User.create("pickUpNonEmptyTreasure", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);
        Item item1 = Entities.getItems().get(0);
        Item item2 = Entities.getItems().get(1);
        Hero player = GameManager.getGame(host).getHeroes().get(0);
        GameManager.getGame(host).addTreasure(Treasure.create(player.getPosition(), new ArrayList<>(List.of(item1, item2)), 0));

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.pickUp(host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()), times(4));

            List<Message> capturedParameters = parameterCaptor.getAllValues();
            assertEquals("lvl-H-0-1-0-", capturedParameters.get(0).toString()
                    , "Getting experience from the treasure");
            assertEquals("p-" + item1.getName() + "-", capturedParameters.get(1).toString()
                    , "Picking up item1");
            assertEquals("p-" + item2.getName() + "-", capturedParameters.get(2).toString()
                    , "Picking up item2");
            assertEquals("t-denied-" + player.getPosition().x + "-" + player.getPosition().y + "-", capturedParameters.get(3).toString()
                    , "Removing treasure from map");
        }
    }

    @Test
    void testDropItemFromInvenotory() {
        User host = User.create("dropItemFromInventory", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);
        Hero player = GameManager.getGame(host).getHeroes().get(0);
        Item item = Entities.getItems().getFirst();
        player.pickUp(item);

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.drop(item, host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()), times(2));

            List<Message> capturedParameters = parameterCaptor.getAllValues();
            assertEquals("t-ok-" + player.getPosition().x + "-" + player.getPosition().y + "-Crystalys-t-", capturedParameters.get(0).toString()
                    , "Dropping the treasure containting the dropped item");
            assertEquals("d-ok-", capturedParameters.get(1).toString()
                    , "Successful drop");
        }
    }

    @Test
    void testDropItemDenyNotInInventory() {
        User host = User.create("dropItemDenyNotInInventory", null);

        GameManager.createLobby(host, "maze.txt");
        GameManager.startGame(host);
        Item item = Entities.getItems().getFirst();
        //player.pickUp(item); // item not in inventory

        try (MockedStatic<ClientCommunication> classBMockedStatic = mockStatic(ClientCommunication.class)) {
            ArgumentCaptor<Message> parameterCaptor = ArgumentCaptor.forClass(Message.class);

            GameManager.drop(item, host);

            classBMockedStatic.verify(() -> ClientCommunication.sendMessage(parameterCaptor.capture()));

            assertEquals("d-denied-", parameterCaptor.getValue().toString()
                    , "Denied drop. Item not in inventory");
        }
    }

}
