package behaviours.human;

import jade.core.behaviours.OneShotBehaviour;

class TravelDefaultBehaviour extends OneShotBehaviour {
    private final FSMHumanBehaviour fsmHumanBehaviour;

    /**
     * Simple behaviour to perform a traveling operation (by foot or subway)
     * @param fsmHumanBehaviour parent behaviour
     */
    public TravelDefaultBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        this.fsmHumanBehaviour = fsmHumanBehaviour;
    }

    @Override
    public void action() {
        String message = fsmHumanBehaviour.informTravel();
        System.out.println(message);
        // ((HumanAgent) myAgent).informMovement(msg);

        fsmHumanBehaviour.currentLocationIndex++;
    }
}
