package game.entities.actors;

import game.entities.Position;
import game.entities.actors.constants.Direction;
import game.entities.actors.constants.Stats;
import game.entities.items.Item;
import game.entities.items.Potion;
import game.entities.items.Spell;
import game.entities.items.Weapon;
import logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static game.entities.actors.constants.Stats.LEVEL_REDUCER;
import static game.entities.actors.constants.Stats.XP_REDUCER;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class Hero implements Actor {

    private Position position;
    private double hp;
    private double mana;
    private double attack;
    private int defence;
    private int xp;
    private int level;
    private boolean alive;
    private Item equippedItem;
    private List<Item> backpack;
    public static final int BACKPACK_CAPACITY = 10;
    public static final double DEFENCE_COEFFICIENT = 0.6;

    private Hero(Position position, double hp, double mana, int xp, int level, double attack, int defence) {
        this.position = position;
        this.hp = hp;
        this.mana = mana;
        this.xp = xp;
        this.level = level;
        this.attack = attack;
        this.defence = defence;
        this.alive = true;
        backpack = new ArrayList<>(BACKPACK_CAPACITY);
    }

    public static Hero create(Position position) {
        if (position == null) {
            System.out.println("Cannot create Hero with null position.");
            Logger.logError(Logger.error(), Level.SEVERE
                    , "Cannot create Hero because position null");
            System.exit(-1);
        }

        return new Hero(position,
                Stats.BASE_HP, Stats.BASE_MANA, Stats.BASE_XP, Stats.BASE_LEVEL, Stats.BASE_ATTACK, Stats.BASE_DEFENCE);
    }

    public int getBackpackItemsCount() {
        return backpack.size();
    }

    public int getBackpackCapacity() {
        return BACKPACK_CAPACITY;
    }

    public List<Item> getBackpack() {
        return Collections.unmodifiableList(backpack);
    }

    public int getLevel() {
        return level;
    }

    public double getHp() {
        return hp;
    }

    public double getMana() {
        return mana;
    }

    public void addXp(int xpAmount) {
        if (xpAmount < Stats.MINIMUM_STAT) {
            return;
        }

        xp += xpAmount;
        while (xp >= level * Stats.LEVEL_UP_XP_REQUIREMENT) {
            xp -= level * Stats.LEVEL_UP_XP_REQUIREMENT;
            levelUp();
        }
    }

    public int dropXp() {
        return xp + ((level - Stats.MINIMUM_LEVEL) * Stats.LEVEL_UP_XP_REQUIREMENT) / 2;
    }

    public void damage(double damage) {
        if (damage < Stats.MINIMUM_STAT) {
            return;
        }

        double inflictDamage = max(Stats.MINIMUM_STAT, damage - defence * DEFENCE_COEFFICIENT);

        if (hp <= inflictDamage) {
            die();
        } else {
            hp -= inflictDamage;
        }
    }

    public double attack() {
        if (equippedItem != null && equippedItem instanceof Weapon weapon) {
            if (weapon instanceof Spell spell) {
                drainMana(spell.getManaCost());
            }
            return attack + weapon.getAttack();
        } else {
            return attack;
        }
    }

    public void drainMana(double mana) {
        this.mana = max(Stats.MINIMUM_STAT, this.mana - mana);
    }

    public Position getPosition() {
        return position;
    }

    public Item getEquippedItem() {
        return equippedItem;
    }

    public Item dropItem(Item item) {
        backpack.remove(item);

        if (!backpack.isEmpty()) {
            equippedItem = backpack.iterator().next();
        } else {
            equippedItem = null;
        }

        return item;
    }

    public void pickUp(Item item) {
        backpack.add(item);
    }

    public void move(Direction direction) {
        switch (direction) {
            case UP -> position.y++;
            case DOWN -> position.y--;
            case LEFT -> position.x++;
            case RIGHT -> position.x--;
        }
    }

    private void addPotionBonus(Potion potion) {
        hp = min(potion.getHpRegen() + hp, Stats.hpCapacity(level));
        mana = min(potion.getManaRegen() + mana, Stats.manaCapacity(level));
    }

    public void equipItem(Item item) {
        if (item == null) {
            equippedItem = item;
        } else if (item instanceof Potion potion) {
            equippedItem = potion;
            addPotionBonus(potion);
            backpack.remove(equippedItem);
            equippedItem = null;
        } else if (item instanceof Weapon weapon) {
            equippedItem = weapon;
        }
    }

    public void die() {
        hp = Stats.MINIMUM_STAT;
        alive = false;
    }

    public void respawn(Position position) {
        if (position != null) {
            this.position = position;
        }

        level /= LEVEL_REDUCER;
        xp /= XP_REDUCER;
        hp = Stats.BASE_HP + Stats.HP_INCREASE * level;
        mana = Stats.BASE_MANA + Stats.MANA_INCREASE * level;
        attack = Stats.BASE_ATTACK + Stats.ATTACK_INCREASE * level;
        defence = Stats.BASE_DEFENCE + Stats.DEFENCE_INCREASE * level;
        alive = true;
    }

    public void levelUp() {
        level++;
        hp += Stats.HP_INCREASE;
        mana += Stats.MANA_INCREASE;
        attack += Stats.ATTACK_INCREASE;
        defence += Stats.DEFENCE_INCREASE;
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hero hero = (Hero) o;
        return Double.compare(hero.hp, hp) == 0 &&
                Double.compare(hero.mana, mana) == 0 &&
                xp == hero.xp &&
                level == hero.level &&
                Objects.equals(position, hero.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, hp, mana, xp, level);
    }

}
