package Shapes;

import Core.Camera;
import Core.Shader;
import Core.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BillBoardQuad extends TriangleMesh{
    private float sideLength;

    public BillBoardQuad(float sideLength){
        this.sideLength = sideLength;
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

        Matrix4f transform = new Matrix4f(
                right.x, up.x, look.x, billPosition.x,
                right.y, up.y, look.y, billPosition.y,
                right.z, up.z, look.z, billPosition.z,
                0,0,0,1
        );
        transform.transpose();  //because opengl stores matrices the other way.

        shader.setUniform("ObjectToWorld", transform);
        super.render();
    }
}
