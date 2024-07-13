package game.entities;

import logger.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

public class Map {

    private final String mapName;
    private final ArrayList<ArrayList<Boolean>> map;
    private static final char FREE_SPACE_CHAR = '.';
    private static final char WALL_CHAR = '#';
    private static final boolean FREE_SPACE = true;
    private static final boolean WALL = false;
    private static final boolean DEFAULT_VALUE = true;
    private static final String MAP_PATH = "maps/";

    private Map(ArrayList<ArrayList<Boolean>> map, String mapName) {
        this.map = map;
        this.mapName = mapName;
    }

    public static Map load(String fileName) {
        try (FileReader fileReader = new FileReader(MAP_PATH + fileName);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            ArrayList<ArrayList<Boolean>> loadMap = new ArrayList<>();

            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                loadMap.add(new ArrayList<>());
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == FREE_SPACE_CHAR) {
                        loadMap.get(lineCount).add(FREE_SPACE);
                    } else if (line.charAt(i) == WALL_CHAR) {
                        loadMap.get(lineCount).add(WALL);
                    } else {
                        loadMap.get(lineCount).add(DEFAULT_VALUE);
                    }
                }
                lineCount++;
            }

            Collections.reverse(loadMap);
            return new Map(loadMap, fileName);
        } catch (IOException e) {
            System.out.println("Cannot load map");
            Logger.logError(Logger.error(), Level.SEVERE, "Cannot load map", e);
            System.exit(-1);
            return null;
        }
    }

    public Boolean get(Position position) {
        if (position.y < 0 || position.x < 0 || position.y >= map.size() || position.x >= map.getFirst().size()) {
            System.out.println("Invalid position.");
            Logger.logError(Logger.error(), Level.SEVERE, "Invalid position.");
            System.exit(-1);
        }

        return map.get(position.y).get(position.x);
    }

    public String getMapName() {
        return mapName;
    }

    public Integer getMapHeight() {
        return map.size();
    }

    public Integer getMapWidth() {
        return map.getFirst().size();
    }

    public static Boolean wall() {
        return WALL;
    }

    public static Boolean free() {
        return FREE_SPACE;
    }
}
