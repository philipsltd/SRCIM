import jade.core.Agent;

public class TestAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("Hello world from " + this.getLocalName());
    }
}
