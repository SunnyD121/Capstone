package World.WorldObjects;

import World.AbstractShapes.Rectangle;
import World.AbstractShapes.Triangle;
import World.TriangleMesh;
import org.joml.Vector3f;

public class Ground extends TriangleMesh {

    private Rectangle r;
    private Vector3f minCorner, maxCorner;

    public Ground(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4){
        r = new Rectangle(v1,v2,v3,v4);
        setPosition(r.getPosition());

        Vector3f center = r.getPosition();
        if (v1.x < center.x && v1.z <= center.z) minCorner = new Vector3f(v1);
        else if (v2.x < center.x && v2.z <= center.z) minCorner = new Vector3f(v2);
        else if (v3.x < center.x && v3.z <= center.z) minCorner = new Vector3f(v3);
        else if (v4.x < center.x && v4.z <= center.z) minCorner = new Vector3f(v4);
        else { System.err.println("I'm confused."); System.exit(-1); }
        if (v1.x >= center.x && v1.z > center.z) maxCorner = new Vector3f(v1);
        else if (v2.x >= center.x && v2.z > center.z) maxCorner = new Vector3f(v2);
        else if (v3.x >= center.x && v3.z > center.z) maxCorner = new Vector3f(v3);
        else if (v4.x >= center.x && v4.z > center.z) maxCorner = new Vector3f(v4);
        else { System.err.println("I'm confused."); System.exit(-1); }
        if (maxCorner.equals(minCorner)) { System.err.println("Why is the ground the zero vector?"); System.exit(-1); }
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

    public Vector3f getMinCorner(){
        return new Vector3f(minCorner);
    }

    public Vector3f getMaxCorner(){
        return new Vector3f(maxCorner);
    }
}
