package Core;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Created by (User name) on 8/15/2017.
 */
public class Camera {

    private Vector3f position;
    private Vector3f u, v, n;   //Camera's x, y, and z axes, respectively.
    private float fovy;
    private float nearPlane, farPlane;
    private float aspect;
    //for ortho projections:
    private float nearX,nearY,nearZ,farX,farY,farZ;
    private Vector3f lookAt, up;

    private ProjectionType pType;

    public enum ProjectionType{
        PERSPECTIVE, ORTHO
    }

    public Camera(ProjectionType type){
        setViewVolume(45.0f, 1.0f, 1.0f, 100000.0f);   //far is the max render distance!
        u = new Vector3f();
        v = new Vector3f();
        n = new Vector3f();

        pType = type;
    }

    public void setOrtho(float minX, float maxX, float minY, float maxY, float minZ, float maxZ){
        nearX = minX;
        nearY = minY;
        nearZ = minZ;
        farX = maxX;
        farY = maxY;
        farZ = maxZ;
    }

    public Matrix4f getProjectionMatrix(){
        Matrix4f m = new Matrix4f();
        if (pType == ProjectionType.PERSPECTIVE)
            m.perspective((float)Math.toRadians(fovy), aspect, nearPlane, farPlane);
        else //pType == ORTHO
            m.ortho(nearX,farX,nearY,farY,nearZ,farZ);
        return m;
    }

    public Matrix4f getViewMatrix(){
        if (pType != ProjectionType.ORTHO) {
            Matrix4f rotation = new Matrix4f(
                    u.x, u.y, u.z, 0.0f,
                    v.x, v.y, v.z, 0.0f,
                    n.x, n.y, n.z, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f
            );
            rotation.transpose();

            Matrix4f translation = new Matrix4f(
                    1.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 1.0f, 0.0f,
                    -position.x, -position.y, -position.z, 1.0f
            );

            Matrix4f destination = new Matrix4f();
            destination = rotation.mul(translation, destination);

            return destination;
        }
        else {//pType = ORTHO
            return new Matrix4f().lookAt(position, lookAt, up);
        }
    }

    public void setLookAtMatrix(Vector3f position, Vector3f lookAtPosition, Vector3f up){
        //TODO: calculate u,v,n based on these passed arguments.
        this.position = position;
        lookAt = lookAtPosition;
        this.up = up;
    }

    public void setViewVolume(float fov, float aspect, float near, float far){
        fovy = fov;
        this.aspect = aspect;
        nearPlane = near;
        farPlane = far;
    }

    public void orient(final Vector3f location, final Vector3f lookAt, final Vector3f up){
        location.sub(lookAt,n); //makes n face away from the camera (-z dir)
        n.normalize();
        up.cross(n, u);
        u.normalize();
        n.cross(u, v);
        v.normalize();
        position = location;
        this.lookAt = lookAt;
        this.up = up;
    }

    public void slide(final Vector3f distance){
        position.x += distance.x;
        position.y += distance.y;
        position.z += distance.z;
    }

    public void rotate(float angle, final Vector3f axis){
        //convert to radians
        angle = (float)Math.toRadians(angle);

        //rotate the camera's u, v, and n
        u.rotateAxis(-angle, axis.x, axis.y, axis.z, u);
        v.rotateAxis(-angle, axis.x, axis.y, axis.z, v);
        n.rotateAxis(-angle, axis.x, axis.y, axis.z, n);
    }

    public void pitch(float angle){
        Vector3f temp1 = new Vector3f();
        Vector3f temp2 = new Vector3f();
        //TODO: make sure this math does what it says it does.

        v.mul((float)Math.cos(angle), temp1);
        n.mul((float)Math.sin(angle), temp2);
        temp1.add(temp2);
        v = new Vector3f(temp1);

        n.mul((float)Math.cos(angle), temp1);
        v.mul((float)Math.sin(angle), temp2);
        temp1.sub(temp2);
        n = new Vector3f(temp1);

    }

    public Vector3f getPosition(){
        return new Vector3f(position);
    }

    //NOTE: returning new Vectors because these are getters. Using them should NOT alter the actual variables. But joml...
    public Vector3f getU(){
        return new Vector3f(u);
    }

    public Vector3f getV(){
        return new Vector3f(v);
    }

    public Vector3f getN(){
        return new Vector3f(n);
    }

    public void updateAspect(float newAspect){
        this.aspect = newAspect;
    }

}
