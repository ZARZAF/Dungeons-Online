package game.entities.actors.constants;

import game.entities.actors.exceptions.InvalidDirectionException;

public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    public static Direction get(String value) throws InvalidDirectionException {
        return switch (value) {
            case "u" -> UP;
            case "d" -> DOWN;
            case "l" -> LEFT;
            case "r" -> RIGHT;
            default -> throw new InvalidDirectionException("Cannot find such direction.");
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case UP -> "u";
            case DOWN -> "d";
            case LEFT -> "l";
            case RIGHT -> "r";
        };
    }
}
