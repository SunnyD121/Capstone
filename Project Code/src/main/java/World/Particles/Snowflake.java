package World.Particles;

import Shapes.Cube;
import org.joml.Vector3f;

public class Snowflake extends Particle {

    public Snowflake(Vector3f loc){
        particleShape = new Cube(1.0f); //TODO: change to triangles
        particleShape.init();
        location = new Vector3f(loc);
        velocity = new Vector3f(((float)Math.random()-0.5f)*2 ,((float)Math.random()-1)*2,((float)Math.random()-0.5f)*2);
        acceleration = new Vector3f(0,-0.1f,0);
        lifespan = 255;
    }

}
