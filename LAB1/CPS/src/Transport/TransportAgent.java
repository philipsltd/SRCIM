package Transport;

import Utilities.DFInteraction;
import jade.core.Agent;
//import jade.core.behaviours.
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import Libraries.ITransport;
import jade.domain.FIPAException;

/**
 *
 * @author Ricardo Silva Peres <ricardo.peres@uninova.pt>
 */
public class TransportAgent extends Agent {

    String id;
    ITransport myLib;
    String description;
    String[] associatedSkills;

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
            DFInteraction.RegisterInDF(this, id, "TransportAgent");
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
        // TO DO: Add responder behaviour/s
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}
