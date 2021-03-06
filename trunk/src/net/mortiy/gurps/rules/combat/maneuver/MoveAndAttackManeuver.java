package net.mortiy.gurps.rules.combat.maneuver;

import net.mortiy.gurps.rules.combat.Fighter;
import net.mortiy.gurps.rules.combat.ManeuverType;
import net.mortiy.gurps.rules.equipment.weapon.Weapon;

/**
 * Created with IntelliJ IDEA.
 * User: oleksandr.sidko
 * Date: 10.02.13
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
public class MoveAndAttackManeuver extends AttackManeuver {

    public MoveAndAttackManeuver(Fighter targetFighter, AttackManeuver.Type attackType, Weapon weapon) {
        super(targetFighter, attackType, weapon);
        this.type = ManeuverType.MoveAndAttack;
    }
}
