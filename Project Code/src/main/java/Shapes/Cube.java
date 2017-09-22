package Shapes;

import Core.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by (User name) on 7/25/2017.
 */
public class Cube extends TriangleMesh {

    private float sideLength;

    public Cube (float side){
        sideLength = side;
    }

    @Override
    public void init(){
        if (vao != 0) {
            System.err.println("CUBE: VAO is not 0.");
            return;
        }
        float halfside = sideLength * 0.5f;

        float[] vertices = {
                // Front
                -halfside,-halfside,halfside,  halfside,-halfside,halfside,   halfside,halfside,halfside,   -halfside,halfside,halfside,
                // Right
                halfside,-halfside,halfside,  halfside,-halfside,-halfside,  halfside,halfside,-halfside,   halfside,halfside,halfside,
                // Back
                -halfside,-halfside,-halfside, -halfside,halfside,-halfside,  halfside,halfside,-halfside,   halfside,-halfside,-halfside,
                // Left
                -halfside,-halfside,halfside,  -halfside,halfside,halfside,   -halfside,halfside,-halfside, -halfside,-halfside,-halfside,
                // Bottom
                -halfside,-halfside,halfside,  -halfside,-halfside,-halfside, halfside,-halfside,-halfside,  halfside,-halfside,halfside,
                // Top
                -halfside,halfside,halfside,   halfside,halfside,halfside,    halfside,halfside,-halfside,  -halfside,halfside,-halfside
        };
        float[] normals = {
                // Front
                0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                // Right
                1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                // Back
                0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f,
                // Left
                -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                // Bottom
                0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
                // Top
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
        };
        float[] tex = {
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f
        };
        int[] index = {
                0,1,2,0,2,3,
                4,5,6,4,6,7,
                8,9,10,8,10,11,
                12,13,14,12,14,15,
                16,17,18,16,18,19,
                20,21,22,20,22,23
        };

        IntBuffer orderBuf = GLBuffers.newDirectIntBuffer(index);
        FloatBuffer vertexBuf = GLBuffers.newDirectFloatBuffer(vertices);
        FloatBuffer normalBuf = GLBuffers.newDirectFloatBuffer(normals);
        FloatBuffer textureBuf = GLBuffers.newDirectFloatBuffer(tex);

        initGpuVertexArrays(orderBuf, vertexBuf, normalBuf, null, textureBuf);
    }
}
