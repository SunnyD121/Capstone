package World.AbstractShapes;

import org.joml.Vector3f;


/** Note: Triangles are created by the shaders when rendering. It is redundant to create a Triangle object.
 * Instead, this class is to be used solely for storing positional data.
 */
public class Triangle {

    public Vector3f p1,p2,p3;

    public Triangle(Vector3f p1,Vector3f p2,Vector3f p3){
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }
}
