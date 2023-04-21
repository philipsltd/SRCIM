package Product;

import jade.core.Agent;
import jade.domain.df;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;

import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class ProductAgent extends Agent {    
    
    String id;
    ArrayList<String> executionPlan = new ArrayList<>();
    // TO DO: Add remaining attributes required for your implementation
    
    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.executionPlan = this.getExecutionList((String) args[1]);
        System.out.println("Product launched: " + this.id + " Requires: " + executionPlan);

        // TO DO: Add necessary behaviour/s for the product to control the flow
        // of its own production

        // setup initiator for CFP
        ACLMessage msgCFP = new ACLMessage(ACLMessage.CFP);
        msgCFP.addReceiver(new AID("responder", false));
        this.addBehaviour(new initiatorCFPAgent(this, msgCFP));

        // setup initiator for REQUEST
        ACLMessage msgRE = new ACLMessage(ACLMessage.REQUEST);
        msgRE.addReceiver(new AID("responder", false));
        this.addBehaviour(new initiatorCFPAgent(this, msgRE));

    }
    private class initiatorCFPAgent extends ContractNetInitiator {
        public initiatorCFPAgent(Agent a, ACLMessage msg){
            super(a, msg);
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            System.out.println(myAgent.getLocalName() + ": All PROPOSALS received");
            ACLMessage auxMsg = (ACLMessage) responses.get(0);
            ACLMessage reply = auxMsg.createReply();
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            acceptances.add(reply);
        }

        @Override
        protected void handleInform(ACLMessage inform){
            System.out.println(myAgent.getLocalName() + ": INFORM message received");
        }
    }

    private class initiatorREAgent extends AchieveREInitiator{
        public initiatorREAgent (Agent a, ACLMessage msg){
            super(a, msg);
        }

        @Override
        protected void handleAgree(ACLMessage agree) {
            System.out.println(myAgent.getLocalName() + ": AGREE message received");
        }

        @Override
        protected void handleInform(ACLMessage inform){
            System.out.println(myAgent.getLocalName() + ": INFORM message received");
        }
    }

    @Override
    protected void takeDown() {
        super.takeDown(); //To change body of generated methods, choose Tools | Templates.
    }
    
    private ArrayList<String> getExecutionList(String productType){
        switch(productType){
            case "A": return Utilities.Constants.PROD_A;
            case "B": return Utilities.Constants.PROD_B;
            case "C": return Utilities.Constants.PROD_C;
        }
        return null;
    }
}


