package Transport;

import Utilities.DFInteraction;
import jade.core.Agent;
//import jade.core.behaviours.
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import Libraries.ITransport;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
//import java.util.StringTokenizer;
//import jade.core.behaviours.WakerBehaviour;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class TransportAgent extends Agent {

    String id;
    ITransport myLib;
    String description;
    String[] associatedSkills;
    Boolean occupied=false;

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.description = (String) args[1];

        //Load hw lib
        try {
            String className = "Libraries." + (String) args[2];
            Class cls = Class.forName(className);
            Object instance;
            instance = cls.newInstance();
            myLib = (ITransport) instance;
            System.out.println(instance);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(TransportAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        myLib.init(this);
        this.associatedSkills = myLib.getSkills();
        System.out.println("Transport Deployed: " + this.id + " Executes: " + Arrays.toString(associatedSkills));


        // TO DO: Register in DF
        try {
            DFInteraction.RegisterInDF(this, associatedSkills, "TransportAgent");
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }

        // TO DO: Add responder behaviour/s
        this.addBehaviour(new responderAgent(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
    }

    private class transportWaker extends WakerBehaviour {

        public transportWaker(Agent a, long timeout) {
            super(a, timeout);
        }

        @Override
        protected void onWake() {
            occupied = false;
        }
    }

    private class responderAgent extends AchieveREResponder {

        public responderAgent(Agent a, MessageTemplate mt){
            super(a, mt);
        }

        @Override
        protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
            System.out.println(myAgent.getLocalName() + ": Processing REQUEST message");
            ACLMessage msgRE = request.createReply();

            if(!occupied) {
                occupied=true;  //ISTO FAZ SENTIDO ESTAR AQUI?
                myAgent.addBehaviour(new transportWaker(myAgent, 2000));
                msgRE.setPerformative(ACLMessage.AGREE);
            } else {
                msgRE.setPerformative(ACLMessage.REFUSE);
            }
            return msgRE;
        }

        @Override
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {

            ACLMessage msgRE = request.createReply();
            msgRE.setPerformative(ACLMessage.INFORM);
            
            if(response.getPerformative() == ACLMessage.AGREE) {

                System.out.println(myAgent.getLocalName() + ": Preparing result of REQUEST");
                String content = request.getContent();
                String[] array = content.split(",");

                String source = array[0];
                String destination = array[1];
                String ProductID = array[2];

                myLib.executeMove(source, destination, ProductID);

                msgRE.setContent(destination);
                occupied = false;
            }

            //block(5000);
            return msgRE;
        }
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}


