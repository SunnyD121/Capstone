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
        /*
        System.out.printf("Pyramid: left vector calculations: x(%f) y(%f) z(%f)\n", left.x, left.y, left.z);
        System.out.printf("Pyramid: front vector calculations: x(%f) y(%f) z(%f)\n", front.x, front.y, front.z);
        System.out.printf("Pyramid: right vector calculations: x(%f) y(%f) z(%f)\n", right.x, right.y, right.z);
        System.out.printf("Pyramid: back vector calculations: x(%f) y(%f) z(%f)\n", back.x, back.y, back.z);
        */
        float norm[] = {
                //base
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                //left face
                left.x, left.y, left.z,
                left.x, left.y, left.z,
                left.x, left.y, left.z,
                //front face
                front.x, front.y, front.z,
                front.x, front.y, front.z,
                front.x, front.y, front.z,

                //right face
                right.x, right.y, right.z,
                right.x, right.y, right.z,
                right.x, right.y, right.z,
                //back face
                back.x, back.y, back.z,
                back.x, back.y, back.z,
                back.x, back.y, back.z
        };

        /* Ugly, Smelly code; needs Refactoring, but it finds the Normals of the corners. See Cone for a better way.
        Vector3f g = new Vector3f();
        Vector3f NE = new Vector3f(halfbase, -halfheight, -halfbase);
        Vector3f SE = new Vector3f(halfbase, -halfheight, halfbase);
        Vector3f SW = new Vector3f(-halfbase, -halfheight, halfbase);
        Vector3f NW = new Vector3f(-halfbase, -halfheight, -halfbase);
        Vector3f pin = new Vector3f(0,halfheight, 0);

        System.out.println("NE "+NE);
        System.out.println("SE "+SE);
        System.out.println("SW "+SW);
        System.out.println("NW "+NW);

        Vector3f hypNE = new Vector3f();
        Vector3f hypSE = new Vector3f();
        Vector3f hypSW = new Vector3f();
        Vector3f hypNW = new Vector3f();

        NE.negate(g).add(pin, hypNE);
        SE.negate(g).add(pin, hypSE);
        SW.negate(g).add(pin, hypSW);
        NW.negate(g).add(pin, hypNW);

        System.out.println("hypNE "+hypNE);
        System.out.println("hypSE "+hypSE);
        System.out.println("hypSW "+hypSW);
        System.out.println("hypNW "+hypNW);

        Vector3f NEC = new Vector3f();
        Vector3f SEC = new Vector3f();
        Vector3f SWC = new Vector3f();
        Vector3f NWC = new Vector3f();

        NW.mul(2, NEC);
        NE.mul(2, SEC);
        SE.mul(2, SWC);
        SW.mul(2, NWC);

        System.out.println("NEC "+NEC);
        System.out.println("SEC "+SEC);
        System.out.println("SWC "+SWC);
        System.out.println("NWC "+NWC);

        Vector3f NEP = new Vector3f();
        Vector3f SEP = new Vector3f();
        Vector3f SWP = new Vector3f();
        Vector3f NWP = new Vector3f();

        hypNE.cross(NEC).negate(NEP);
        hypSE.cross(SEC).negate(SEP);
        hypSW.cross(SWC).negate(SWP);
        hypNW.cross(NWC).negate(NWP);

        System.out.println("NEP "+NEP);
        System.out.println("SEP "+SEP);
        System.out.println("SWP "+SWP);
        System.out.println("NWP "+NWP);

        NEP.normalize();
        SEP.normalize();
        SWP.normalize();
        NWP.normalize();


        float norm[] = {
                //base
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                //left face
                NWP.x, NWP.y, NWP.z,
                SWP.x, SWP.y, SWP.z,
                -1.0f, 0, 0,
                //front face
                SWP.x, SWP.y, SWP.z,
                SEP.x, SEP.y, SEP.z,
                0, 0, 1.0f,
                //right face
                SEP.x, SEP.y, SEP.z,
                NEP.x, NEP.y, NEP.z,
                1.0f, 0, 0,
                //back face
                NEP.x, NEP.y, NEP.z,
                NWP.x, NWP.y, NWP.z,
                0, 0, -1.0f

        };
        */
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
