package game.entities.actors;

import game.entities.Position;

public interface Actor {

    /** Adds xpAmount to Actor's current xp.
     *
     *  @throws IllegalArgumentException if xpAmount is negative.*/
    void addXp(int xpAmount);

    /** Returns the xpAmount that should be dropped when the Actor dies.*/
    int dropXp();

    /**Damages the Actor with damage hp points.
     *
     * @throws IllegalArgumentException if damage is negative.*/
    void damage(double damage);

    /** Returns the attack points incl. modifiers (weapons, critical hits).*/
    double attack();

    /** Returns the current position of the Actor.*/
    Position getPosition();

    void die();

    /** Resets the Actor to its basic status and positions it at position.*
     *
     *  @throws IllegalArgumentException if position is null.*/

    void respawn(Position position);

    /** Increases the level of the Actor and sets the xp to current level required xp if it was less than that.
     * Increases the stats of the Actor according to the current new level.*/
    void levelUp();

    /** Returns the alive status of the actor.*/
    boolean isAlive();

    /** Returns the hp of the actor.*/
    double getHp();

    /** Returns the level of the actor.*/
    int getLevel();

}
