import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        // Create a new Vert.x instance
        Vertx vertx = Vertx.vertx();
        
        // Deploy an instance of MainVerticle as a verticle in the Vert.x instance
        vertx.deployVerticle(new MainVerticle());
    }
}
