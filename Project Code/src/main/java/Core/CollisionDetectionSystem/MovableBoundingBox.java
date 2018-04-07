package Core.CollisionDetectionSystem;

import org.joml.Vector3f;

public class MovableBoundingBox extends BoundingBox {

    private Vector3f specialTransform;

    public MovableBoundingBox(Vector3f minPoint, Vector3f maxPoint, Vector3f transform){
        this.specialTransform = (transform == null ? new Vector3f(0) : transform);
        this.minPoint = minPoint.add(this.specialTransform);
        this.maxPoint = maxPoint.add(this.specialTransform);
        setVertexData(this.minPoint, this.maxPoint);

    }

    public void setPosition(Vector3f newCenterPosition){

        Vector3f newMin = new Vector3f(
                newCenterPosition.x - (maxPoint.x - minPoint.x) / 2.0f,
                newCenterPosition.y - (maxPoint.y - minPoint.y) / 2.0f,
                newCenterPosition.z - (maxPoint.z - minPoint.z) / 2.0f);

        Vector3f newMax = new Vector3f(
                newCenterPosition.x + (maxPoint.x - minPoint.x) / 2.0f,
                newCenterPosition.y + (maxPoint.y - minPoint.y) / 2.0f,
                newCenterPosition.z + (maxPoint.z - minPoint.z) / 2.0f);

        newMin.add(specialTransform, this.minPoint);
        newMax.add(specialTransform, this.maxPoint);

        setVertexData(this.minPoint, this.maxPoint);
    }

}
