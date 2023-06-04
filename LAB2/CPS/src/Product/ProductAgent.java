package Product;

import Utilities.DFInteraction;
import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.domain.df;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */

public class ProductAgent extends Agent {    
    
    String id;
    String location;
    String destination;
    Integer prediction;
    String productID;
    AID agentToExec;
    String nextSkill;
    Boolean needTransportation = false;
    Boolean actionComplete = false;
    Boolean occupied = false;
    Integer step = 0;
    ArrayList<String> executionPlan = new ArrayList<>();
    ArrayList<String> stationSpeeds = new ArrayList<>();
    List<String> speeds = Arrays.asList("35", "50", "65", "75", "100");
    // TO DO: Add remaining attributes required for your implementation

    
    @Override
    protected void setup() {

        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.executionPlan = this.getExecutionList((String) args[1]);
        this.location = "Source";
        stationSpeeds.addAll(speeds);

        System.out.println("Product launched: " + this.id + " Requires: " + executionPlan);

        // TO DO: Add necessary behaviour/s for the product to control the flow
        // of its own production

        // --------- setup initiator for CFP ---------

        ACLMessage msgCFP = new ACLMessage(ACLMessage.CFP);
        nextSkill = executionPlan.get(step);
        DFAgentDescription[] dfAgentDescriptions;

        Random random = new Random();
        int randomIndex = random.nextInt(speeds.size());
        String randomSpeed = speeds.get(randomIndex);

        try {
            dfAgentDescriptions = DFInteraction.SearchInDFByName(nextSkill, this);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }

        for(int i = 0; i < dfAgentDescriptions.length; i++){
            msgCFP.addReceiver(dfAgentDescriptions[i].getName());
        }

        String speedAndSkill = "["+randomSpeed+","+nextSkill+"]";
        msgCFP.setContent(speedAndSkill);

        this.addBehaviour(new initiatorCFPAgent(this, msgCFP));
    }

    private class initiatorCFPAgent extends ContractNetInitiator {

        public initiatorCFPAgent(Agent a, ACLMessage msg){
            super(a, msg);
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {

            ACLMessage auxMsg;
            ACLMessage reply;
            String auxDestination;
            Boolean chosen = false;
            int numberResponses = responses.size();

            System.out.println(myAgent.getLocalName() + ": All PROPOSALS received");

            responses.removeIf(e -> ((ACLMessage)e).getPerformative()!=ACLMessage.PROPOSE);

            System.out.println(myAgent.getLocalName() + ": All PROPOSALS cleared");

            for(int i = 1; i < numberResponses; i++){
                auxMsg = (ACLMessage)responses.get(i);
                reply = auxMsg.createReply();
                String msgContent = auxMsg.getContent();
                String cleanContent = msgContent.substring(1, msgContent.length() - 1);
                String[] parts = cleanContent.split(",");

                int auxPrediction = Integer.parseInt(parts[0].trim());
                auxDestination = parts[1].trim();

                if (location.compareTo(auxDestination) == 0){
                    needTransportation = false;
                    chosen = true;
                    destination = auxDestination;
                    agentToExec = auxMsg.getSender();
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    acceptances.add(reply);
                }
                
                // TODO missing the case where the prediction is the lowest

                

                if(!chosen){
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.add(reply);
                }
            }

            System.out.println(myAgent.getLocalName() + ": All PROPOSALS rejected");

            if (!chosen){
                needTransportation = true;
                auxMsg = (ACLMessage) responses.get(0);
                reply = auxMsg.createReply();
                destination = auxMsg.getContent();
                agentToExec = auxMsg.getSender();
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                acceptances.add(reply);
            }

            System.out.println(myAgent.getLocalName() + ": 1 PROPOSAL accepted");

            skipNextResponses();
        }

        @Override
        protected void handleInform(ACLMessage inform){
            System.out.println(myAgent.getLocalName() + ": INFORM message received");
            actionComplete = true;
            destination = inform.getContent();
            productID = myAgent.getLocalName();

            ACLMessage msgRETransport = new ACLMessage(ACLMessage.REQUEST);
            String moveSkill = "sk_move";
            DFAgentDescription[] dfAgentDescriptions;

            try {
                dfAgentDescriptions = DFInteraction.SearchInDFByName(moveSkill, myAgent);     // procurar entre todos os agentes qual faz o transporte
            } catch (FIPAException e) {
                throw new RuntimeException(e);
            }

            msgRETransport.addReceiver(dfAgentDescriptions[0].getName());            // como apenas existe um agv, executar o movimento através dele
            msgRETransport.setContent(location + "," + destination + "," + productID);
            myAgent.addBehaviour(new initiatorREAgentTransport(myAgent, msgRETransport));
        }
    }

    // REQUEST for Transport Agent
    public class initiatorREAgentTransport extends AchieveREInitiator{
        public initiatorREAgentTransport (Agent a, ACLMessage msg){
            super(a, msg);
        }

        @Override
        protected void handleAgree(ACLMessage agree) {
            System.out.println(myAgent.getLocalName() + ": AGREE message received");
        }

        protected void handleRefuse(ACLMessage refuse){
            System.out.println(myAgent.getLocalName() + ": REFUSE message received");

            ACLMessage msgRETransport = new ACLMessage(ACLMessage.REQUEST);
            String moveSkill = "sk_move";
            DFAgentDescription[] dfAgentDescriptions;

            try {
                dfAgentDescriptions = DFInteraction.SearchInDFByName(moveSkill, myAgent);     // procurar entre todos os agentes qual faz o transporte
            } catch (FIPAException e) {
                throw new RuntimeException(e);
            }

            msgRETransport.addReceiver(dfAgentDescriptions[0].getName());            // como apenas existe um agv, executar o movimento através dele
            msgRETransport.setContent(location + "," + destination + "," + productID);
            myAgent.addBehaviour(new initiatorREAgentTransport(myAgent, msgRETransport));
        }

        @Override
        protected void handleInform(ACLMessage inform){   // acabar isto com jeito
            System.out.println(myAgent.getLocalName() + ": INFORM message received");
            actionComplete = true;
            occupied = false;

            location = inform.getContent();

            ACLMessage msgREExecute = new ACLMessage(ACLMessage.REQUEST);

            msgREExecute.setContent(nextSkill);
            msgREExecute.addReceiver(agentToExec);
            myAgent.addBehaviour(new initiatorREAgentResource(myAgent, msgREExecute));
        }
    }

    // REQUEST for Resource Agent
    public class initiatorREAgentResource extends AchieveREInitiator{
        public initiatorREAgentResource (Agent a, ACLMessage msg){
            super(a, msg);
        }

        @Override
        protected void handleAgree(ACLMessage agree) {
            System.out.println(myAgent.getLocalName() + ": AGREE message received");
        }

        @Override
        protected void handleInform(ACLMessage inform){
            System.out.println(myAgent.getLocalName() + ": INFORM message received");
            //System.out.println(executionPlan.size());
            step++;
            if (step < executionPlan.size()) {

                ACLMessage newCFPMsg = new ACLMessage(ACLMessage.CFP);
                System.out.println(step);
                System.out.println(executionPlan.size());
                nextSkill = executionPlan.get(step);
                DFAgentDescription[] dfAgentDescriptions;

                Random random = new Random();
                int randomIndex = random.nextInt(speeds.size());
                String randomSpeed = speeds.get(randomIndex);

                try {
                    dfAgentDescriptions = DFInteraction.SearchInDFByName(nextSkill, myAgent);
                } catch (FIPAException e) {
                    throw new RuntimeException(e);
                }

                for(int i = 0; i < dfAgentDescriptions.length; i++){
                    newCFPMsg.addReceiver(dfAgentDescriptions[i].getName());
                }

                String speedAndSkill = "["+randomSpeed+","+nextSkill+"]";
                newCFPMsg.setContent(speedAndSkill);

                myAgent.addBehaviour(new initiatorCFPAgent(myAgent, newCFPMsg));  //ADAPTAR PARA O PRÓXIMO SKILL A EXECUTAR
            }else
                System.out.println("Plan Achieved");
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


