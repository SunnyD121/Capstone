package World.AbstractShapes;

import World.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;

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

    @Override
    public float getLength() {
        return length;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public void init(){
        if (vao != 0) return;

        float halflength = length / 2.0f;
        float halfwidth = width / 2.0f;
        float halfheight = height / 2.0f;

        float[] points = {
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

        float[] normals = {
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

        int[] indices = {
                0, 1, 2, 0, 2, 3,
                4, 5, 6, 4, 6, 7,
                8, 9, 10, 8, 10, 11,
                12, 13, 14, 12, 14, 15,
                16, 17, 18, 16, 18, 19,
                20, 21, 22, 20, 22, 23

        };

        //Triangles, for use in collision detection
        triangles = new Triangle[indices.length/3];
        for (int i = 0; i < triangles.length; i++){
            triangles[i] = new Triangle(
                    new Vector3f(points[3*indices[3*i]], points[3*indices[3*i]+1], points[3*indices[3*i]+2]),
                    new Vector3f(points[3*indices[3*i+1]], points[3*indices[3*i+1]+1], points[3*indices[3*i+1]+2]),
                    new Vector3f(points[3*indices[3*i+2]], points[3*indices[3*i+2]+1], points[3*indices[3*i+2]+2])
            );
        }

        FloatBuffer vertices = GLBuffers.newDirectFloatBuffer(points);
        FloatBuffer norms = GLBuffers.newDirectFloatBuffer(normals);
        IntBuffer elements = GLBuffers.newDirectIntBuffer(indices);

        initGpuVertexArrays(elements, vertices, norms, null, null);
    }

    @Override
    public Triangle[] getTriangles() {
        return triangles;
    }
}
