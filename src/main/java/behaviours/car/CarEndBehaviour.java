package behaviours.car;

import jade.core.behaviours.OneShotBehaviour;

public class CarEndBehaviour extends OneShotBehaviour {
    @Override
    public void action() {
        System.out.println("FINITO");
    }
}
