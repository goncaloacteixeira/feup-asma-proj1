package behaviours.human;

import graph.edge.Edge;
import graph.edge.RoadEdge;
import jade.core.behaviours.OneShotBehaviour;

class TravelCarBehaviour extends OneShotBehaviour {
    private final FSMHumanBehaviour fsmHumanBehaviour;
    private int exitValue;

    public TravelCarBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        this.fsmHumanBehaviour = fsmHumanBehaviour;
    }

    @Override
    public void action() {
        String message = fsmHumanBehaviour.informTravel();
        System.out.println(message);
        // ((HumanAgent) myAgent).informMovement(msg);

        fsmHumanBehaviour.currentLocationIndex++;

        if (fsmHumanBehaviour.currentLocationIndex == fsmHumanBehaviour.path.getLength()) {
            exitValue = FSMHumanBehaviour.EVENT_DST;
            return;
        }

        Edge edge = (Edge) fsmHumanBehaviour.path.getEdgeList().get(fsmHumanBehaviour.currentLocationIndex);

        if (edge instanceof RoadEdge) {
            exitValue = FSMHumanBehaviour.EVENT_CAR;
        } else {
            exitValue = FSMHumanBehaviour.EVENT_DEF;
        }
    }

    @Override
    public int onEnd() {
        return exitValue;
    }
}
