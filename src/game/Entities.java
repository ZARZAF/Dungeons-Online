package game;

import game.entities.Map;
import game.entities.items.Item;
import game.entities.items.Potion;
import game.entities.items.Spell;
import game.entities.items.Weapon;
import logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

public class Entities {
    private static final String MAPS_FOLDER_PATH = "maps/";
    private static final ArrayList<Map> MAPS = loadMaps();
    private static final String ITEMS_FOLDER_PATH = "items/";
    private static final String WEAPONS_PATH = ITEMS_FOLDER_PATH + "weapons.csv";
    private static final String POTIONS_PATH = ITEMS_FOLDER_PATH + "potions.csv";
    private static final String SPELLS_PATH = ITEMS_FOLDER_PATH + "spells.csv";
    private static final ArrayList<Item> ITEMS = loadItems();

    public static List<Map> getMaps() {
        return Collections.unmodifiableList(MAPS);
    }

    public static List<Item> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    private static ArrayList<Map> loadMaps() {
        ArrayList<Map> maps = new ArrayList<>();

        File folder = new File(MAPS_FOLDER_PATH);

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        maps.add(Map.load(file.getName()));
                    }
                }
            } else {
                System.out.println("No maps found in maps folder");
                Logger.logError(Logger.error(), Level.SEVERE, "No maps found in maps folder");
                System.exit(-1);
            }
        } else {
            System.out.println("Provided path is not a directory.");
            Logger.logError(Logger.error(), Level.SEVERE, "Provided path is not a directory.");
            System.exit(-1);
        }

        return maps;
    }

    private static ArrayList<Item> readItemsFile(File file, Function<String, Item> lineParser) {
        try (FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            ArrayList<Item> loadItems = new ArrayList<>();

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                loadItems.add(lineParser.apply(line));
            }

            return loadItems;
        } catch (IOException e) {
            System.out.println("Cannot read files.");
            Logger.logError(Logger.error(), Level.SEVERE, "Cannot read files.", e);
            System.exit(-1);
            return null;
        }
    }

    private static Item readPotion(String line) {
        String[] attributes = line.split(",");

        final int nameIndex = 0;
        final int levelIndex = 1;
        final int hpRegenIndex = 2;
        final int manaRegenIndex = 3;

        String name = attributes[nameIndex];
        int level = Integer.parseInt(attributes[levelIndex]);
        int hpRegen = Integer.parseInt(attributes[hpRegenIndex]);
        double manaRegen = Double.parseDouble(attributes[manaRegenIndex]);

        return Potion.create(name, level, hpRegen, manaRegen);
    }

    private static Item readSpell(String line) {
        String[] attributes = line.split(",");

        final int nameIndex = 0;
        final int levelIndex = 1;
        final int attackIndex = 2;
        final int manaCostIndex = 3;

        String name = attributes[nameIndex];
        int level = Integer.parseInt(attributes[levelIndex]);
        double attack = Double.parseDouble(attributes[attackIndex]);
        double manaCost = Double.parseDouble(attributes[manaCostIndex]);

        return Spell.create(name, level, attack, manaCost);
    }

    private static Item readWeapon(String line) {
        String[] attributes = line.split(",");

        final int nameIndex = 0;
        final int levelIndex = 1;
        final int attackIndex = 2;

        String name = attributes[nameIndex];
        int level = Integer.parseInt(attributes[levelIndex]);
        double attack = Double.parseDouble(attributes[attackIndex]);

        return Weapon.create(name, level, attack);
    }

    private static ArrayList<Item> loadItems() {
        ArrayList<Item> items = new ArrayList<>();

        File weapons = new File(WEAPONS_PATH);
        File spells = new File(SPELLS_PATH);
        File potion = new File(POTIONS_PATH);

        items.addAll(readItemsFile(weapons, Entities::readWeapon));
        items.addAll(readItemsFile(spells, Entities::readSpell));
        items.addAll(readItemsFile(potion, Entities::readPotion));

        return items;
    }
}
