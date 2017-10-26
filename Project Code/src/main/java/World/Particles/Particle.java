package World.Particles;

//TODO: should have a superclass (interface?) called particle, and have each different particle class describe what kind of particle it is.

import Core.Shader;
import Core.TriangleMesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class Particle {
    protected Vector3f location;
    protected Vector3f velocity;
    protected Vector3f acceleration;
    protected float lifespan;
    protected TriangleMesh particleShape;

    public void update(){
        Vector3f garbage = new Vector3f();
        velocity.add(acceleration.mul(0.1f, garbage));    // * by scalar because update happens per frame. Waaay to fast.
        location.add(velocity.mul(0.1f, garbage));
        lifespan -= 1;
    }

    public void render(Shader shader, Matrix4f O2W){
        O2W.translate(location);
        shader.setUniform("ObjectToWorld", O2W);
        particleShape.render();
    }

    public boolean isDead(){
        if (lifespan <= 0.0)
            return true;
        else
            return false;
    }

    public void run(Shader shader, Matrix4f ObjectToWorld){
        update();
        render(shader, ObjectToWorld);
    }
    public Vector3f getVelocity(){
        return velocity;
    }
    public Vector3f getAcceleration(){
        return acceleration;
    }
    public Vector3f getLocation(){
        return location;
    }
}
