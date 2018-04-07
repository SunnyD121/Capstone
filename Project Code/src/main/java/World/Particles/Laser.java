package World.Particles;

import World.Shapes.Cylinder;
import org.joml.Vector3f;

public class Laser extends Particle{

    public Laser (Vector3f emitterLocation, Vector3f spewDirection){
        particleShape = new Cylinder(0.05f, 1.0f);
        particleShape.init();

        setPosition(emitterLocation);   //starts at the Emitter's location
        setDirection(spewDirection);

        velocity = new Vector3f(getDirection().mul(75.0f, new Vector3f()));
        acceleration = new Vector3f(0);

        lifespan = 100;
    }

    @Override
    public float getLength() {
        return particleShape.getLength();
    }

    @Override
    public float getHeight() {
        return particleShape.getHeight();
    }

    @Override
    public float getWidth() {
        return particleShape.getWidth();
    }
}
