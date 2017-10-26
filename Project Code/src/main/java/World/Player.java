package World;

import Core.Shader;
import Core.Utilities;
import Shapes.AbstractHuman;
import Shapes.RectangularPrism;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Created by (User name) on 8/14/2017.
 */
public class Player extends Character{

    private float length = 1.0f;
    private float width = 1.0f;
    private float height = 3.5f;

    public Player(Vector3f position, Vector3f direction){
        this.position = position;
        this.direction = direction.normalize();
        this.model = new AbstractHuman(height);
    }
    public Player(Vector3f direction){
        this.position = new Vector3f(0,0,0);
        this.direction = direction.normalize();
        this.model = new AbstractHuman(height);
    }
    public Player(){
        this.position = new Vector3f(0,0,0);
        this.direction = new Vector3f(0,0,1);
        this.model = new AbstractHuman(height);
    }

    @Override
    public void init(){
        //TODO: this.   EDIT: ???
        model.init();
    }

    @Override
    public void render(Shader shader){


        float directionAngle = (float)Math.acos(direction.normalize().dot(new Vector3f(0,0,1).normalize()));
        if (direction.x < 0)
            directionAngle *= -1.0f;

        Matrix4f m = new Matrix4f().translate(position).mul(new Matrix4f().rotate(directionAngle, new Vector3f(0,1,0)));
        m.translate(0,height/2.0f - 0.25f,0);   //second arg to move the player onto the ground, instead of through it.
        //set shaders
        shader.setUniform("ObjectToWorld", m);

        //shader.setUniform("color", new Vector4f(1,0,0,1));      //red
        //define the material's light properties
        Material material = new Material();
        material.Kd = new Vector3f(1,1,1);
        //material.Ks = new Vector3f(1,0,0);    //humans aren't shiny!
        material.setUniforms(shader);

        model.render(shader, m);
    }



}
