package game.entities.items;

public class Spell extends Weapon {

    private final double manaCost;

    private Spell(String name, int level, double attackBonus, double manaCost) {
        super(name, level, attackBonus);
        this.manaCost = manaCost;
    }

    public static Spell create(String name, int level, double attackBonus, double manaCost) {
        return new Spell(name, level, attackBonus, manaCost);
    }

    public double getManaCost() {
        return manaCost;
    }

}
