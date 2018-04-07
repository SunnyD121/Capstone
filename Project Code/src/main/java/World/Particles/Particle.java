package World.Particles;

import Core.Shader;
import World.SceneEntity;
import World.TriangleMesh;
import org.joml.Matrix4f;
import Utilities.Utilities;
import org.joml.Vector3f;

public abstract class Particle extends SceneEntity{
    //protected Vector3f location;
    //protected Vector3f direction;
    protected Vector3f velocity;
    protected Vector3f acceleration;
    protected float lifespan;
    protected TriangleMesh particleShape;
    //if ever Particle.location and SceneEntity.position are unequal, shoot me please.
    //NOTE: no error checking exists to safeguard against above comment.

    public void update(){
        Vector3f garbage = new Vector3f();
        velocity.add(acceleration.mul(0.1f, garbage));    // * by scalar because update happens per frame. Waaay to fast.
        //location.add(velocity.mul(0.1f, garbage));
        setPosition(getPosition().add(velocity.mul(0.1f, garbage)));
        lifespan -= 1;
    }

    public void render(Shader shader){
        Matrix4f mat = Utilities.setLocationAndDirectionManually(getPosition(), getDirection());
        shader.setUniform("ObjectToWorld", mat);    //move particle from shapeview to location
        particleShape.render();
        shader.setUniform("ObjectToWorld", new Matrix4f());     //this "patches" the ObjectToWorld unintended reuse bug.
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
    /*
    public Vector3f getLocation(){
        return new Vector3f(location);
    }
    */
}
