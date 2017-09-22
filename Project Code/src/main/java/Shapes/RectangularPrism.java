package Shapes;

import Core.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by (User name) on 8/12/2017.
 */
public class RectangularPrism extends TriangleMesh {

    private float length;
    private float width;
    private float height;

    public RectangularPrism(float length, float width, float height){
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public void init(){
        if (vao != 0) return;

        float halflength = length / 2.0f;
        float halfwidth = width / 2.0f;
        float halfheight = height / 2.0f;

        float[] v = {
                // Front
                -halflength, -halfheight, halfwidth,
                halflength, -halfheight, halfwidth,
                halflength, halfheight, halfwidth,
                -halflength, halfheight, halfwidth,
                // Right
                halflength, -halfheight, halfwidth,
                halflength, -halfheight, -halfwidth,
                halflength, halfheight, -halfwidth,
                halflength, halfheight, halfwidth,
                // Back
                -halflength, -halfheight, -halfwidth,
                -halflength, halfheight, -halfwidth,
                halflength, halfheight, -halfwidth,
                halflength, -halfheight, -halfwidth,
                // Left
                -halflength, -halfheight, halfwidth,
                -halflength, halfheight, halfwidth,
                -halflength, halfheight, -halfwidth,
                -halflength, -halfheight, -halfwidth,
                // Bottom
                -halflength, -halfheight, halfwidth,
                -halflength, -halfheight, -halfwidth,
                halflength, -halfheight, -halfwidth,
                halflength, -halfheight, halfwidth,
                // Top
                -halflength, halfheight, halfwidth,
                halflength, halfheight, halfwidth,
                halflength, halfheight, -halfwidth,
                -halflength, halfheight, -halfwidth
        };

        float[] n = {
                // Front
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                // Right
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                // Back
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                // Left
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                // Bottom
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                // Top
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        };

        int[] el = {
                0, 1, 2, 0, 2, 3,
                4, 5, 6, 4, 6, 7,
                8, 9, 10, 8, 10, 11,
                12, 13, 14, 12, 14, 15,
                16, 17, 18, 16, 18, 19,
                20, 21, 22, 20, 22, 23

        };

        FloatBuffer points = GLBuffers.newDirectFloatBuffer(v);
        FloatBuffer normals = GLBuffers.newDirectFloatBuffer(n);
        IntBuffer elements = GLBuffers.newDirectIntBuffer(el);

        initGpuVertexArrays(elements, points, normals, null, null);
    }
}
