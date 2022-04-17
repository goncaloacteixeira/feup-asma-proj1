package behaviours.human;

import graph.GraphUtils;
import jade.core.behaviours.OneShotBehaviour;

class DestinationBehaviour extends OneShotBehaviour {
    private final FSMHumanBehaviour fsmHumanBehaviour;

    public DestinationBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        this.fsmHumanBehaviour = fsmHumanBehaviour;
    }

    @Override
    public void action() {
        double actualCost = GraphUtils.calculateCost(fsmHumanBehaviour.graph, fsmHumanBehaviour.path);
        System.out.printf("%s: Completed Path! Cost: %.02f (before: %.02f)\n", myAgent.getLocalName(), actualCost, fsmHumanBehaviour.path.getWeight());
        if (fsmHumanBehaviour.path.getWeight() != actualCost) {
            System.out.printf("%s: Shared Road Segment!\n", myAgent.getLocalName());
        }
    }
}
