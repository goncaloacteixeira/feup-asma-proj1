package agents;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;

public abstract class SubscribableAgent extends Agent {

    public abstract void setAgentDescription(DFAgentDescription agentDescription);

    public abstract DFAgentDescription getAgentDescription();
}
