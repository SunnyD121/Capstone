package World.Particles;

import Shapes.Cylinder;
import org.joml.Vector3f;

public class Laser extends Particle{

    public Laser (Vector3f emitterLocation, Vector3f spewDirection){
        particleShape = new Cylinder(0.05f, 1.0f);
        particleShape.init();

        //TODO: delete the new?
        location = new Vector3f(emitterLocation);   //starts at the Emitter's location
        direction = new Vector3f(spewDirection);

        velocity = new Vector3f(direction.mul(75.0f, new Vector3f()));
        acceleration = new Vector3f(0);

        lifespan = 100;
    }

}
