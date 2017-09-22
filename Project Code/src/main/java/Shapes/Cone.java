package Shapes;

import Core.Shader;
import Core.TriangleMesh;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by (User name) on 8/11/2017.
 */
public class Cone extends TriangleMesh {

    private float radius;
    private float height;
    private int slices;
    private boolean capped;
    private Disk d;

    public Cone(float radius, float height, int slices, boolean capped){
        this.radius = radius;
        this.height = height;
        this.slices = slices;
        this.capped = capped;
    }

    public float getHeight(){
        return height;
    }

    @Override
    public void init(){
        if(vao != 0) return;
        if(capped){
            d = new Disk(radius, slices);
            d.init();
        }

        float[] points = new float[6 * slices];
        float[] normals = new float[6 * slices];
        int[] index = new int[3 * slices];

        float halfheight = height / 2.0f;
        float factor = 2 * (float)Math.PI / slices;
        for (int i = 0; i < slices; i++){
            float angle = factor * i;

            //points
            points[i * 6] = radius * (float)Math.cos(angle);
            points[i * 6 + 1] = radius * (float)Math.sin(angle);
            points[i * 6 + 2] = -halfheight;
            //centerpoint duplications
            points[i * 6 + 3] = 0;
            points[i * 6 + 4] = 0;
            points[i * 6 + 5] = halfheight;

            //normals
            Vector3f surface = new Vector3f(0 - points[i * 6], 0 - points[i * 6 + 1], halfheight - 0);  //points from apex to base
            Vector3f radial = new Vector3f(0 - points[i * 6], 0 - points[i * 6 + 1], 0 - halfheight);			//points from edge of base to center of base

            Vector3f tangent = new Vector3f();
            surface.cross(radial,tangent);

            Vector3f normal = new Vector3f();
            surface.cross(tangent, normal);
            normal.normalize();

            normals[i * 6] = normal.x;
            normals[i * 6 + 1] = normal.y;
            normals[i * 6 + 2] = normal.z;
            //centernormal duplications
            Vector3f midpoint = new Vector3f(
                    radius * (float)Math.cos(angle) + radius*(float)Math.cos((2 * (float)Math.PI) / (2 * slices)),
                    radius * (float)Math.sin(angle) + radius*(float)Math.sin((2 * (float)Math.PI) / (2 * slices)),
                    -halfheight
            ); //finds the midpoint at the base of the triangle
            surface = new Vector3f(0 - midpoint.x, 0 - midpoint.y, halfheight - midpoint.z);
            radial = new Vector3f(0 - midpoint.x, 0 - midpoint.y, -halfheight - midpoint.z);
            surface.cross(radial, tangent);
            surface.cross(tangent, normal);
            normal.normalize();

            normals[i * 6 + 3] = normal.x;
            normals[i * 6 + 4] = normal.y;
            normals[i * 6 + 5] = normal.z;

            //indices
            index[i * 3] = 2 * i;
            index[i * 3 + 1] = (2 * (i + 1)) % (2 * slices);
            index[i * 3 + 2] = 2 * i + 1;
        }
        FloatBuffer p = GLBuffers.newDirectFloatBuffer(points);
        FloatBuffer n = GLBuffers.newDirectFloatBuffer(normals);
        IntBuffer e = GLBuffers.newDirectIntBuffer(index);

        initGpuVertexArrays(e, p, n, null, null);
    }
    public void setHeight(float newheight){
        height = newheight;
        init();
    }


    public void render(Shader shader, Matrix4f ObjectToWorld){
        if (capped) {
            Matrix4f temp = new Matrix4f();
            ObjectToWorld.rotate((float)Math.toRadians(180), 0, 1, 0, temp);
            temp.translate(0,0, height / 2);
            shader.setUniform("ObjectToWorld", temp);
            d.render();
            shader.setUniform("ObjectToWorld", ObjectToWorld);
        }
        super.render();
    }

    @Override
    public void render(){
        if (capped)
            throw new IllegalArgumentException("Cone: Don't call Cone.render(GL4). Call Cone.render(GL4, Shader, Matrix4f)");
        super.render();
    }


}
