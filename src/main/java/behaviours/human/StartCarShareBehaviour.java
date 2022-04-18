package behaviours.human;

import jade.core.behaviours.OneShotBehaviour;

class StartCarShareBehaviour extends OneShotBehaviour {
    private final FSMHumanBehaviour fsmHumanBehaviour;
    private int exitValue;

    /**
     * Behaviour to decide whether to join or initiate a car share
     * @param fsmHumanBehaviour parent behaviour
     */
    public StartCarShareBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        this.fsmHumanBehaviour = fsmHumanBehaviour;
    }

    @Override
    public void action() {
        System.out.println(myAgent.getLocalName() + ": Start Car Share...");
        exitValue = fsmHumanBehaviour.preferences.isCarShareInitiator() ? FSMHumanBehaviour.EVENT_INITIATE : FSMHumanBehaviour.EVENT_RESPOND;
    }

    @Override
    public int onEnd() {
        return exitValue;
    }
}
