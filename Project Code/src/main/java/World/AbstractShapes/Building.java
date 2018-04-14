package World.AbstractShapes;

import World.TriangleMesh;
import Utilities.Utilities;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

/**
 * Created by (User name) on 8/14/2017.
 */
public class Building extends TriangleMesh{
    private Vector3f[] base;
    private float[] height;

    private float length;
    private float width;

    public Building() {
        base = new Vector3f[4];
        height = new float[4];
    }

    public float getHeight(){
        boolean same = true;
        float tempVar = height[0];
        for(int i =0; i < height.length; i++) if (tempVar != height[i]) same = false;

        if(same) return tempVar;
        else return -1;
    }
    public float getLength(){
        return length;
    }
    public float getWidth(){
        return width;
    }

    @Override
    public void init(){
        ArrayList<Float> points = new ArrayList<>();
        ArrayList<Float> normals = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();

        //side faces
        buildFace(points, normals, indices, 0,1);
        buildFace(points, normals, indices, 1,2);
        buildFace(points, normals, indices, 2,3);
        buildFace(points, normals, indices, 3,0);

        //top face
        buildTop(points, normals, indices, 0,1,2);
        buildTop(points, normals, indices, 0,2,3);

        float tex[] = {
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f };


        //Triangles, for use in collision detection
        triangles = new Triangle[indices.size() / 3];
        for (int i = 0; i < triangles.length; i++){
            triangles[i] = new Triangle(
                    new Vector3f(points.get(3*indices.get(3*i)), points.get(3*indices.get(3*i)+1), points.get(3*indices.get(3*i)+2)),
                    new Vector3f(points.get(3*indices.get(3*i+1)), points.get(3*indices.get(3*i+1)+1), points.get(3*indices.get(3*i+1)+2)),
                    new Vector3f(points.get(3*indices.get(3*i+2)), points.get(3*indices.get(3*i+2)+1), points.get(3*indices.get(3*i+2)+2))
            );
        }


        FloatBuffer p = Utilities.convertF(points);
        FloatBuffer n = Utilities.convertF(normals);
        IntBuffer i = Utilities.convertI(indices);
        FloatBuffer textureBuf = GLBuffers.newDirectFloatBuffer(tex);

        initGpuVertexArrays(i, p, n, null, textureBuf);
    }

    @Override
    public Triangle[] getTriangles() {
        return triangles;
    }

    public void setData(Vector3f[] base, float[] height){
        for(int i = 0; i < 4; i++){
            this.base[i] = base[i];
            this.height[i] = height[i];
        }
        this.length = Utilities.dist(base[0], base[1]);
        this.width = Utilities.dist(base[1], base[2]);

        this.setPosition(interpolatePosition());
    }

    public Vector3f getMin(){
        float minX = minValue(base[0].x,base[1].x,base[2].x,base[3].x);
        float minY = base[0].y;
        float minZ = minValue(base[0].z,base[1].z,base[2].z,base[3].z);
        return new Vector3f(minX, minY, minZ);
    }

    public Vector3f getMax(){
        float maxX = maxValue(base[0].x,base[1].x,base[2].x,base[3].x);
        float maxY = height[0];
        float maxZ = maxValue(base[0].z,base[1].z,base[2].z,base[3].z);
        return new Vector3f(maxX, maxY, maxZ);
    }

    private float minValue(float...v){
        float min = v[0];
        for (float f : v) if (min > f) min = f;
        return min;
    }

    private float maxValue(float...v){
        float max = v[0];
        for (float f : v) if (max < f) max = f;
        return max;
    }

    private void buildFace(ArrayList<Float> points, ArrayList<Float> normals, ArrayList<Integer> indices, int idx1, int idx2){
        int offset = points.size() / 3;
        Vector3f[] p = new Vector3f[4];
        p[0] = base[idx1];
        p[1] = base[idx2];
        p[2] = new Vector3f(p[1].x, height[idx2], p[1].z);
        p[3] = new Vector3f(p[0].x, height[idx1], p[0].z);

        Vector3f temp1 = new Vector3f();
        p[1].sub(p[0], temp1);
        Vector3f temp2 = new Vector3f();
        p[2].sub(p[0], temp2);
        Vector3f n = new Vector3f();
        temp1.cross(temp2, n);
        n.normalize();

        for(int i = 0; i < 4; i++){
            points.add(p[i].x);
            points.add(p[i].y);
            points.add(p[i].z);
            normals.add(n.x);
            normals.add(n.y);
            normals.add(n.z);
        }
        indices.add(offset + 0);
        indices.add(offset + 1);
        indices.add(offset + 2);
        indices.add(offset + 0);
        indices.add(offset + 2);
        indices.add(offset + 3);
    }

    private void buildTop(ArrayList<Float> points, ArrayList<Float> normals, ArrayList<Integer> indices, int idx1, int idx2, int idx3){
        int offset = points.size() / 3;

        Vector3f[] p = new Vector3f[4];
        p[0] = new Vector3f(base[idx1].x, height[idx1], base[idx1].z);
        p[1] = new Vector3f(base[idx2].x, height[idx2], base[idx2].z);
        p[2] = new Vector3f(base[idx3].x, height[idx3], base[idx3].z);

        Vector3f temp1 = new Vector3f();
        p[1].sub(p[0], temp1);
        Vector3f temp2 = new Vector3f();
        p[2].sub(p[0], temp2);
        Vector3f n = new Vector3f();
        temp1.cross(temp2, n);
        n.normalize();

        for(int i = 0; i < 3; i++){
            int a  = p.length;
            points.add(p[i].x);
            points.add(p[i].y);
            points.add(p[i].z);
            normals.add(n.x);
            normals.add(n.y);
            normals.add(n.z);
        }
        indices.add(offset + 0);
        indices.add(offset + 1);
        indices.add(offset + 2);
    }

    private Vector3f interpolatePosition(){
        float centerX = (base[0].x < base[2].x ? base[0].x : base[2].x) + (Math.abs(base[0].x - base[2].x) / 2.0f);
        float centerY = (height[0] - base[0].y) / 2.0f;
        float centerZ = (base[0].z < base[2].z ? base[0].z : base[2].z) + (Math.abs(base[0].z - base[2].z) / 2.0f);

        return new Vector3f(centerX, centerY, centerZ);
    }


}
