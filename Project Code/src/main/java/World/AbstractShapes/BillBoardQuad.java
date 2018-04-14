package World.AbstractShapes;

import Core.Shader;
import World.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BillBoardQuad extends TriangleMesh{
    private float sideLength;
    private Matrix4f transform;

    public BillBoardQuad(float sideLength){
        this.sideLength = sideLength;
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
        return 0;
    }

    @Override
    public void init(){
        float half = sideLength/2;

        float[] points = {
            -half,-half,0,
            half,-half,0,
            half,half,0,
            -half,half,0
        };

        float[] normals = {
            0,0,1,
            0,0,1,
            0,0,1,
            0,0,1
        };

        int[] indices = {
                0,1,2,0,2,3
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

        FloatBuffer p = GLBuffers.newDirectFloatBuffer(points);
        FloatBuffer n = GLBuffers.newDirectFloatBuffer(normals);
        IntBuffer e = GLBuffers.newDirectIntBuffer(indices);

        initGpuVertexArrays(e, p, n, null, null);
    }

    @Override
    public void render(){
        throw new IllegalArgumentException("Don't call BillBoardQuad.render(), try the other one.");
    }

    @Override
    public void render(Shader shader, Vector3f billPosition, Vector3f cameraPosition){
        Vector3f look = new Vector3f();
        billPosition.sub(cameraPosition, look);     //temporary look vector
        look.negate();
        look.normalize();

        Vector3f right = new Vector3f();
        new Vector3f(0,1,0).cross(look, right);

        Vector3f up = new Vector3f();
        look.cross(right, up);

        transform = new Matrix4f(
                right.x, up.x, look.x, billPosition.x,
                right.y, up.y, look.y, billPosition.y,
                right.z, up.z, look.z, billPosition.z,
                0,0,0,1
        );
        transform.transpose();  //because opengl stores matrices the other way.

        shader.setUniform("ObjectToWorld", transform);
        super.render();
    }

    @Override
    public Triangle[] getTriangles() {
        Triangle[] temp = new Triangle[triangles.length];
        for(int i = 0; i < triangles.length; i++) temp[i] = triangles[i];
        for (int i = 0; i < temp.length; i++){
            Vector4f p1 = new Vector4f(temp[i].p1.x, temp[i].p1.y, temp[i].p1.z, 1);
            Vector4f p2 = new Vector4f(temp[i].p2.x, temp[i].p2.y, temp[i].p2.z, 1);
            Vector4f p3 = new Vector4f(temp[i].p3.x, temp[i].p3.y, temp[i].p3.z, 1);
            p1.mul(transform);
            p2.mul(transform);
            p3.mul(transform);
            Vector3f newP1 = new Vector3f(p1.x, p1.y, p1.z);
            Vector3f newP2 = new Vector3f(p2.x, p2.y, p2.z);
            Vector3f newP3 = new Vector3f(p3.x, p3.y, p3.z);
            temp[i] = new Triangle(newP1, newP2, newP3);
        }
        return temp;
    }
}
