package Shapes;

import Core.TriangleMesh;
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

    public Building() {
        base = new Vector3f[4];
        height = new float[4];
    }

    @Override
    public void init(){
        ArrayList<Float> points = new ArrayList<>();
        ArrayList<Float> normals = new ArrayList<>();
        ArrayList<Integer> index = new ArrayList<>();

        //side faces
        buildFace(points, normals, index, 0,1);
        buildFace(points, normals, index, 1,2);
        buildFace(points, normals, index, 2,3);
        buildFace(points, normals, index, 3,0);

        //top face
        buildTop(points, normals, index, 0,1,2);
        buildTop(points, normals, index, 0,2,3);

        float tex[] = {
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f };

        FloatBuffer p = Utilities.convertF(points);
        FloatBuffer n = Utilities.convertF(normals);
        IntBuffer i = Utilities.convertI(index);
        FloatBuffer textureBuf = GLBuffers.newDirectFloatBuffer(tex);

        initGpuVertexArrays(i, p, n, null, textureBuf);
    }

    public void setData(Vector3f[] base, float[] height){
        for(int i = 0; i < 4; i++){
            this.base[i] = base[i];
            this.height[i] = height[i];
        }
    }

    private void buildFace(ArrayList<Float> points, ArrayList<Float> normals, ArrayList<Integer> index, int idx1, int idx2){
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
        index.add(offset + 0);
        index.add(offset + 1);
        index.add(offset + 2);
        index.add(offset + 0);
        index.add(offset + 2);
        index.add(offset + 3);
    }

    private void buildTop(ArrayList<Float> points, ArrayList<Float> normals, ArrayList<Integer> index, int idx1, int idx2, int idx3){
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
        index.add(offset + 0);
        index.add(offset + 1);
        index.add(offset + 2);
    }

}
