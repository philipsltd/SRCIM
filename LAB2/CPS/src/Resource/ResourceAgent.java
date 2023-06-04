package Resource;

import Utilities.DFInteraction;
import jade.core.Agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import Libraries.IResource;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAException;
import jade.core.Agent;
import jade.imtp.leap.http.HTTPRequest;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;


/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class ResourceAgent extends Agent {

    String id;
    IResource myLib;
    String description;
    String[] associatedSkills;
    String location;
    String skill;
    Float prediction = 0.0F;
    Integer speed = 0;
    Boolean occupied = false;
    ArrayList<String> stationSpeeds = new ArrayList<>();
    List<String> speeds = Arrays.asList("65", "75", "100");
    String randomSpeed;

    @Override
    protected void setup() {

        Object[] args = this.getArguments();
        this.id = (String) args[0];
        this.description = (String) args[1];
        stationSpeeds.addAll(speeds);

        Random random = new Random();
        int randomIndex = random.nextInt(speeds.size());
        randomSpeed = speeds.get(randomIndex);

        //Load hw lib
        try {
            String className = "Libraries." + (String) args[2];
            Class cls = Class.forName(className);
            Object instance;
            instance = cls.newInstance();
            myLib = (IResource) instance;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ResourceAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.location = (String) args[3];

        myLib.init(this);
        this.associatedSkills = myLib.getSkills();
        System.out.println("Resource Deployed: " + this.id + " Executes: " + Arrays.toString(associatedSkills));

        //TO DO: Register in DF with the corresponding skills as services
        try {
            DFInteraction.RegisterInDF(this, associatedSkills, "ResourceAgent");
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }

        this.addBehaviour(new responderREAgent(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
        this.addBehaviour(new responderCFPAgent(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
    }

    private class resourceAgentWaker extends WakerBehaviour {

        public resourceAgentWaker(Agent a, long timeout) {
            super(a, timeout);
        }

        @Override
        protected void onWake() {
            occupied = false;
        }
    }


        // TO DO: Add responder behaviour/s
    private class responderREAgent extends AchieveREResponder {         // aqui vou ter de ter os dois tipos de responders: ao CFP e ao Request

        public responderREAgent(Agent a, MessageTemplate mt){
                super(a, mt);
            }

        @Override
        protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
            System.out.println(myAgent.getLocalName() + ": Processing REQUEST message");
            ACLMessage msg = request.createReply();
            msg.setPerformative(ACLMessage.AGREE);
            return msg;
        }

        @Override
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            ACLMessage execMsg = request.createReply();
            execMsg.setPerformative(ACLMessage.INFORM);
            if(response.getPerformative()==ACLMessage.AGREE){
                System.out.println(myAgent.getLocalName() + ": Preparing result of REQUEST");
                String skill=request.getContent();
                if(skill!=null){
                    myLib.executeSkill(skill);
                    execMsg.setContent("successful");
                }else
                    execMsg.setContent("unsuccessful");
                occupied=false;
                return execMsg;
            }
            return execMsg;
        }
    }

    private class responderCFPAgent extends ContractNetResponder{

        public responderCFPAgent(Agent a, MessageTemplate mt){
            super(a, mt);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
            System.out.println(myAgent.getLocalName() + ": Processing CFP message");

            int station = 0;
            int skillToExec = 0;

            speed = Integer.parseInt(randomSpeed);
            skill = cfp.getContent();

            if((!skill.equals("sk_pick") && !skill.equals("sk_drop") && !skill.equals("sk_move"))){

                switch (id){
                    case "GS1": station = 1; break;
                    case "GS2": station = 2; break;
                    case "GS3": station = 3; break;
                    case "GS4": station = 4; break;
                }

                switch (skill){
                    case "sk_g_a": skillToExec = 1; break;
                    case "sk_g_b": skillToExec = 2; break;
                    case "sk_g_c": skillToExec = 3; break;
                }

                prediction = setAPI.returnPrediction(speed, station, skillToExec);

                ACLMessage msgCFP = cfp.createReply();
                msgCFP.setPerformative(ACLMessage.PROPOSE);
                msgCFP.setContent(String.valueOf(prediction));
                return msgCFP;
            }
            else{
                ACLMessage msgCFP = cfp.createReply();
                msgCFP.setPerformative(ACLMessage.PROPOSE);
                msgCFP.setContent("0.0");
                return msgCFP;
            }
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            System.out.println(myAgent.getLocalName() + ": Preparing result of CFP");
            block(5000);
            ACLMessage msgCFP = cfp.createReply();
            msgCFP.setPerformative(ACLMessage.INFORM);
            msgCFP.setContent(location);
            occupied=true;
            return msgCFP;
        }
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }


}



