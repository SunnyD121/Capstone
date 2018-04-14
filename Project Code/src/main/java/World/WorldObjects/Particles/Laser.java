package World.WorldObjects.Particles;

import Core.CollisionDetectionSystem.CollisionDetectionSystem;
import World.AbstractShapes.Cylinder;
import World.AbstractShapes.Triangle;
import org.joml.Vector3f;

public class Laser extends Particle{

    private Vector3f previousLocation;

    public Laser (Vector3f emitterLocation, Vector3f spewDirection){
        particleShape = new Cylinder(0.05f, 1.0f);
        particleShape.init();

        setPosition(emitterLocation);   //starts at the Emitter's location
        setDirection(spewDirection);

        velocity = new Vector3f(getDirection().mul(75.0f, new Vector3f()));
        acceleration = new Vector3f(0);

        Vector3f offset = new Vector3f();
        emitterLocation.sub(getPosition().sub(velocity.mul(0.1f, new Vector3f()), new Vector3f()), offset);
        previousLocation = new Vector3f(offset);

        lifespan = 100;
        super.moreConstruction();
    }

    @Override
    public void update(){
        previousLocation = getPosition();
        super.update();
    }

    public Vector3f getprevPosition() {
        return new Vector3f(previousLocation);
    }

    @Override
    public void generateCollisionBox() {
        float t = transform.m00();
        float length = (1-t) * particleShape.getHeight() + (t) * particleShape.getWidth();
        float width = (1-t) * particleShape.getWidth() + (t) * particleShape.getHeight();
        float height = particleShape.getLength();
        CollisionDetectionSystem.getInstance().addCollisionBox(this, this.getPosition(), length, height, width, null);
    }

    @Override
    public Triangle[] getTriangles() {
        return particleShape.getTriangles();
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
