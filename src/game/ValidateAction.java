package game;

import game.entities.Map;
import game.entities.Position;
import game.entities.actors.Actor;
import game.entities.actors.Hero;
import game.entities.actors.constants.Direction;
import game.entities.items.Item;
import game.entities.items.Spell;

public class ValidateAction {

    public static boolean move(Direction direction, Hero hero, Game game) {
        Position movePosition = new Position(hero.getPosition());

        switch (direction) {
            case UP -> {
                movePosition.y++;
                return movePosition.y < game.getMap().getMapHeight() && game.getMap().get(movePosition) == Map.free();
            }
            case DOWN -> {
                movePosition.y--;
                return movePosition.y >= 0 && game.getMap().get(movePosition) == Map.free();
            }
            case LEFT -> {
                movePosition.x++;
                return movePosition.x < game.getMap().getMapWidth() && game.getMap().get(movePosition) == Map.free();
            }
            case RIGHT -> {
                movePosition.x--;
                return movePosition.x >= 0 && game.getMap().get(movePosition) == Map.free();
            }
            default -> {
                return false;
            }
        }
    }

    public static boolean drop(Item item, Hero hero) {
        return item != null && hero.getBackpack().contains(item);
    }

    public static boolean equip(Item item, Hero hero) {
        return hero.getBackpack().contains(item) && item.getLevel() <= hero.getLevel();
    }

    public static boolean attack(Actor attacker, Actor attacked) {
        if (attacker instanceof Hero hero
                && hero.getEquippedItem() != null
                && hero.getEquippedItem() instanceof Spell spell
                && hero.getMana() < spell.getManaCost()) {
            return false;
        }
        return attacker.isAlive() && attacked.isAlive() && attacker.getPosition().equals(attacked.getPosition());
    }

}
