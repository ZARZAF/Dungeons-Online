package game.entities.actor;

import game.entities.Position;
import game.entities.actors.Hero;
import game.entities.actors.constants.Stats;
import game.entities.items.Item;
import game.entities.items.Potion;
import game.entities.items.Weapon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HeroTest {

    @Test
    public void testCreateHero() {
        Position position = new Position(0, 0);
        Hero hero = Hero.create(position);
        assertNotNull(hero);
        assertEquals(position, hero.getPosition());
        assertEquals(Stats.BASE_HP, hero.getHp());
        assertEquals(Stats.BASE_MANA, hero.getMana());
        assertEquals(Stats.BASE_XP, hero.dropXp());
        assertEquals(Stats.BASE_LEVEL, hero.getLevel());
        assertEquals(Stats.BASE_ATTACK, hero.attack());
        assertTrue(hero.isAlive());
        assertTrue(hero.getBackpack().isEmpty());
    }

    @Test
    public void testAddXp() {
        Hero hero = Hero.create(new Position(0, 0));
        hero.addXp(101);
        assertEquals(51, hero.dropXp(), "Testing adding xp 101 = lvl 2 + 1xp remaining.");
        assertEquals(2, hero.getLevel(), "Testing damaging hero without killing him.");
    }

    @Test
    public void testDamage() {
        Hero hero = Hero.create(new Position(0, 0));
        hero.damage(50);
        assertEquals(80, hero.getHp(), "Testing damaging hero without killing him.");
        hero.damage(Stats.BASE_HP * 2); // Damage that exceeds HP
        assertFalse(hero.isAlive(), "Testing damaging hero with killing him.");
    }

    @Test
    public void testPickUpAndDropItem() {
        Hero hero = Hero.create(new Position(0, 0));
        Item potion = Potion.create("Health Potion", 10, 10, 10);
        Item weapon = Weapon.create("Sword", 10, 10);
        hero.pickUp(potion);
        hero.pickUp(weapon);
        assertEquals(2, hero.getBackpackItemsCount());
        assertTrue(hero.getBackpack().contains(potion));
        assertTrue(hero.getBackpack().contains(weapon));

        hero.dropItem(potion);
        assertEquals(1, hero.getBackpackItemsCount());
        assertFalse(hero.getBackpack().contains(potion));
    }

}

