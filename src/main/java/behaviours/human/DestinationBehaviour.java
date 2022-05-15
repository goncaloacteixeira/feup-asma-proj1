package behaviours.human;

import agents.HumanAgent;
import graph.GraphUtils;
import jade.core.behaviours.OneShotBehaviour;
import messages.results.PathEnd;

class DestinationBehaviour extends OneShotBehaviour {
    private final FSMHumanBehaviour fsmHumanBehaviour;

    /**
     * Final behaviour for Humans, reached final point on path
     * @param fsmHumanBehaviour parent behaviour
     */
    public DestinationBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        this.fsmHumanBehaviour = fsmHumanBehaviour;
    }

    @Override
    public void action() {
        double actualCost = GraphUtils.calculateCost(fsmHumanBehaviour.graph, fsmHumanBehaviour.path);
        ((HumanAgent) myAgent).informResults(new PathEnd(myAgent.getLocalName(), actualCost));
        System.out.printf("%s: Completed Path! Cost: %.02f\n", myAgent.getLocalName(), actualCost);
    }
}
