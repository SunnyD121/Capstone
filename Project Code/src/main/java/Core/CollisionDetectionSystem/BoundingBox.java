package Core.CollisionDetectionSystem;

import Utilities.Utilities;
import org.joml.Vector3f;

public abstract class BoundingBox {
    Vector3f minPoint;
    Vector3f maxPoint;
    Vector3f[] vertices;

    public boolean overlaps(BoundingBox other){
        return (maxPoint.x > other.minPoint.x &&
                minPoint.x < other.maxPoint.x &&
                maxPoint.y > other.minPoint.y &&
                minPoint.y < other.maxPoint.y &&
                maxPoint.z > other.minPoint.z &&
                minPoint.z < other.maxPoint.z);
    }

    public boolean contains(BoundingBox other){
        return (other.minPoint.x >= minPoint.x &&
                other.maxPoint.x <= maxPoint.x &&
                other.minPoint.y >= minPoint.y &&
                other.maxPoint.y <= maxPoint.y &&
                other.minPoint.z >= minPoint.z &&
                other.maxPoint.z <= maxPoint.z);
    }

    //creates a new Bounding Box that is the collective space of 'this' and 'other'
    public BoundingBox merge(BoundingBox other){
        return new FixedBoundingBox(
                new Vector3f(Math.min(minPoint.x, other.minPoint.x),Math.min(minPoint.y, other.minPoint.y),Math.min(minPoint.z, other.minPoint.z)),
                new Vector3f(Math.max(maxPoint.x, other.maxPoint.x),Math.max(maxPoint.y, other.maxPoint.y),Math.max(maxPoint.z, other.maxPoint.z)),
                null
        );
    }
    
    //creates a new Bounding Box that is the space defined by 'this' and 'other's overlap
    public BoundingBox intersection(BoundingBox other){
        return new FixedBoundingBox(
                new Vector3f(Math.max(minPoint.x, other.minPoint.x),Math.max(minPoint.y, other.minPoint.y),Math.max(minPoint.z, other.minPoint.z)),
                new Vector3f(Math.min(maxPoint.x, other.maxPoint.x),Math.min(maxPoint.y, other.maxPoint.y),Math.min(maxPoint.z, other.maxPoint.z)),
                null
        );
    }

    public Vector3f[] getVertexData(){
        return vertices;
    }

    public void setVertexData(Vector3f newMin, Vector3f newMax){
        //Points are clockwise. Works for some reason.
        Vector3f p1 = new Vector3f(newMin);
        Vector3f p2 = new Vector3f(newMin.x, newMin.y, newMax.z);
        Vector3f p3 = new Vector3f(newMax.x, newMin.y, newMax.z);
        Vector3f p4 = new Vector3f(newMax.x, newMin.y, newMin.z);
        Vector3f p5 = new Vector3f(newMin.x, newMax.y, newMin.z);
        Vector3f p6 = new Vector3f(newMin.x, newMax.y, newMax.z);
        Vector3f p7 = new Vector3f(newMax);
        Vector3f p8 = new Vector3f(newMax.x, newMax.y, newMin.z);

        vertices = new Vector3f[]{p1,p2,p3,p4,p5,p6,p7,p8};
    }

    public float getLength(){
        return maxPoint.x - minPoint.x;
    }
    public float getHeight(){
        return maxPoint.y - minPoint.y;
    }
    public float getWidth(){
        return maxPoint.z - minPoint.z;
    }

    public Vector3f getMaxPoint() {
        return maxPoint;
    }

    public Vector3f getMinPoint() {
        return minPoint;
    }

    public Vector3f getCenterPoint() {
        Vector3f temp = new Vector3f();
        maxPoint.sub(minPoint, temp);
        temp.div(2.0f);
        temp.add(minPoint);
        return temp;
    }
}
