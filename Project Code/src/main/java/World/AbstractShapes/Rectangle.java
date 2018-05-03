package World.AbstractShapes;

import World.TriangleMesh;
import Utilities.Utilities;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by (User name) on 8/12/2017.
 */
public class Rectangle extends TriangleMesh {

    private Vector3f[] p;
    private float tex[] = {0.0f, 0.0f, 5.0f, 0.0f, 5.0f, 5.0f, 0.0f, 5.0f};

    //points must be counterclockwise!
    public Rectangle(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4){
        p = new Vector3f[4];
        setVertexData(p1, p2, p3, p4);
        setPosition(interpolateCenter());
    }
    public Rectangle(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, float[] textureCoordinates){
        p = new Vector3f[4];
        setVertexData(p1, p2, p3, p4);
        setPosition(interpolateCenter());
        this.tex = textureCoordinates;
    }

    @Override
    public float getLength() {
        return Utilities.dist(p[0], p[1]);
    }

    @Override
    public float getHeight() {
        return Utilities.dist(p[1],p[2]);
    }

    @Override
    public float getWidth() {
        return 0;
    }

    @Override
    public void init(){
        if (vao != 0) return;

        float[] points = new float[12];
        float[] normals = new float[12];
        int[] indices = new int[6];

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
        indices[0] = 0;
        indices[1] = 1;
        indices[2] = 2;
        indices[3] = 0;
        indices[4] = 2;
        indices[5] = 3;

        //Triangles, for use in collision detection
        triangles = new Triangle[indices.length/3];
        for (int i = 0; i < triangles.length; i++){
            triangles[i] = new Triangle(
                    new Vector3f(points[3*indices[3*i]], points[3*indices[3*i]+1], points[3*indices[3*i]+2]),
                    new Vector3f(points[3*indices[3*i+1]], points[3*indices[3*i+1]+1], points[3*indices[3*i+1]+2]),
                    new Vector3f(points[3*indices[3*i+2]], points[3*indices[3*i+2]+1], points[3*indices[3*i+2]+2])
            );
        }


        FloatBuffer p = GLBuffers.newDirectFloatBuffer(points);
        FloatBuffer n = GLBuffers.newDirectFloatBuffer(normals);
        IntBuffer e = GLBuffers.newDirectIntBuffer(indices);
        FloatBuffer t = GLBuffers.newDirectFloatBuffer(tex);

        initGpuVertexArrays(e, p, n, null, t);
    }

    @Override
    public Triangle[] getTriangles() {
        return triangles;
    }

    private void setVertexData(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4){
        p[0] = p1;
        p[1] = p2;
        p[2] = p3;
        p[3] = p4;
    }

    private Vector3f interpolateCenter(){
        float centerX = (p[0].x < p[2].x ? p[0].x : p[2].x) + (Math.abs(p[0].x - p[2].x) / 2.0f);
        float centerY = (p[0].y < p[2].y ? p[0].y : p[2].y) + (Math.abs(p[0].y - p[2].y) / 2.0f);
        float centerZ = (p[0].z < p[2].z ? p[0].z : p[2].z) + (Math.abs(p[0].z - p[2].z) / 2.0f);

        return new Vector3f(centerX, centerY, centerZ);
    }
}
