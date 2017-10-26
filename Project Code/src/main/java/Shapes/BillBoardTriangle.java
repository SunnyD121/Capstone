package Shapes;

import Core.Shader;
import Core.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BillBoardTriangle extends TriangleMesh{
    private float sideLength;

    public BillBoardTriangle(float sideLength){
        this.sideLength = sideLength;
    }

    @Override
    public void init(){
        float half = sideLength/2;

        float[] points = {
            0,half,0,
            -half,-half,0,
            half,-half,0
        };

        float[] normals = {
            0,0,1,
            0,0,1,
            0,0,1
        };

        int[] indices = {
                0,1,2
        };

        FloatBuffer p = GLBuffers.newDirectFloatBuffer(points);
        FloatBuffer n = GLBuffers.newDirectFloatBuffer(normals);
        IntBuffer e = GLBuffers.newDirectIntBuffer(indices);

        initGpuVertexArrays(e, p, n, null, null);
    }

    @Override
    public void render(){
        throw new IllegalArgumentException("Don't call BillBoardTriangle.render(), try the other one.");
    }

    public void render(Shader shader, Matrix4f O2W, Vector3f cameraDirection){
        //TODO Problem: need to rotate the O2W matrix so that the Triangle is facing the negated cameraDirection.
/*
        float[] OtoW = new float[16];
        O2W.get(OtoW);
        Vector3f position = new Vector3f(OtoW[12], OtoW[13], OtoW[14]);
        Vector3f direction = new Vector3f(OtoW[8], OtoW[9], OtoW[10]);

        Vector3f crossAxis = new Vector3f();
        direction.cross(cameraDirection, crossAxis);
        float dotAngle = direction.dot(cameraDirection);
        O2W.rotateY(dotAngle * crossAxis.y);
        //normal.rotateY(-dotAngle * crossAxis.y);
        shader.setUniform("ObjectToWorld", O2W);
*/
        super.render();
    }
}
