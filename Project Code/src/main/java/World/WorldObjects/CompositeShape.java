package World.WorldObjects;


import Core.Shader;
import World.AbstractShapes.Triangle;
import World.SceneEntity;
import World.TriangleMesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;

/**
 * Essentially a label for SceneEntity objects that are comprised of simpler objects.
 * For example, a 'Tree' object is a Cone and a Cylinder stacked one on the other.
 */
public abstract class CompositeShape extends TriangleMesh {
    protected HashMap<SceneEntity, Matrix4f> transformMap = new HashMap<>();


    Triangle[] transformTriangleArray(Triangle[] triangles, Matrix4f mat){
        Triangle[] temp = new Triangle[triangles.length];
        for(int i = 0; i < triangles.length; i++) temp[i] = triangles[i];
        for (int i = 0; i < temp.length; i++){
            Vector4f p1 = new Vector4f(temp[i].p1.x, temp[i].p1.y, temp[i].p1.z, 1);
            Vector4f p2 = new Vector4f(temp[i].p2.x, temp[i].p2.y, temp[i].p2.z, 1);
            Vector4f p3 = new Vector4f(temp[i].p3.x, temp[i].p3.y, temp[i].p3.z, 1);
            p1.mul(mat);
            p2.mul(mat);
            p3.mul(mat);
            Vector3f newP1 = new Vector3f(p1.x, p1.y, p1.z);
            Vector3f newP2 = new Vector3f(p2.x, p2.y, p2.z);
            Vector3f newP3 = new Vector3f(p3.x, p3.y, p3.z);
            temp[i] = new Triangle(newP1, newP2, newP3);
        }
        return temp;
    }
    public abstract void render();
}
