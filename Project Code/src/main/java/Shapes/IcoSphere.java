package Shapes;

import Core.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by (User name) on 8/10/2017.
 * Sources used: http://blog.andreaskahler.com/2009/06/creating-icosphere-mesh-in-code.html
 */
public class IcoSphere extends TriangleMesh {

    private float radius;
    private float depth;
    private int index = 0;

    /** <long key, int value>: key is a number the map uses to index based on the unique side of triangle in question. See the .cpp for more info.
     *  					 : value is the new point index that is created on the mesh.
     */
    private HashMap<Long, Integer> middlePointIndexCache;
    private ArrayList<Vector3f> positions;  //consider using Float[3] instead
    private int[] elements;
    private float[] points;
    private float[] normals;

    private class TriangleIndices{
        public int v1, v2, v3;
        public TriangleIndices(int v1, int v2, int v3){
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }
    }

    public IcoSphere(float radius, int complexity){
        this.radius = radius;
        this.depth = complexity;
        middlePointIndexCache = new HashMap<>();
        positions = new ArrayList<>();
    }

    @Override
    public void init() {
        if (vao != 0) return;

        float t = (1.0f + (float) Math.sqrt(5.0f)) / 2.0f;   //beware the conversion double -> float
        addVertex(-1, t, 0);
        addVertex(1, t, 0);
        addVertex(-1, -t, 0);
        addVertex(1, -t, 0);

        addVertex(0, -1, t);
        addVertex(0, 1, t);
        addVertex(0, -1, -t);
        addVertex(0, 1, -t);

        addVertex(t, 0, -1);
        addVertex(t, 0, 1);
        addVertex(-t, 0, -1);
        addVertex(-t, 0, 1);

        ArrayList<TriangleIndices> faces = new ArrayList<>();

        // 5 faces around point 0
        faces.add(new TriangleIndices(0, 11, 5));
        faces.add(new TriangleIndices(0, 5, 1));
        faces.add(new TriangleIndices(0, 1, 7));
        faces.add(new TriangleIndices(0, 7, 10));
        faces.add(new TriangleIndices(0, 10, 11));

        // 5 adjacent faces 
        faces.add(new TriangleIndices(1, 5, 9));
        faces.add(new TriangleIndices(5, 11, 4));
        faces.add(new TriangleIndices(11, 10, 2));
        faces.add(new TriangleIndices(10, 7, 6));
        faces.add(new TriangleIndices(7, 1, 8));

        // 5 faces around point 3
        faces.add(new TriangleIndices(3, 9, 4));
        faces.add(new TriangleIndices(3, 4, 2));
        faces.add(new TriangleIndices(3, 2, 6));
        faces.add(new TriangleIndices(3, 6, 8));
        faces.add(new TriangleIndices(3, 8, 9));

        // 5 adjacent faces 
        faces.add(new TriangleIndices(4, 9, 5));
        faces.add(new TriangleIndices(2, 4, 11));
        faces.add(new TriangleIndices(6, 2, 10));
        faces.add(new TriangleIndices(8, 6, 7));
        faces.add(new TriangleIndices(9, 8, 1));

        //refine time!
        for (int i = 0; i < depth; i++) {
            ArrayList<TriangleIndices> faces2 = new ArrayList<>();
            for (int j = 0; j < faces.size(); j++) {
                TriangleIndices tri = faces.get(j);
                //replace each triangle with 4. (Think zelda triforce!)
                int a = getMiddlePoint(tri.v1, tri.v2);
                int b = getMiddlePoint(tri.v2, tri.v3);
                int c = getMiddlePoint(tri.v3, tri.v1);

                faces2.add(new TriangleIndices(tri.v1, a, c));
                faces2.add(new TriangleIndices(tri.v2, b, a));
                faces2.add(new TriangleIndices(tri.v3, c, b));
                faces2.add(new TriangleIndices(a, b, c));
            }
            faces = faces2;
        }

        //done with inititalization, now to add triangles with GPUVertexArray
        //TODO: Normals are incorrect.
        points = new float[faces.size() * 9];
        normals = new float[faces.size() * 9];
        elements = new int[faces.size() * 3];


        for (int i = 0; i < faces.size(); i++) {
            TriangleIndices vertex = faces.get(i);
            points[i * 9 + 0] = positions.get(vertex.v1).x;
            points[i * 9 + 1] = positions.get(vertex.v1).y;
            points[i * 9 + 2] = positions.get(vertex.v1).z;
            normals[i * 9 + 0] = positions.get(vertex.v1).x;
            normals[i * 9 + 1] = positions.get(vertex.v1).y;
            normals[i * 9 + 2] = positions.get(vertex.v1).z;;
            elements[i * 3 + 0] = i * 3 + 0;

            points[i * 9 + 3] = positions.get(vertex.v2).x;
            points[i * 9 + 4] = positions.get(vertex.v2).y;
            points[i * 9 + 5] = positions.get(vertex.v2).z;
            normals[i * 9 + 3] = positions.get(vertex.v2).x;
            normals[i * 9 + 4] = positions.get(vertex.v2).y;
            normals[i * 9 + 5] = positions.get(vertex.v2).z;
            elements[i * 3 + 1] = i * 3 + 1;

            points[i * 9 + 6] = positions.get(vertex.v3).x;
            points[i * 9 + 7] = positions.get(vertex.v3).y;
            points[i * 9 + 8] = positions.get(vertex.v3).z;
            normals[i * 9 + 6] = positions.get(vertex.v3).x;
            normals[i * 9 + 7] = positions.get(vertex.v3).y;
            normals[i * 9 + 8] = positions.get(vertex.v3).z;
            elements[i * 3 + 2] = i * 3 + 2;
        }

        IntBuffer e = GLBuffers.newDirectIntBuffer(elements);
        FloatBuffer p = GLBuffers.newDirectFloatBuffer(points);
        FloatBuffer n = GLBuffers.newDirectFloatBuffer(normals);
        initGpuVertexArrays(e, p, n, null, null);
    }

    private int addVertex(float x, float y, float z){
        double unitDivisor = Math.sqrt(x*x + y*y + z*z);
        Vector3f temp = new Vector3f (
                (float)(x / unitDivisor) * radius,
                (float)(y / unitDivisor) * radius,
                (float)(z / unitDivisor) * radius
        );
        positions.add(temp);
        return index++;
    }

    private int getMiddlePoint(int p1, int p2){
        //first check if already exists
        boolean firstIsSmaller = p1 < p2;
        long smallerIndex = firstIsSmaller ? p1 : p2; //if true, p1, else, p2;
        long greaterIndex = firstIsSmaller ? p2 : p1;
        long key = (smallerIndex << 32) + greaterIndex;

        int ret = -1;   //if value at key exists, ret will never be -1.
        ret = middlePointIndexCache.getOrDefault(key, -1);

        if (ret != -1) return ret;
        //else not in cache
        Vector3f point1 = positions.get(p1);
        Vector3f point2 = positions.get(p2);
        Vector3f middle = new Vector3f(
                (point1.x + point2.x) / 2.0f,
                (point1.y + point2.y) / 2.0f,
                (point1.z + point2.z) / 2.0f
        );
        //add vertex to sphere
        int i = addVertex(middle.x, middle.y, middle.z);
        middlePointIndexCache.put(key, i);
        return i;
    }
}
