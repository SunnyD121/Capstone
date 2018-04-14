package World.AbstractShapes;

import World.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;

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
    public float getLength() {
        return sideLength;
    }

    @Override
    public float getHeight() {
        return sideLength;
    }

    @Override
    public float getWidth() {
        return sideLength;
    }

    @Override
    public void init(){
        if (vao != 0) {
            System.err.println("CUBE: VAO is not 0.");
            return;
        }
        float halfside = sideLength * 0.5f;

        float[] points = {
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
        int[] indices = {
                0,1,2,0,2,3,
                4,5,6,4,6,7,
                8,9,10,8,10,11,
                12,13,14,12,14,15,
                16,17,18,16,18,19,
                20,21,22,20,22,23
        };
        
        
        /*
        //This is just the code to see how it's done.
        for (int i = 0; i < triangles.length; i++){
            int p1 = indices[3*i];
            int p2 = indices[3*i+1];
            int p3 = indices[3*i+2];
            Triangle t = new Triangle(
                    new Vector3f(points[3*p1], points[3*p1+1], points[3*p1+2]),
                    new Vector3f(points[3*p2], points[3*p2+1], points[3*p2+2]),
                    new Vector3f(points[3*p3], points[3*p3+1], points[3*p3+2])
            );
            triangles[i] = t;
        }
        */

        //Triangles, for use in collision detection
        triangles = new Triangle[indices.length/3];
        for (int i = 0; i < triangles.length; i++){
            triangles[i] = new Triangle(
                    new Vector3f(points[3*indices[3*i]], points[3*indices[3*i]+1], points[3*indices[3*i]+2]),
                    new Vector3f(points[3*indices[3*i+1]], points[3*indices[3*i+1]+1], points[3*indices[3*i+1]+2]),
                    new Vector3f(points[3*indices[3*i+2]], points[3*indices[3*i+2]+1], points[3*indices[3*i+2]+2])
            );
        }

        IntBuffer orderBuf = GLBuffers.newDirectIntBuffer(indices);
        FloatBuffer vertexBuf = GLBuffers.newDirectFloatBuffer(points);
        FloatBuffer normalBuf = GLBuffers.newDirectFloatBuffer(normals);
        FloatBuffer textureBuf = GLBuffers.newDirectFloatBuffer(tex);

        initGpuVertexArrays(orderBuf, vertexBuf, normalBuf, null, textureBuf);
    }

    @Override
    public Triangle[] getTriangles() {
        return triangles;
    }
}
