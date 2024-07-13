package game.entities.items;

public abstract class Item {

    private String name;
    private int level;

    public Item(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

}
