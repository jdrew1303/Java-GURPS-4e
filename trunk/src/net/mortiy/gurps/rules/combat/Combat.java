package net.mortiy.gurps.rules.combat;

import net.mortiy.gurps.Log;
import net.mortiy.gurps.rules.Character;
import net.mortiy.gurps.rules.attributes.Attribute;
import net.mortiy.gurps.rules.combat.exceptions.ImpossibleManeuverException;
import net.mortiy.gurps.rules.combat.maneuver.*;
import net.mortiy.gurps.rules.equipment.Item;
import net.mortiy.gurps.rules.equipment.weapon.MeleeWeapon;
import net.mortiy.gurps.rules.equipment.weapon.MusclePoweredMeleeWeapon;
import net.mortiy.gurps.rules.equipment.weapon.Weapon;
import net.mortiy.gurps.rules.equipment.weapon.statistics.MusclePoweredDamage;
import net.mortiy.gurps.rules.skills.Skill;
import net.mortiy.gurps.rules.table.DiceRoller;
import net.mortiy.gurps.rules.table.RollFormula;
import net.mortiy.gurps.rules.table.rolls.SuccessRoll;

import java.util.*;

/**
 * Represents fight between several Characters
 */
public class Combat {

    private List<Fighter> fighters = new ArrayList<>();

    public Combat(Character... fighters) {
        this(Arrays.asList(fighters));
    }

    public Combat(List<Character> characters) {

        /**
         * Determine turns order by character's Basic Speed:
         */
        Collections.sort(characters, new Comparator<Character>() {
            public int compare(Character c1, Character c2) {
                int result = c2.getBasicSpeed().compareTo(c1.getBasicSpeed());
                if (result == 0) {
                    return c2.getBasicAttribute(Attribute.Dexterity).compareTo(c1.getBasicAttribute(Attribute.Dexterity));
                }
                return result;
            }
        });

        /**
         * Initialize fighters and their starting maneuvers:
         */
        for (Character character : characters) {
            Fighter fighter = new Fighter(character);
            fighter.setManeuver(new DoNothingManeuver());
            this.fighters.add(fighter);
        }
    }

    public List<Fighter> getFighters() {
        return fighters;
    }

    /**
     * Removes character from the combat
     *
     * @param character
     */
    public void leaveCombat(Character character) {
        for (Fighter fighter : fighters) {
            if (fighter.getCharacter() == character) {
                leaveCombat(fighter);
            }
        }
    }

    public void leaveCombat(Fighter fighter) {
        fighters.remove(fighter);
    }

    /**
     * Resolver fighter's maneuver
     * @param fighter
     * @throws net.mortiy.gurps.rules.combat.exceptions.ImpossibleManeuverException
     */
    void resolveManeuver(Fighter fighter) throws ImpossibleManeuverException {
        Maneuver maneuver = fighter.getActiveManeuver();

        Character fighterCharacter = fighter.getCharacter();
        String fighterName = fighterCharacter.getName();

        _log("'%s' performs <%s>", fighterName, maneuver.getType());

        switch (maneuver.getType()) {

            case DoNothing:
                _log("'%s' does nothing.", fighterName);
                break;
            case ChangePosture:
                ChangePostureManeuver changePostureManeuver = (ChangePostureManeuver) maneuver;
                Character.Posture posture = changePostureManeuver.getPosture();
                _log("'%s' changed posture to '%s'", fighterName, posture);
                fighterCharacter.changePosture(posture);
                fighter.setManeuver(new DoNothingManeuver());
                break;
            case Attack:
                resolveAtackManeuver(fighter, (AttackManeuver)maneuver);
                break;
            case MoveAndAttack:
                resolveAtackManeuver(fighter, (MoveAndAttackManeuver) maneuver);
                break;
            case AllOutAttack:
                resolveAtackManeuver(fighter, (AllOutAtackManeuver) maneuver);
                break;
        }
    }


    private void resolveAtackManeuver(Fighter fighter, AttackManeuver attackManeuver) throws ImpossibleManeuverException {

        Character fighterCharacter = fighter.getCharacter();
        Fighter targetFighter = attackManeuver.getTargetFigher();

        Item item = fighter.getActiveWeapon();
        if (item == null) {
            throw new ImpossibleManeuverException("'%s' has no equipped weapon.", fighterCharacter.getName());
        }
        if(!fighter.getReadyList().contains(item)){
            return;
        }
        // Check if equipped item can be used as weapon:
        if (item instanceof Weapon) {

            // If weapon is Muscle Powered:
            if (item instanceof MeleeWeapon) {


                Damage fightersDamage = damageRoll(fighter);
                // Perform ATTACK ROLL:
                switch (attackRoll(fighter)) {
                    case CriticalSuccess:
                        // Critical hit can't be defended,
                        // so no defence roll by target fighter performed:
                        targetFighter.injure(fightersDamage);
                        break;
                    case Success:

                        // Perform DEFENSE ROLL:
                        switch (defenseRoll(targetFighter)) {
                            case CriticalSuccess:
                            case Success:
                                // TODO: Successful defense event

                                break;
                            case CriticalFailure:
                            case Failure:
                                targetFighter.injure(fightersDamage);
                                break;
                        }
                        break;
                    case CriticalFailure:
                    case Failure:
                        // TODO: Failed attack event

                        break;
                }
            }
        } else {
            throw new ImpossibleManeuverException("'%s' is not weapon.", item.getName());
        }

    }



    /**
     * Returns damage amount dealt by fighter with given weapon
     * @param fighter Attacking fighter
     * @return Damage object
     * @throws ImpossibleManeuverException
     */
    private Damage damageRoll(Fighter fighter) throws ImpossibleManeuverException {

        Weapon weapon = fighter.getActiveWeapon();
        Character fighterCharacter = fighter.getCharacter();
        String fighterName = fighterCharacter.getName();

        int basicDamage;
        RollFormula damageFormula;
        Damage.Type damageType = null;
        if(weapon instanceof MusclePoweredMeleeWeapon){
            MusclePoweredMeleeWeapon meeleeWeapon = (MusclePoweredMeleeWeapon) weapon;
            MusclePoweredDamage.Type physicalDamageType = meeleeWeapon.getActiveWeaponMode().getBasicType();
            damageType = meeleeWeapon.getActiveWeaponMode().getWeaponDamage().getDamageType();
            try {
                damageFormula = meeleeWeapon.getDamageFormula(fighterCharacter, physicalDamageType);
            } catch (MeleeWeapon.WeaponHasNoSuchDamageTypeException e) {
                throw new ImpossibleManeuverException("'%s' has no '%s' damage type.", fighterName, physicalDamageType);
            }
        } else {
            // Mock:
            damageFormula = new RollFormula(1, 2);

        }
        basicDamage = DiceRoller.getInstance().roll(damageFormula).getTotal();

        // TODO: Add damage modifiers:

        _log("'%s' performs damage roll %s = [%d]", fighterName, damageFormula, basicDamage);

        return new Damage(basicDamage, damageType);
    }


    public DiceRoller.RollResult attackRoll(Fighter fighter) {
        Character fighterCharacter = fighter.getCharacter();
        String fighterName = fighterCharacter.getName();
        // Calculate Attack roll modifierse:
        // Get characters skill level for used weapon:
        Weapon weapon = fighter.getActiveWeapon();
        Class weaponSkillClass = weapon.getRequiredSkillClass();
        Skill weaponSkill = fighter.getCharacter().getSkill(weaponSkillClass);
        Integer skillLevel = weaponSkill.getSkillLevel();

        _log("'%s' has weapon skill '%s' level at %d", fighterName, weaponSkill.getName(), skillLevel);

        int attackModifiers = fighter.getAttackModifier(fighter.getActiveManeuver().getType(), weapon);

        SuccessRoll attackSuccessRoll = weaponSkill.successRoll(attackModifiers);

        // region Attack logging
        switch (attackSuccessRoll.getRollResult()) {
            case CriticalSuccess:
                _log("'%s' made critical hit with roll [%d] against [%d]", fighterName, attackSuccessRoll.getDiceRoll(), skillLevel);

                break;
            case Success:
                _log("'%s' makes hit with roll [%d] against [%d]", fighterName, attackSuccessRoll.getDiceRoll(), skillLevel);
                break;
            case CriticalFailure:
            case Failure:
                // TODO: Failed attack event
                _log("'%s' has failed attack with roll [%d] against [%d]", fighterName, attackSuccessRoll.getDiceRoll(), skillLevel);
                break;
        }

        // endregion

        return attackSuccessRoll.getRollResult();

    }
    public DiceRoller.RollResult defenseRoll(Fighter targetFighter) {
        // Resolve hit success:
        // Target fighter tries to perform defend roll:
        // TODO: Defence Roll

        Maneuver activeManeuver = targetFighter.getActiveManeuver();
        String targetFighterName = targetFighter.getCharacter().getName();

        // Defense is unavailable for fighter who already performed AllOutAttack this turn:
        if(activeManeuver.getType() == ManeuverType.AllOutAttack){
                _log("'%s' performed earlier AllOutAttack so can not defended", targetFighterName);
            return DiceRoller.RollResult.Failure;
        }

        Defense defense = targetFighter.getDefense();
            _log("'%s' tries to defend with <%s>", targetFighterName, defense.getBestStrategy());
        int defenseLevel = defense.getDefenseLevel();
        SuccessRoll defenseRoll = new SuccessRoll(defenseLevel).roll();
        DiceRoller.RollResult rollResult = defenseRoll.getRollResult();

        // region Defense logging
        switch (rollResult) {
            case CriticalSuccess:
            case Success:
                _log("'%s' successfully defended with <%s>", targetFighterName, defense.getBestStrategy());
                break;
            case CriticalFailure:
            case Failure:
                _log("'%s' failed <%s>", targetFighterName, defense.getBestStrategy());
                break;
        }
        // endregion

        return rollResult;
    }


    private void _log(String message, Object... args) {
        Log.i("Combat", String.format(message, args));
    }




}