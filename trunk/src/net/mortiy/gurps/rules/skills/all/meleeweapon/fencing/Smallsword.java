package net.mortiy.gurps.rules.skills.all.meleeweapon.fencing;

import net.mortiy.gurps.rules.Individual;
import net.mortiy.gurps.rules.skills.all.meleeweapon.FencingMeleeWeapon;

/**
 * Smallsword
 * ========================
 * Description:
 * Any short (up to 1 yard), light thrusting
 * sword or one-handed short staff (such
 * as the sticks used in the martial arts
 * arnis, escrima, and kali).
 */
public class Smallsword extends FencingMeleeWeapon {
    public Smallsword(Individual individual) {
        super(individual, "Smallsword");
        try {
            setDefault("Shortsword", -4);
        } catch (UnknownSkillDefaultSkillException e) {
            e.printStackTrace();
        }
    }
}
