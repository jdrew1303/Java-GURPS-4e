package net.mortiy.gurps.rules.combat.maneuver;

import net.mortiy.gurps.Log;
import net.mortiy.gurps.rules.Individual;
import net.mortiy.gurps.rules.combat.Fighter;
import net.mortiy.gurps.rules.combat.Maneuver;
import net.mortiy.gurps.rules.combat.ManeuverResult;
import net.mortiy.gurps.rules.combat.ManeuverType;
import net.mortiy.gurps.rules.combat.exceptions.ImpossibleManeuverException;
import net.mortiy.gurps.rules.combat.exceptions.IsNotReadyException;

/**
 * Created with IntelliJ IDEA.
 * User: oleksandr.sidko
 * Date: 10.02.13
 * Time: 19:29
 * To change this template use File | Settings | File Templates.
 */
public class ChangePostureManeuver extends Maneuver {
    private Individual.Posture posture;
    public ChangePostureManeuver(Individual.Posture posture) {
        super(ManeuverType.ChangePosture);
        this.posture = posture;
    }

    @Override
    public ManeuverResult resolve(Fighter fighter) throws ImpossibleManeuverException, IsNotReadyException {
        Log.i("Change Posture Maneuver", "'%s' changed posture to '%s'", fighter.getIndividual().getName(), posture);
        fighter.getIndividual().changePosture(posture);
        fighter.setNextManeuver(new DoNothingManeuver());
        return new ManeuverResult(ManeuverResult.Status.Success);
    }
}
