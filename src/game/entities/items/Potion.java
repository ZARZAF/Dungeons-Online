package game.entities.items;

public class Potion extends Item {

    private final double hpRegen;
    private final double manaRegen;

    private Potion(String name, int level, double hpRegen, double manaRegen) {
        super(name, level);
        this.hpRegen = hpRegen;
        this.manaRegen = manaRegen;
    }

    public static Potion create(String name, int level, double hpRegen, double manaRegen) {
        return new Potion(name, level, hpRegen, manaRegen);
    }

    public double getHpRegen() {
        return hpRegen;
    }

    public double getManaRegen() {
        return manaRegen;
    }
}
