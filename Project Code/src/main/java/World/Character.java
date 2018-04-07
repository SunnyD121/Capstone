package World;

import Core.Shader;
import World.Shapes.CharacterModel;
import org.joml.Vector3f;

/**
 * Created by (User name) on 8/14/2017.
 */
public abstract class Character extends SceneEntity{

    CharacterModel model;
    //Vector3f direction;   //handled in superclass
    //Vector3f position;    //handled in superclass

    public abstract void init();

    public abstract void render(Shader shader);

    public Vector3f getPosition(){
        return new Vector3f(position);
    }

    public void setPosition(Vector3f pos){
        position = new Vector3f(pos);
    }

    public Vector3f getDirection(){
        return new Vector3f(direction);
    }

    public void setDirection(Vector3f dir){
        direction = new Vector3f(dir);
    }

    public void move(float distance){
        model.animate();    //BUG: Alters static data from a class instance.
        Vector3f temp = new Vector3f();
        direction.mul(distance, temp);
        position.add(temp, position);

    }
    public void moveY(float distance){
        position.y += distance;
    }

    public void turn(float angle){
        direction.rotateY((float)Math.toRadians(angle), direction);
    }

}
