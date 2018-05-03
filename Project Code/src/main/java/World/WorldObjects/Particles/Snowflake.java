package World.WorldObjects.Particles;

import Core.CollisionDetectionSystem.CollisionDetectionSystem;
import Core.Shader;
import World.AbstractShapes.BillBoardQuad;
import World.AbstractShapes.Cube;
import World.AbstractShapes.Triangle;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Snowflake extends Particle {

    public Snowflake(Vector3f loc){
        particleShape = new BillBoardQuad(0.1f);
        particleShape.init();

        //rotation vector:
        //Vector3f r = new Vector3f(-1,0,1);
        //float rotationAmount = (float)Math.toRadians(30);
        setPosition( new Vector3f(((float)Math.random() - 0.5f) * 200, 0, ((float)Math.random() -0.5f) * 200) );   //spread out over plane of 100,0,100
        setPosition(getPosition().add(loc));  //move to emitter position

        float xFactor = (float)Math.random() * 2 - 1;
        float zFactor = (float)Math.random() * 2 - 1;
        velocity = new Vector3f(xFactor,-1 * 2, zFactor);
        acceleration = new Vector3f(xFactor * 0.1f, -2 * 0.1f, zFactor * 0.1f).div(4);

        maxLifeSpan = 100;
        lifespan = maxLifeSpan;

    }

    @Override
    public void renew(Vector3f emitterPos) {
        setPosition( new Vector3f(((float)Math.random() - 0.5f) * 200, 0, ((float)Math.random() -0.5f) * 200) );   //spread out over plane of 100,0,100
        setPosition(getPosition().add(emitterPos));  //move to emitter position

        float xFactor = (float)Math.random() * 2 - 1;
        float zFactor = (float)Math.random() * 2 - 1;
        velocity = new Vector3f(xFactor,-1 * 2, zFactor);

        lifespan = maxLifeSpan;
    }

    public void render(Matrix4f O2W, Vector3f cameraPosition){
        O2W.translate(getPosition());
        shader.setUniform("ObjectToWorld", O2W);
        //Guarenteed to be a billboard.
        particleShape.render(new Vector3f(O2W.m30(), O2W.m31(), O2W.m32()), cameraPosition);
    }

    @Override
    public Triangle[] getTriangles() {
        return particleShape.getTriangles();
    }

    @Override
    public void generateCollisionBox() {
        System.err.println("Not implemented yet.");
        System.exit(-1);
    }

    public void run(Matrix4f ObjectToWorld, Vector3f cameraPosition, boolean pureDraw){
        if (!pureDraw)update();
        render(ObjectToWorld, cameraPosition);
    }

    @Override
    public float getLength() {
        return particleShape.getLength();
    }

    @Override
    public float getHeight() {
        return particleShape.getHeight();
    }

    @Override
    public float getWidth() {
        return particleShape.getWidth();
    }
}
