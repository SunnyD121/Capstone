package World.AbstractShapes;

import World.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
/**
 * Created by (User name) on 8/10/2017.
 */
public class Disk extends TriangleMesh {
    private int slices;
    private float radius;

    public Disk(float radius, int slices){
        this.radius = radius;
        this.slices = slices;
    }

    @Override
    public float getLength() {
        return radius * 2.0f;
    }

    @Override
    public float getHeight() {
        return radius * 2.0f;
    }

    @Override
    public float getWidth() {
        return 0;
    }

    @Override
    public void init(){
        if (vao != 0) return;

        float[] points = new float[3 * (slices + 1)];
        float[] normals = new float[3 * (slices + 1)];
        int[] indices = new int[3 * slices];

        float factor = (2 * (float)Math.PI) / slices;         //Be wary the double -> float truncation
        for(int i=0; i<slices; i++){
            float angle = factor * i;
            points[i*3+0] = radius * (float)Math.cos(angle);     //Be wary the double -> float truncation
            points[i*3+1] = radius * (float)Math.sin(angle);     //Be wary the double -> float truncation
            points[i*3+2] = 0.0f;

            normals[i*3+0] = 0.0f;
            normals[i*3+1] = 0.0f;
            normals[i*3+2] = 1.0f;

            indices[i*3+0] = i;
            indices[i*3+1] = (i +1) % slices;
            indices[i*3+2] = slices;
        }
        //Add center point
        points[slices*3+0] = 0.0f;
        points[slices*3+1] = 0.0f;
        points[slices*3+2] = 0.0f;
        normals[slices*3+0] = 0.0f;
        normals[slices*3+1] = 0.0f;
        normals[slices*3+2] = 1.0f;

        //Triangles, for use in collision detection
        triangles = new Triangle[indices.length/3];
        for (int i = 0; i < triangles.length; i++){
            triangles[i] = new Triangle(
                    new Vector3f(points[3*indices[3*i]], points[3*indices[3*i]+1], points[3*indices[3*i]+2]),
                    new Vector3f(points[3*indices[3*i+1]], points[3*indices[3*i+1]+1], points[3*indices[3*i+1]+2]),
                    new Vector3f(points[3*indices[3*i+2]], points[3*indices[3*i+2]+1], points[3*indices[3*i+2]+2])
            );
        }

        IntBuffer elements = GLBuffers.newDirectIntBuffer(indices);
        FloatBuffer vertices = GLBuffers.newDirectFloatBuffer(points);
        FloatBuffer normalss = GLBuffers.newDirectFloatBuffer(normals);

        initGpuVertexArrays(elements, vertices, normalss,null, null);
    }

    @Override
    public Triangle[] getTriangles() {
        return triangles;
    }
}
