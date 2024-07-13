package game.entities;

import game.entities.items.Item;

import java.util.List;

public record Treasure(Position position, List<Item> items, int xp) {

    private static final int EMPTY_XP = 0;

    public static Treasure create(Position position, List<Item> items, int xp) {
        return new Treasure(position, items, xp);
    }

    public static int emptyXp() {
        return EMPTY_XP;
    }

}
