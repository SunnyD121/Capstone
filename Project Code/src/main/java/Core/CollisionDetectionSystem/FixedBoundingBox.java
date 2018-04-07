package Core.CollisionDetectionSystem;


import org.joml.Vector3f;

// Inner class describing the collision box.
public class FixedBoundingBox extends BoundingBox{

    private Vector3f specialTransform;

    public FixedBoundingBox(Vector3f minPoint, Vector3f maxPoint, Vector3f specialTransform){
        this.specialTransform = (specialTransform == null ? new Vector3f(0) : specialTransform);
        this.minPoint = minPoint.add(this.specialTransform);
        this.maxPoint = maxPoint.add(this.specialTransform);
        setVertexData(this.minPoint, this.maxPoint);
    }
}

