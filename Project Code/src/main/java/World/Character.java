package World;

import Core.Shader;
import Core.TriangleMesh;
import org.joml.Vector3f;

/**
 * Created by (User name) on 8/14/2017.
 */
public abstract class Character {

    TriangleMesh geometry;
    Vector3f position;
    Vector3f direction;

    public abstract void init();

    public abstract void render(Shader shader);

    public Vector3f getPosition(){
        return position;
    }

    public void setPosition(Vector3f pos){
        position = pos;
    }

    public Vector3f getDirection(){
        return direction;
    }

    public void setDirection(Vector3f dir){
        direction = dir;
    }

    public void move(float distance){
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
