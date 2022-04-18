package behaviours.car;

import jade.core.Agent;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

public class ContractNetResponderCarBehaviour extends ContractNetResponder {
    public ContractNetResponderCarBehaviour(Agent a, MessageTemplate mt) {
        super(a, mt);
    }
}
