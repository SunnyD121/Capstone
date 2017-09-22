package Shapes;

import Core.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by (User name) on 8/11/2017.
 */
public class Prism extends TriangleMesh {

    private float length;
    private float radius;
    private int sides;

    public Prism(float radius, float length, int sides){
        if (sides < 3){
            throw new IllegalArgumentException("Prism: cannot have less than 3 sides.");
        }
        this.radius = radius;
        this.length = length;
        this.sides = sides;
    }

    public void setHeight(float newHeight){
        length = newHeight;
        init();
    }

    public float getHeight(){
        return length;
    }

    @Override
    public void init(){
        if (vao != 0) return;

        float halflength = length / 2.0f;
        float[] points = new float[sides * 12 + 6];    //12: 3xyz * 4 objects (Prism 2 disk) + 6xyz (2 centers)
        float[] normals = new float[sides * 12 + 6];
        int[] index = new int[sides * 12];

        float factor = 2 * (float)Math.PI / sides;
        //middle section
        for (int i = 0; i < sides; i++) {
            float angle = factor * i;

            //points
            //base
            points[i * 6 + 0] = radius * (float)Math.cos(angle);
            points[i * 6 + 1] = radius * (float)Math.sin(angle);
            points[i * 6 + 2] = -halflength;
            //top
            points[i * 6 + 3] = radius * (float)Math.cos(angle);
            points[i * 6 + 4] = radius * (float)Math.sin(angle);
            points[i * 6 + 5] = halflength;

            //normals
            Vector3f normal = new Vector3f(radius * (float)Math.cos(angle), radius * (float)Math.sin(angle), 0).normalize();

            normals[i * 6 + 0] = normal.x;
            normals[i * 6 + 1] = normal.y;
            normals[i * 6 + 2] = normal.z;
            normals[i * 6 + 3] = normal.x;
            normals[i * 6 + 4] = normal.y;
            normals[i * 6 + 5] = normal.z;

            //indexes
            index[i * 6] = (2 * i) % (2 * sides);
            index[i * 6 + 1] = (2 * i + 2) % (2 * sides);
            index[i * 6 + 2] = (2 * i + 1) % (2 * sides);
            index[i * 6 + 3] = (2 * i + 2) % (2 * sides);
            index[i * 6 + 4] = (2 * i + 3) % (2 * sides);
            index[i * 6 + 5] = (2 * i + 1) % (2 * sides);
        }

        int offset = sides * 6;    //so that we add to the end of the array, instead of overwriting.
        //near cap
        for (int i = 0; i < sides; i++) {
            float angle = factor * i;

            points[i * 3 + offset + 0] = radius * (float)Math.cos(angle);
            points[i * 3 + offset + 1] = radius * (float)Math.sin(angle);
            points[i * 3 + offset + 2] = halflength;

            normals[i * 3 + offset + 0] = 0.0f;
            normals[i * 3 + offset + 1] = 0.0f;
            normals[i * 3 + offset + 2] = 1.0f;

            index[i * 3 + offset + 0] = i + (sides * 2);
            index[i * 3 + offset + 1] = (i + 1) % (sides) + (sides * 2);
            index[i * 3 + offset + 2] = sides + (sides * 2);
        }
        //Add center point (near)
        points[sides * 9 + 0] = 0.0f;
        points[sides * 9 + 1] = 0.0f;
        points[sides * 9 + 2] = halflength;
        normals[sides * 9 + 0] = 0.0f;
        normals[sides * 9 + 1] = 0.0f;
        normals[sides * 9 + 2] = 1.0f;

        offset += (sides * 3 + 3);
        int index_offset = offset - 3;  //because of the above center point, point/normal increase, but index doesn't.
        //far cap
        for (int i = 0; i < sides; i++) {
            float angle = factor * i;

            points[i * 3 + offset + 0] = radius * (float) Math.cos(angle);
            points[i * 3 + offset + 1] = radius * (float) Math.sin(angle);
            points[i * 3 + offset + 2] = -halflength;

            normals[i * 3 + offset + 0] = 0.0f;
            normals[i * 3 + offset + 1] = 0.0f;
            normals[i * 3 + offset + 2] = -1.0f;

            index[i * 3 + index_offset + 1] = i + (sides * 3) + 1;
            index[i * 3 + index_offset + 0] = (i + 1) % (sides) + (sides * 3) + 1;
            index[i * 3 + index_offset + 2] = sides + (sides * 3) + 1;

        }
        //Add center point (far)
        offset = 3;     //to account for the other center point
        points[sides * 12 + offset + 0] = 0.0f;
        points[sides * 12 + offset + 1] = 0.0f;
        points[sides * 12 + offset + 2] = -halflength;
        normals[sides * 12 + offset + 0] = 0.0f;
        normals[sides * 12 + offset + 1] = 0.0f;
        normals[sides * 12 + offset + 2] = -1.0f;



        FloatBuffer p = GLBuffers.newDirectFloatBuffer(points);
        FloatBuffer n = GLBuffers.newDirectFloatBuffer(normals);
        IntBuffer e = GLBuffers.newDirectIntBuffer(index);

        initGpuVertexArrays(e, p, n, null, null);
    }
}
