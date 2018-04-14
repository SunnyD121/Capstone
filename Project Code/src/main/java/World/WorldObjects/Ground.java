package World.WorldObjects;

import World.AbstractShapes.Rectangle;
import World.AbstractShapes.Triangle;
import World.TriangleMesh;
import org.joml.Vector3f;

public class Ground extends TriangleMesh {

    private Rectangle r;

    public Ground(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4){
        r = new Rectangle(v1,v2,v3,v4);
        setPosition(r.getPosition());
    }

    @Override
    public Triangle[] getTriangles() {
        return r.getTriangles();
    }

    @Override
    public float getLength() {
        return r.getLength();
    }

    @Override
    public float getHeight() {
        return r.getHeight();
    }

    @Override
    public float getWidth() {
        return r.getWidth();
    }

    @Override
    public void init() {
        r.init();
    }

    @Override
    public void render(){
        r.render();
    }
}
