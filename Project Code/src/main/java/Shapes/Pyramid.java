package Shapes;

import Core.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by (User name) on 8/11/2017.
 */
public class Pyramid extends TriangleMesh {

    private float base;
    private float height;

    public Pyramid(float baseLength, float height){
        this.base = baseLength;
        this.height = height;
    }

    @Override
    public void init(){
        if (vao != 0) return;

        float halfheight = height / 2.0f;
        float halfbase = base / 2.0f;

        float[] v = {
                //base	//clockwise order as you look up from below.
                -halfbase, -halfheight, -halfbase,
                halfbase, -halfheight, -halfbase,
                halfbase, -halfheight, halfbase,
                -halfbase, -halfheight, halfbase,

                //duplicated pinnacle necessary? Answer: YES. One for each face that connects to it.
                //left face
                -halfbase, -halfheight, halfbase,
                0.0f,halfheight,0.0f,
                -halfbase, -halfheight, -halfbase,
                //front face
                halfbase, -halfheight, halfbase,
                0.0f, halfheight, 0.0f,
                -halfbase, -halfheight, halfbase,
                //right face
                halfbase, -halfheight, -halfbase,
                0.0f, halfheight, 0.0f,
                halfbase, -halfheight, halfbase,
                //back face
                -halfbase, -halfheight, -halfbase,
                0.0f, halfheight, 0.0f,
                halfbase, -halfheight, -halfbase

        };
        //left normal vector
        float a = (float)Math.cos((float)Math.PI / 2.0f - (float)Math.atan(height / halfbase)) * (float)Math.sin((float)Math.atan(height / halfbase)) * halfbase;
        float b = (float)Math.sin((float)Math.PI / 2.0f - (float)Math.atan(height / halfbase)) * (float)Math.sin((float)Math.atan(height / halfbase)) * halfbase;
        Vector3f left = new Vector3f(-a, b, 0.0f).normalize();
        Vector3f front = new Vector3f(0.0f, b, a).normalize();
        Vector3f right = new Vector3f(a, b, 0.0f).normalize();
        Vector3f back = new Vector3f(0.0f, b, -a).normalize();
        System.out.printf("Pyramid: left vector calculations: x(%f) y(%f) z(%f)\n", left.x, left.y, left.z);
        System.out.printf("Pyramid: front vector calculations: x(%f) y(%f) z(%f)\n", front.x, front.y, front.z);
        System.out.printf("Pyramid: right vector calculations: x(%f) y(%f) z(%f)\n", right.x, right.y, right.z);
        System.out.printf("Pyramid: back vector calculations: x(%f) y(%f) z(%f)\n", back.x, back.y, back.z);

        float norm[] = {
            //base
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            //left face
            left.x, left.y, left.z,
            //front face
            back.x, back.y, back.z,
            //right face
            right.x, right.y, right.z,
            //back face
            front.x, front.y, front.z
        };

        int el[] = {
            //base
            0,1,2,0,2,3,
            //left face
            4,5,6,
            //front face
            7,8,9,
            //right face
            10,11,12,
            //back face
            13,14,15
        };

        FloatBuffer p = GLBuffers.newDirectFloatBuffer(v);
        FloatBuffer n = GLBuffers.newDirectFloatBuffer(norm);
        IntBuffer e = GLBuffers.newDirectIntBuffer(el);

        initGpuVertexArrays(e, p, n, null, null);
    }
}
