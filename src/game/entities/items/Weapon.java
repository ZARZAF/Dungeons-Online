package game.entities.items;

public class Weapon extends Item {
    private final double attackBonus;

    Weapon(String name, int level, double attackBonus) {
        super(name, level);
        this.attackBonus = attackBonus;
    }

    public static Weapon create(String name, int level, double attackBonus) {
        return new Weapon(name, level, attackBonus);
    }

    public double getAttack() {
        return attackBonus;
    }

}
