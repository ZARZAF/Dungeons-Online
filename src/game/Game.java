package game;

import game.entities.Position;
import game.entities.actors.Hero;
import game.entities.actors.Minion;
import game.entities.Treasure;
import game.entities.Map;
import game.entities.items.Item;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Game {

    private final Map map;
    private final List<Hero> heroes;
    private final List<Minion> minions;
    private final List<Treasure> treasures;
    private static final int MAXIMUM_NUMBER_OF_ITEMS_IN_TREASURE = 2;
    private static final int XP_PER_ITEM = 100;

    private Game(String mapName, int playersCount) throws FileNotFoundException {
        map = setMap(mapName);

        heroes = new ArrayList<>(playersCount);
        for (int i = 0; i < playersCount; i++) {
            addHero();
        }

        minions = new ArrayList<>(playersCount);
        for (int i = 0; i < playersCount; i++) {
            addMinion();
        }

        treasures = new ArrayList<>(playersCount);
        for (int i = 0; i < playersCount; i++) {
            addTreasure();
        }
    }

    public static Game create(String mapName, int playersCount) throws FileNotFoundException {
        return new Game(mapName, playersCount);
    }

    public Map getMap() {
        return map;
    }

    public List<Hero> getHeroes() {
        return Collections.unmodifiableList(heroes);
    }

    public List<Hero> getHeroes(Position position) {
        List<Hero> heroesAtLocation = new ArrayList<>();

        for (Hero h : heroes) {
            if (h.getPosition().equals(position)) {
                heroesAtLocation.add(h);
            }
        }

        return heroesAtLocation;
    }

    public List<Minion> getMinions() {
        return Collections.unmodifiableList(minions);
    }

    public List<Minion> getMinions(Position position) {
        List<Minion> minionsAtLocation = new ArrayList<>();

        for (Minion m : minions) {
            if (m.getPosition().equals(position)) {
                minionsAtLocation.add(m);
            }
        }

        return minionsAtLocation;
    }

    public List<Treasure> getTreasures() {
        return Collections.unmodifiableList(treasures);
    }

    public List<Treasure> getTreasures(Position position) {
        List<Treasure> treasuresAtLocation = new ArrayList<>();

        for (Treasure t : treasures) {
            if (t.position().equals(position)) {
                treasuresAtLocation.add(t);
            }
        }

        return treasuresAtLocation;
    }

    public void removeTreasure(Treasure treasure) {
        treasures.remove(treasure);
    }

    private Map setMap(String mapName) throws FileNotFoundException {
        for (Map m : Entities.getMaps()) {
            if (m.getMapName().equals(mapName)) {
                return m;
            }
        }

        throw new FileNotFoundException("Map: " + mapName + " not found.");
    }

    public Position getRandomMapCoordinates() {
        Position newPosition;
        Random random = new Random();

        do {
            int x = random.nextInt(map.getMapWidth());
            int y = random.nextInt(map.getMapHeight());

            newPosition = new Position(x, y);
        } while (map.get(newPosition) == Map.wall());

        return newPosition;
    }

    private void addHero() {
        heroes.add(Hero.create(getRandomMapCoordinates()));
    }

    private static int generateMinionLevel() {
        Random random = new Random();
        final int probability = 100;
        final int one = 1;
        final int two = 2;
        final int three = 3;
        final int oneProb = 60;
        final int twoProb = 90;
        int randomNumber = random.nextInt(probability);

        if (randomNumber < oneProb) {
            return one;
        } else if (randomNumber < twoProb) {
            return two;
        } else {
            return three;
        }
    }

    private void addMinion() {
        minions.add(Minion.create(getRandomMapCoordinates(), generateMinionLevel()));
    }

    private List<Item> generateTresureContent() {
        List<Item> content = new ArrayList<>();
        
        if (Entities.getItems().isEmpty()) {
            return content;
        }

        Random random = new Random();
        int numberOfItems = random.nextInt(MAXIMUM_NUMBER_OF_ITEMS_IN_TREASURE) + 1;

        int randomItemIndex;
        for (int i = 0; i < numberOfItems; i++) {
            randomItemIndex = random.nextInt(Entities.getItems().size());
            content.add(Entities.getItems().get(randomItemIndex));
        }

        return content;
    }

    private void addTreasure() {
        List<Item> treasureContent = generateTresureContent();
        treasures.add(
                Treasure.create(getRandomMapCoordinates(), treasureContent, treasureContent.size() * XP_PER_ITEM));
    }

    public void addTreasure(Treasure treasure) {
        treasures.add(treasure);
    }

}
