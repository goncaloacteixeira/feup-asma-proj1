package behaviours.human;

import graph.edge.RoadEdge;
import jade.core.behaviours.OneShotBehaviour;
import org.jgrapht.graph.DefaultWeightedEdge;

class EvaluatePathBehaviour extends OneShotBehaviour {
    private final FSMHumanBehaviour fsmHumanBehaviour;
    private int exitValue;

    /**
     * Behaviour to evaluate if it is a road or default (street or subway) segment
     *
     * @param fsmHumanBehaviour parent behaviour
     */
    public EvaluatePathBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        this.fsmHumanBehaviour = fsmHumanBehaviour;
    }

    @Override
    public void action() {
        if (fsmHumanBehaviour.currentLocationIndex == fsmHumanBehaviour.path.getLength()) {
            exitValue = FSMHumanBehaviour.EVENT_DST;
            return;
        }

        DefaultWeightedEdge edge = fsmHumanBehaviour.path.getEdgeList().get(fsmHumanBehaviour.currentLocationIndex);

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
