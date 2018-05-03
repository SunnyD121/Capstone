package World.WorldObjects.Particles;

import Core.Shader;
import World.AbstractShapes.BillBoardQuad;
import World.AbstractShapes.Cube;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Sand extends Particle{

    public Sand(Vector3f loc){
        particleShape = new BillBoardQuad(0.2f);
        particleShape.init();

//        setPosition( new Vector3f(((float)Math.random() - 0.5f) * 200, 0, ((float)Math.random() -0.5f) * 200) );   //spread out over plane of 100,0,100
        setPosition(new Vector3f(((float)Math.random() - 0.5f) * 240, ((float)Math.random() - 0.5f) * 15, 0) );
        setPosition(getPosition().add(loc));  //move to emitter position

        float xFactor = (float)Math.random() * 2 - 1;
        float yFactor = (float)Math.random() * 2 - 1;
        velocity = new Vector3f(xFactor, yFactor, -1 * 20);
        acceleration = new Vector3f(xFactor * 0.1f, yFactor * 0.1f, -2 * 0.1f).div(4);

        maxLifeSpan = 140;
        lifespan = maxLifeSpan;
    }

    @Override
    public void renew(Vector3f emitterPos) {
        setPosition(new Vector3f(((float)Math.random() - 0.5f) * 240, ((float)Math.random() - 0.5f) * 15, 0) );
        setPosition(getPosition().add(emitterPos));  //move to emitter position
        float xFactor = (float)Math.random() * 2 - 1;
        float yFactor = (float)Math.random() * 2 - 1;
        velocity = new Vector3f(xFactor, yFactor, -1 * 20);
        acceleration = new Vector3f(xFactor * 0.1f, yFactor * 0.1f, -2 * 0.1f).div(4);
        lifespan = maxLifeSpan;
    }

    @Override
    public void generateCollisionBox() {
        System.err.println("Not implemented yet.");
        System.exit(-1);
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

    public void run(Matrix4f o2w, Vector3f cameraPos, boolean pureDraw){
        if (!pureDraw)update();
        render(o2w, cameraPos);
    }

    public void render(Matrix4f o2w, Vector3f camPos){
        o2w.translate(getPosition());
        shader.setUniform("ObjectToWorld", o2w);
        //Guaranteed to be billboard
        particleShape.render(new Vector3f(o2w.m30(), o2w.m31(), o2w.m32()), camPos);
    }
}
