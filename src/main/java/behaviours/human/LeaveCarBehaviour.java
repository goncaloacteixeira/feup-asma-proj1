package behaviours.human;

import agents.HumanAgent;
import jade.core.behaviours.OneShotBehaviour;
import utils.ServiceUtils;

/**
 * State for when the car travel ends and the human has to leave the car.
 */
public class LeaveCarBehaviour extends OneShotBehaviour {

    private final FSMHumanBehaviour fsmHumanBehaviour;

    private int exitValue;

    public LeaveCarBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        this.fsmHumanBehaviour = fsmHumanBehaviour;
    }

    @Override
    public void action() {
        System.out.printf("%s: Leaving the car.\n", this.fsmHumanBehaviour.getAgent().getLocalName());
        // removes the human from the DF service of the ride. goodbye!
        HumanAgent humanAgent = (HumanAgent) this.myAgent;
        ServiceUtils.leaveService(humanAgent, this.fsmHumanBehaviour.getCurrentCarService());
        this.fsmHumanBehaviour.setCurrentCarService(null);

        // updates the state of the machine
        if (fsmHumanBehaviour.currentLocationIndex == fsmHumanBehaviour.path.getLength()) {
            this.exitValue = FSMHumanBehaviour.EVENT_DST;
        } else {
            this.exitValue = FSMHumanBehaviour.EVENT_DEF;
        }
    }

    @Override
    public int onEnd() {
        return this.exitValue;
    }
}
