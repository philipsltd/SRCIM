package Product;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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
    
    /*private int currentStep = 0; // the current step in the execution plan
    private boolean isPaused = false; // whether the product is currently paused
    private ArrayList<AID> skillAgents = new ArrayList<>(); // list of skill agents for this product*/
    
    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.executionPlan = this.getExecutionList((String) args[1]);
        System.out.println("Product launched: " + this.id + " Requires: " + executionPlan);

        // TO DO: Add necessary behaviour/s for the product to control the flow
        // of its own production
        
        /*SequentialBehaviour productionBehaviour = new SequentialBehaviour();
        for (String skill : executionPlan) {
            productionBehaviour.addSubBehaviour(new CFPBehaviourSkill(this, "Skill: " + skill));
        }*/


        // setup initiator for CFP
        ACLMessage msgCFP = new ACLMessage(ACLMessage.CFP);
        /*msgCFP.setContent("Request skill agents");
        msgCFP.setOntology("skill-ontology");
        for(String skill: executionPlan){
            msgCFP.addReceiver(new AID(skill, AID.ISLOCALNAME));
        }
        addBehaviour((new initiatorCFPAgent(this, msgCFP)));*/
        msgCFP.addReceiver(new AID("responder", false));
        this.addBehaviour(new initiatorCFPAgent(this, msgCFP));


        // setup initiator for REQUEST
        ACLMessage msgRE = new ACLMessage(ACLMessage.REQUEST);
        /*msgRE.setContent("Star production");
        msgRE.setOntology("production-ontology");
        for(AID skillAgent:skillAgents){
            msgRE.addReceiver(skillAgent);
        }
        addBehaviour(new initiatorREAgent(this, msgRE));*/

        msgRE.addReceiver(new AID("responder", false));
        this.addBehaviour(new initiatorCFPAgent(this, msgRE));


        // setup sequential behaviour
        SequentialBehaviour sb = new SequentialBehaviour();
        /*sb.addSubBehaviour(productionBehaviour);
        sb.addSubBehaviour(new OneShotBehaviour() {
            public void action(){
                System.out.println("Production Complete!");
            }
        });*/
        
        
        //sb.addSubBehaviour(new RequestSkillBehaviour(this, "CFP Skill"));
        //sb.addSubBehaviour(new RequestTransportBehaviour(this, "REQUEST Transport"));
        //sb.addSubBehaviour(new ExecutePlanBehaviour(this, "REQUEST Skill"));
        this.addBehaviour(sb);
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

    /*private class simpleBeh extends SimpleBehaviour {

        private boolean finished = false;
        int step = 0;
        String printOut;

        public simpleBeh(Agent a, String prtOut) {
            super(a);
            this.printOut = prtOut;
        }

        @Override
        public void action() {
            System.out.println("SimpleBehaviour: SubBehaviour:" + printOut + " - step: " + ++step);
        }

        @Override
        public boolean done() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }*/

    private class CFPBehaviourSkill extends SimpleBehaviour {

        private boolean finished = false;
        int step = 0;
        String printOut;

        public CFPBehaviourSkill(Agent a, String prtOut) {
            super(a);
            this.printOut = prtOut;
        }

        @Override
        public void action() {
            //System.out.println("SimpleBehaviour: SubBehaviour:" + printOut + " - step: " + ++step);
        }

        @Override
        public boolean done() {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            finished = true;
            return finished;
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


