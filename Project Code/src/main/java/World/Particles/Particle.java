package World.Particles;

import Core.Shader;
import Core.TriangleMesh;
import org.joml.Matrix4f;
import Utilities.Utilities;
import org.joml.Vector3f;

public abstract class Particle {
    protected Vector3f location;
    protected Vector3f direction;
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

    public void render(Shader shader){
        Matrix4f mat = Utilities.setLocationAndDirectionManually(location, direction);
        shader.setUniform("ObjectToWorld", mat);    //move particle from shapeview to location
        particleShape.render();
    }

    public boolean isDead(){
        if (lifespan <= 0.0) return true;
        else return false;
    }

    public void run(Shader shader){
        update();
        render(shader);
    }
    public Vector3f getVelocity(){
        return new Vector3f(velocity);
    }
    public Vector3f getAcceleration(){
        return new Vector3f(acceleration);
    }
    public Vector3f getLocation(){
        return new Vector3f(location);
    }
}
