package Shapes;

import Core.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;

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
    public void init(){
        if (vao != 0) return;

        float[] v = new float[3 * (slices + 1)];
        float[] n = new float[3 * (slices + 1)];
        int[] e = new int[3 * slices];

        float factor = (2 * (float)Math.PI) / slices;         //Be wary the double -> float truncation
        for(int i=0; i<slices; i++){
            float angle = factor * i;
            v[i*3+0] = radius * (float)Math.cos(angle);     //Be wary the double -> float truncation
            v[i*3+1] = radius * (float)Math.sin(angle);     //Be wary the double -> float truncation
            v[i*3+2] = 0.0f;

            n[i*3+0] = 0.0f;
            n[i*3+1] = 0.0f;
            n[i*3+2] = 1.0f;

            e[i*3+0] = i;
            e[i*3+1] = (i +1) % slices;
            e[i*3+2] = slices;
        }
        //Add center point
        v[slices*3+0] = 0.0f;
        v[slices*3+1] = 0.0f;
        v[slices*3+2] = 0.0f;
        n[slices*3+0] = 0.0f;
        n[slices*3+1] = 0.0f;
        n[slices*3+2] = 1.0f;

        IntBuffer elements = GLBuffers.newDirectIntBuffer(e);
        FloatBuffer vertices = GLBuffers.newDirectFloatBuffer(v);
        FloatBuffer normals = GLBuffers.newDirectFloatBuffer(n);

        initGpuVertexArrays(elements, vertices, normals,null, null);
    }
}
