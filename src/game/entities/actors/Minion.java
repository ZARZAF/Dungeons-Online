package game.entities.actors;

import game.entities.Position;
import game.entities.actors.constants.Stats;
import logger.Logger;

import java.util.Objects;
import java.util.logging.Level;

public class Minion implements Actor {
    private Position position;
    private double hp;
    private int xp;
    private int level;
    private boolean alive;
    private static final double DEFENCE_COEFFICIENT = 0.3;

    private Minion(Position position, int level) {
        this.position = position;
        this.level = level;
        this.hp = Stats.BASE_HP + level * Stats.HP_INCREASE;
        alive = true;
    }

    public static Minion create(Position position, int level) {
        if (position == null) {
            System.out.println("Cannot create Minion with null position.");
            Logger.logError(Logger.error(), Level.SEVERE, "Cannot create Minion with null position.");
        }
        if (level < Stats.MINIMUM_LEVEL) {
            level = Stats.MINIMUM_LEVEL;
        }
        return new Minion(position, level);
    }

    public double getHp() {
        return hp;
    }

    public int getLevel() {
        return level;
    }

    public void addXp(int xpAmount) {
        if (xpAmount < Stats.MINIMUM_STAT) {
            return;
        }

        xp += xpAmount;
        while (xp > level * Stats.LEVEL_UP_XP_REQUIREMENT) {
            xp -= level * Stats.LEVEL_UP_XP_REQUIREMENT;
            levelUp();
        }
    }

    public int dropXp() {
        return xp + (level * Stats.LEVEL_UP_XP_REQUIREMENT);
    }

    public void damage(double damage) {
        if (damage < Stats.MINIMUM_STAT) {
            return;
        }

        double inflictDamage = damage - (Stats.BASE_DEFENCE + level * Stats.DEFENCE_INCREASE) * DEFENCE_COEFFICIENT;

        if (hp <= inflictDamage) {
            die();
        } else {
            hp -= inflictDamage;
        }
    }

    public double attack() {
        return Stats.BASE_ATTACK + level * Stats.ATTACK_INCREASE;
    }

    public Position getPosition() {
        return position;
    }

    public void die() {
        hp = Stats.MINIMUM_STAT;
        alive = false;
    }

    public void respawn(Position position) {
        this.position = position;
        hp = Stats.BASE_HP + level * Stats.HP_INCREASE;
        alive = true;
    }

    public void levelUp() {
        level++;
        hp += Stats.HP_INCREASE;
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Minion minion = (Minion) o;
        return level == minion.level && Objects.equals(position, minion.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, level);
    }

}
