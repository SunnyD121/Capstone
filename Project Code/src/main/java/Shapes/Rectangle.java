package Shapes;

import Core.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by (User name) on 8/12/2017.
 */
public class Rectangle extends TriangleMesh {

    private Vector3f[] p;

    //points must be counterclockwise!
    public Rectangle(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4){
        p = new Vector3f[4];
        setPosition(p1, p2, p3, p4);
    }

    @Override
    public void init(){
        if (vao != 0) return;

        float[] points = new float[12];
        float[] normals = new float[12];
        int[] elements = new int[6];

        Vector3f a = new Vector3f();
        Vector3f b = new Vector3f();
        p[1].sub(p[0], a);
        p[2].sub(p[0], b);
        Vector3f norm = a.cross(b).normalize();

        for(int i = 0; i < 4; i++){
            points[i * 3 + 0] = p[i].x;
            points[i * 3 + 1] = p[i].y;
            points[i * 3 + 2] = p[i].z;
            normals[i * 3 + 0] = norm.x;
            normals[i * 3 + 1] = norm.y;
            normals[i * 3 + 2] = norm.z;
        }
        elements[0] = 0;
        elements[1] = 1;
        elements[2] = 2;
        elements[3] = 0;
        elements[4] = 2;
        elements[5] = 3;

        float[] tex = {0.0f, 0.0f, 5.0f, 0.0f, 5.0f, 5.0f, 0.0f, 5.0f};

        FloatBuffer p = GLBuffers.newDirectFloatBuffer(points);
        FloatBuffer n = GLBuffers.newDirectFloatBuffer(normals);
        IntBuffer e = GLBuffers.newDirectIntBuffer(elements);
        FloatBuffer t = GLBuffers.newDirectFloatBuffer(tex);

        initGpuVertexArrays(e, p, n, null, t);
    }



    private void setPosition(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4){
        p[0] = p1;
        p[1] = p2;
        p[2] = p3;
        p[3] = p4;
    }
}
