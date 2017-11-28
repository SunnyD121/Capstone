package World.Particles;

import Core.Shader;
import Shapes.BillBoardQuad;
import Shapes.Cube;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Snowflake extends Particle {

    public Snowflake(Vector3f loc){
        particleShape = new BillBoardQuad(0.1f);
        particleShape.init();

        //rotation vector:
        //Vector3f r = new Vector3f(-1,0,1);
        //float rotationAmount = (float)Math.toRadians(30);
        location = new Vector3f(((float)Math.random() - 0.5f) * 200, 0, ((float)Math.random() -0.5f) * 200);   //spread out over plane of 100,0,100
        location.add(loc);  //move to emitter position

        float xFactor = (float)Math.random() * 2 - 1;
        float zFactor = (float)Math.random() * 2 - 1;
        velocity = new Vector3f(xFactor,-1 * 2, zFactor);
        acceleration = new Vector3f(xFactor * 0.1f, -2 * 0.1f, zFactor * 0.1f).div(4);

        lifespan = 200;

    }
    //NOTE: Snowflake.java had to overload render() and run() from superclass Particle.java because Snowflake's particleShape
    //          requires a custom render call instead of the default TriangleMesh one.
    public void render(Shader shader, Matrix4f O2W, Vector3f cameraPosition){
        O2W.translate(location);
        shader.setUniform("ObjectToWorld", O2W);
        //Guarenteed to be a billboard.
        particleShape.render(shader, new Vector3f(O2W.m30(), O2W.m31(), O2W.m32()), cameraPosition);
    }

    public void run(Shader shader, Matrix4f ObjectToWorld, Vector3f cameraPosition){
        update();
        render(shader, ObjectToWorld, cameraPosition);
    }
}
