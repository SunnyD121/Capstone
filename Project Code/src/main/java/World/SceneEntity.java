package World;

import World.AbstractShapes.Triangle;
import org.joml.Vector3f;

import java.util.ArrayList;

public abstract class SceneEntity {
    Vector3f position;
    Vector3f direction;
    protected Triangle[] triangles;
    public boolean isAffectedByGravity = false;
    public boolean isOnGround = true;

    public Vector3f getPosition() {
        return new Vector3f(position);
    }
    public void setPosition(Vector3f newPosition){this.position = new Vector3f(newPosition);}

    public Vector3f getDirection() {
        return new Vector3f(direction);
    }
    public void setDirection(Vector3f newDirection) {
        this.direction = new Vector3f(newDirection);
    }

    public void moveDistance(Vector3f distance){ position.add(distance);}

    public abstract float getLength();
    public abstract float getHeight();
    public abstract float getWidth();
    public abstract Triangle[] getTriangles();

}
