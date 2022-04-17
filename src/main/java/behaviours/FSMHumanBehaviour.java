package behaviours;

import graph.GraphUtils;
import graph.edge.Edge;
import graph.edge.RoadEdge;
import graph.vertex.Point;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

import java.util.Arrays;
import java.util.Date;

public class FSMHumanBehaviour extends FSMBehaviour {
    static String STATE_EVAL = "EVAL";
    static String STATE_CAR = "CAR";
    static String STATE_TRC = "TRAVEL_CAR";
    static String STATE_TRD = "TRAVEL_DEFAULT";
    static String STATE_DST = "DST";
    static String STATE_CNI = "INITIATOR";
    static String STATE_CNR = "RESPONDER";

    static int EVENT_DEF = 0;
    static int EVENT_CAR = 1;
    static int EVENT_DST = 2;
    static int EVENT_INITIATE = 3;
    static int EVENT_RESPOND = 4;

    private int currentLocationIndex = 0;
    protected Graph<Point, DefaultWeightedEdge> graph;
    protected GraphPath<Point, DefaultWeightedEdge> path;
    protected boolean initiator;

    public FSMHumanBehaviour(Agent a, Graph<Point, DefaultWeightedEdge> graph, String src, String dst, boolean initiator) {
        super(a);
        this.graph = graph;
        this.path = GraphUtils.getPathFromAtoB(graph, src, dst);
        this.initiator = initiator;

        if (initiator) {
            ServiceUtils.register(myAgent, "car-share-initiators");
        } else {
            ServiceUtils.register(myAgent, "car-share-responders");
        }


        System.out.println(myAgent.getLocalName() + ": Path: " + GraphUtils.printPath(graph, path));

        this.registerFirstState(new EvaluatePath(), STATE_EVAL);
        this.registerLastState(new Destination(), STATE_DST);

        this.registerState(new TravelDefault(), STATE_TRD);
        this.registerState(new StartCarShare(), STATE_CAR);
        this.registerState(new TravelCar(), STATE_TRC);
        this.registerState(new CNIHelper(myAgent, "car-share-responders"), STATE_CNI);
        this.registerState(new CNRHelper(myAgent), STATE_CNR);

        this.registerTransition(STATE_EVAL, STATE_CAR, EVENT_CAR);
        this.registerTransition(STATE_EVAL, STATE_DST, EVENT_DST);
        this.registerTransition(STATE_EVAL, STATE_TRD, EVENT_DEF);
        this.registerDefaultTransition(STATE_TRD, STATE_EVAL);

        this.registerTransition(STATE_CAR, STATE_CNI, EVENT_INITIATE);
        this.registerTransition(STATE_CAR, STATE_CNR, EVENT_RESPOND);
        this.registerDefaultTransition(STATE_CNI, STATE_TRC);
        this.registerDefaultTransition(STATE_CNR, STATE_TRC);

        this.registerTransition(STATE_TRC, STATE_TRC, EVENT_CAR);
        this.registerTransition(STATE_TRC, STATE_EVAL, EVENT_DEF);
        this.registerTransition(STATE_TRC, STATE_DST, EVENT_DST);
    }

    private Pair<ACLMessage, Integer> prepareCNIMessage(String service) {
        // Initiate ContractNet
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // Deadline is 10s after message is sent
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        msg.setContent("dummy-action");

        // procurar malta que está a procura de carro
        DFAgentDescription[] agents = ServiceUtils.search(myAgent, service);

        Arrays.stream(agents)
                .forEach(agent -> msg.addReceiver(agent.getName()));

        return Pair.of(msg, agents.length);
    }

    private MessageTemplate prepareCNRTemplate() {
        // Respond to ContractNet
        return MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));
    }

    class EvaluatePath extends OneShotBehaviour {
        private int exitValue;

        @Override
        public void action() {
            if (currentLocationIndex == path.getLength()) {
                exitValue = FSMHumanBehaviour.EVENT_DST;
                return;
            }

            DefaultWeightedEdge edge = path.getEdgeList().get(currentLocationIndex);

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

    static class Destination extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println(myAgent.getLocalName() + ": Completed path!");
        }
    }

    class TravelDefault extends OneShotBehaviour {
        @Override
        public void action() {
            Point pt1 = path.getVertexList().get(currentLocationIndex);
            Point pt2 = path.getVertexList().get(currentLocationIndex + 1);
            Edge edge = (Edge) path.getEdgeList().get(currentLocationIndex++);

            String msg = String.format("Moving from [%s] to [%s] by %s", pt1, pt2, edge);

            System.out.println(myAgent.getLocalName() + ":" + msg);
            // ((HumanAgent) myAgent).informMovement(msg);
        }
    }

    class TravelCar extends OneShotBehaviour {
        private int exitValue;

        @Override
        public void action() {
            Point pt1 = path.getVertexList().get(currentLocationIndex);
            Point pt2 = path.getVertexList().get(currentLocationIndex + 1);
            Edge edge = (Edge) path.getEdgeList().get(currentLocationIndex++);

            String msg = String.format("Moving from [%s] to [%s] by %s", pt1, pt2, edge);

            System.out.println(myAgent.getLocalName() + ":" + msg);
            // ((HumanAgent) myAgent).informMovement(msg);

            if (currentLocationIndex == path.getLength()) {
                exitValue = FSMHumanBehaviour.EVENT_DST;
                return;
            }

            edge = (Edge) path.getEdgeList().get(currentLocationIndex);

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

    class StartCarShare extends OneShotBehaviour {
        private int exitValue;

        @Override
        public void action() {
            System.out.println(myAgent.getLocalName() + ": Start Car Share...");
            exitValue = initiator ? FSMHumanBehaviour.EVENT_INITIATE : FSMHumanBehaviour.EVENT_RESPOND;
        }

        @Override
        public int onEnd() {
            return exitValue;
        }
    }

    class CNRHelper extends Behaviour {
        private MessageTemplate messageTemplate;
        private Pair<String, Boolean> done = Pair.of("done", false);
        private boolean busy = false;

        public CNRHelper(Agent a) {
            super(a);
            this.messageTemplate = MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                    MessageTemplate.MatchPerformative(ACLMessage.CFP));
        }

        @Override
        public boolean done() {
            return done.getSecond();
        }

        @Override
        public int onEnd() {
            busy = false;
            done.setSecond(false);
            return super.onEnd();
        }

        @Override
        public void action() {
            if (!busy) {
                ACLMessage cfp = myAgent.receive(messageTemplate);
                if (cfp != null) {
                    Behaviour behaviour = new CarShareContractNetResponder(myAgent, cfp, done);
                    busy = true;
                    myAgent.addBehaviour(behaviour);
                }
            }
        }
    }

    class CNIHelper extends Behaviour {
        private String service;
        private Pair<String, Boolean> done = Pair.of("done", false);
        private boolean busy = false;

        public CNIHelper(Agent a, String service) {
            super(a);
            this.service = service;
        }

        @Override
        public boolean done() {
            return done.getSecond();
        }

        @Override
        public int onEnd() {
            busy = false;
            done.setSecond(false);
            return super.onEnd();
        }

        @Override
        public void action() {
            if (!busy) {
                Pair<ACLMessage, Integer> cfpLengthPair = prepareCNIMessage(this.service);

                Behaviour behaviour = new CarShareContractNetInitiator(myAgent, cfpLengthPair.getFirst(), cfpLengthPair.getSecond(), done);
                busy = true;
                myAgent.addBehaviour(behaviour);
            }
        }
    }


}
