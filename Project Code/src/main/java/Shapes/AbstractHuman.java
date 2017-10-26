package Shapes;

import Core.Shader;
import Core.TriangleMesh;
import org.joml.Matrix4f;

public class AbstractHuman extends TriangleMesh {
    private Cylinder leftArm, rightArm, body, leftLeg, rightLeg;
    private IcoSphere head, leftHand, rightHand;
    private float height;

    private final float BODY_FACTOR = 0.4f;
    private final float ARM_FACTOR = BODY_FACTOR * 0.66f;
    private final float LEG_FACTOR = BODY_FACTOR * 0.625f;
    private final float HEAD_FACTOR = BODY_FACTOR * 0.1875f;

    private final float BODY_THICKNESS = 0.4f;
    private final float ARM_THICKNESS = BODY_THICKNESS/3.5f;
    private final float LEG_THICKNESS = BODY_THICKNESS/2;

    public AbstractHuman(float height){
        this.height = height;
        leftArm = new Cylinder(ARM_THICKNESS, height * ARM_FACTOR);
        rightArm = new Cylinder(ARM_THICKNESS, height * ARM_FACTOR);
        body = new Cylinder(BODY_THICKNESS, height * BODY_FACTOR);
        leftLeg = new Cylinder(LEG_THICKNESS, height * LEG_FACTOR);
        rightLeg = new Cylinder(LEG_THICKNESS, height * LEG_FACTOR);

        head = new IcoSphere(BODY_THICKNESS, 2);
        leftHand = new IcoSphere(ARM_THICKNESS * 1.5f, 2);
        rightHand = new IcoSphere(ARM_THICKNESS * 1.5f, 2);
    }

    @Override
    public void init() {
        leftArm.init();
        rightArm.init();
        body.init();
        leftLeg.init();
        rightLeg.init();
        head.init();
        leftHand.init();
        rightHand.init();
    }

    @Override
    public void render() {
        throw new IllegalArgumentException("Don't call AbstractHuman.render(). Call AbstractHuman.render(Shader, Matrix4f)");
    }

    public void render(Shader shader, Matrix4f O2W){
        Matrix4f temp = new Matrix4f();
        //render body
        O2W.rotateX((float)Math.toRadians(-90), temp);
        shader.setUniform("ObjectToWorld", temp);
        body.render();

        //render arms
        O2W.translate((BODY_THICKNESS + ARM_THICKNESS), height * (BODY_FACTOR/2 - ARM_FACTOR/2), 0, temp);
        temp.rotateX((float)Math.toRadians(-90)); //Y-axis and Z-axis swap until temp resets.
        temp.rotateY((float)Math.toRadians(-20));
        shader.setUniform("ObjectToWorld", temp);
        leftArm.render();
        O2W.translate(-(BODY_THICKNESS + ARM_THICKNESS), height * (BODY_FACTOR/2 - ARM_FACTOR/2), 0, temp);
        temp.rotateX((float)Math.toRadians(-90));
        temp.rotateY((float)Math.toRadians(20));
        shader.setUniform("ObjectToWorld", temp);
        rightArm.render();
        //render legs
        O2W.translate(-BODY_THICKNESS/2, -height * (BODY_FACTOR/2 + LEG_FACTOR/2),0, temp);
        temp.rotateX((float)Math.toRadians(-90));
        shader.setUniform("ObjectToWorld", temp);
        leftLeg.render();
        O2W.translate(BODY_THICKNESS/2,-height * (BODY_FACTOR/2 + LEG_FACTOR/2), 0, temp);
        temp.rotateX((float)Math.toRadians(-90));
        shader.setUniform("ObjectToWorld", temp);
        rightLeg.render();
        //render head
        O2W.translate(0, height * (BODY_FACTOR/2 + HEAD_FACTOR), 0, temp);
        shader.setUniform("ObjectToWorld", temp);
        head.render();
        //render hands
        O2W.translate((BODY_THICKNESS + ARM_THICKNESS), height * (BODY_FACTOR/2 - ARM_FACTOR/2), 0, temp);
        temp.rotateZ((float)Math.toRadians(20));
        temp.translate(0,-ARM_FACTOR*2, 0);
        shader.setUniform("ObjectToWorld", temp);
        leftHand.render();
        O2W.translate(-(BODY_THICKNESS + ARM_THICKNESS), height * (BODY_FACTOR/2 - ARM_FACTOR/2), 0, temp);
        temp.rotateZ((float)Math.toRadians(-20));
        temp.translate(0,-ARM_FACTOR*2, 0);
        shader.setUniform("ObjectToWorld", temp);
        rightHand.render();


    }
}
