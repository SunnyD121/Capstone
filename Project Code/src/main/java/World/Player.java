package World;

import Core.Shader;
import World.AbstractShapes.Triangle;
import World.WorldObjects.HumanModel;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Created by (User name) on 8/14/2017.
 */
public class Player extends Character {

    private float length = 1.0f;
    private float width = 1.0f;
    private float height = 3.5f;

    private Vector3f groundAdjustment = new Vector3f(0,height/2.0f - 0.25f,0);

    public Player(Vector3f position, Vector3f direction){
        this.position = position;
        this.direction = direction.normalize();
        this.model = new HumanModel(height);
        this.isAffectedByGravity = true;
    }
    public Player(Vector3f direction){
        this.position = new Vector3f(0,0,0);
        this.direction = direction.normalize();
        this.model = new HumanModel(height);
        this.isAffectedByGravity = true;
    }
    public Player(){
        this.position = new Vector3f(0,0,0);
        this.direction = new Vector3f(0,0,1);
        this.model = new HumanModel(height);
        this.isAffectedByGravity = true;
    }

    @Override
    public void init(){
        model.init();
    }

    @Override
    public void render(Shader shader){

        /*
        float directionAngle = (float)Math.acos(direction.normalize().dot(new Vector3f(0,0,1).normalize()));
        if (direction.x < 0)
            directionAngle *= -1.0f;
        */
        Matrix4f m = new Matrix4f().translate(position).mul(Utilities.Utilities.changeDirection(direction));
        m.translate(groundAdjustment);   //second arg to move the player onto the ground, instead of through it.
        //set shaders
        shader.setUniform("ObjectToWorld", m);

        //shader.setUniform("color", new Vector4f(1,0,0,1));      //red
        //define the material's light properties
        Material material = new Material();
        material.Kd = new Vector3f(1,1,1);
        //material.Ks = new Vector3f(1,1,1);    //humans aren't shiny!
        material.setUniforms(shader);

        model.render(shader, m);
    }

    @Override
    public Triangle[] getTriangles() {
        return model.getTriangles();
    }

    public float getLength(){
        return model.getLength();
    }
    public float getWidth() {return model.getWidth();}
    public float getHeight(){return model.getHeight();}
    public Vector3f getGroundAdjustment() {
        return new Vector3f(groundAdjustment);
    }

    public void killPlayer(){
        World.spawnPlayer();
    }
}
