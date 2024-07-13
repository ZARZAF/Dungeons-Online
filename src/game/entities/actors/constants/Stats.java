package game.entities.actors.constants;

public class Stats {

    /** Base stats*/
    public static final double BASE_HP = 100;
    public static final double BASE_MANA = 100;
    public static final double BASE_ATTACK = 50;
    public static final int BASE_DEFENCE = 50;

    /** Base progress*/
    public static final int BASE_XP = 0;
    public static final int BASE_LEVEL = 1;

    /** Level up stats*/
    public static final double HP_INCREASE = 10;
    public static final double MANA_INCREASE = 10;
    public static final double ATTACK_INCREASE = 5;
    public static final int DEFENCE_INCREASE = 5;

    /** Level up xp requirement*/
    public static final int LEVEL_UP_XP_REQUIREMENT = 100;

    /** Minimum stats*/
    public static final int MINIMUM_STAT = 0;
    public static final int MINIMUM_LEVEL = 1;

    /** Respawn coefficients*/
    public static final int LEVEL_REDUCER = 2;
    public static final int XP_REDUCER = 4;

    /** Capacities*/
    public static double hpCapacity(int level) {
        return BASE_HP + level * HP_INCREASE;
    }

    public static double manaCapacity(int level) {
        return BASE_MANA + level * MANA_INCREASE;
    }

}
