package World.WorldObjects;

import Core.Shader;
import World.AbstractShapes.Cylinder;
import World.AbstractShapes.IcoSphere;
import World.AbstractShapes.Triangle;
import World.WorldObjects.CharacterModel;
import org.joml.Matrix4f;

public class HumanModel extends CharacterModel {
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

    public HumanModel(float height){
        this.height = height;
        leftArm = new Cylinder(ARM_THICKNESS, height * ARM_FACTOR);
        rightArm = new Cylinder(ARM_THICKNESS, height * ARM_FACTOR);
        body = new Cylinder(BODY_THICKNESS, height * BODY_FACTOR);
        leftLeg = new Cylinder(LEG_THICKNESS, height * LEG_FACTOR);
        rightLeg = new Cylinder(LEG_THICKNESS, height * LEG_FACTOR);

        head = new IcoSphere(BODY_THICKNESS, 2);
        leftHand = new IcoSphere(ARM_THICKNESS * 1.5f, 2);
        rightHand = new IcoSphere(ARM_THICKNESS * 1.5f, 2);

        angle = 1.0f;
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
    public float getLength() {
        return body.getLength();
    }

    @Override
    public float getHeight() {
        return leftLeg.getHeight() + body.getHeight() + head.getHeight();
    }

    @Override
    public float getWidth() {
        return body.getWidth();
    }

    @Override
    public void render() {
        throw new IllegalArgumentException("Don't call HumanModel.render(). Call HumanModel.render(Shader, Matrix4f)");
    }

    public void render(Matrix4f O2W){
        Matrix4f temp = new Matrix4f();
        /* render body */
        O2W.rotateX((float)Math.toRadians(-90), temp);
        shader.setUniform("ObjectToWorld", temp);
        transformMap.put(body, temp);
        body.render();

        /* render arms */
        O2W.translate((BODY_THICKNESS + ARM_THICKNESS), height * (BODY_FACTOR/2 - ARM_FACTOR/2), 0, temp);
        temp.rotateX((float)Math.toRadians(-90)); //Y-axis and Z-axis swap until temp resets.
        temp.rotateY((float)Math.toRadians(-20));
        //animation
        temp.translate(0,0,0.5f);
        temp.rotate((float)Math.toRadians(angle), 1,0,0);
        temp.translate(0,0,-0.5f);
        shader.setUniform("ObjectToWorld", temp);
        transformMap.put(leftArm, temp);
        leftArm.render();

        O2W.translate(-(BODY_THICKNESS + (ARM_THICKNESS * 0.25f)), height * (BODY_FACTOR/2 - ARM_FACTOR/2), 0, temp);
        temp.translate(0,ARM_FACTOR/2.0f,height * ARM_FACTOR / 2.0f);
        shader.setUniform("ObjectToWorld", temp);
        transformMap.put(rightArm, temp);
        rightArm.render();

        /* render legs */
        O2W.translate(-BODY_THICKNESS/2, -height * (BODY_FACTOR/2 + LEG_FACTOR/2),0, temp);
        temp.rotateX((float)Math.toRadians(-90));
        //animation
        temp.translate(0,0,(height * LEG_FACTOR/2.0f));
        temp.rotate((float)Math.toRadians(angle), 1, 0, 0);
        temp.translate(0,0,-(height * LEG_FACTOR/2.0f));
        shader.setUniform("ObjectToWorld", temp);
        transformMap.put(leftLeg, temp);
        leftLeg.render();

        O2W.translate(BODY_THICKNESS/2,-height * (BODY_FACTOR/2 + LEG_FACTOR/2), 0, temp);
        temp.rotateX((float)Math.toRadians(-90));
        //animation
        temp.translate(0,0,(height * LEG_FACTOR/2.0f));
        temp.rotate((float)Math.toRadians(-angle), 1, 0, 0);
        temp.translate(0,0,-(height * LEG_FACTOR/2.0f));
        shader.setUniform("ObjectToWorld", temp);
        transformMap.put(rightLeg, temp);
        rightLeg.render();

        /* render head */
        O2W.translate(0, height * (BODY_FACTOR/2 + HEAD_FACTOR), 0, temp);
        shader.setUniform("ObjectToWorld", temp);
        transformMap.put(head, temp);
        head.render();

        /* render hands */
        O2W.translate((BODY_THICKNESS + ARM_THICKNESS), height * (BODY_FACTOR/2 - ARM_FACTOR/2), 0, temp);
        temp.rotateZ((float)Math.toRadians(20));
        temp.translate(0,-ARM_FACTOR*2, 0);
        //animation
        temp.rotateX((float)Math.toRadians(-90));
        temp.translate(0,0,0.5f + (ARM_FACTOR * height/2));
        temp.rotate((float)Math.toRadians(angle), 1,0,0);
        temp.translate(0,0,-0.5f - (ARM_FACTOR * height/2));
        shader.setUniform("ObjectToWorld", temp);
        transformMap.put(leftHand, temp);
        leftHand.render();

        O2W.translate(-(BODY_THICKNESS + (ARM_THICKNESS * 0.25f)), height * (BODY_FACTOR/2 - ARM_FACTOR/2), 0, temp);
        temp.translate(0,ARM_FACTOR/2.0f,height*ARM_FACTOR);
        shader.setUniform("ObjectToWorld", temp);
        transformMap.put(rightHand, temp);
        rightHand.render();
    }

    @Override
    public Triangle[] getTriangles() {
        triangles = new Triangle[
                head.getTriangles().length
                + body.getTriangles().length
                + leftArm.getTriangles().length
                + leftHand.getTriangles().length
                + rightArm.getTriangles().length
                + rightHand.getTriangles().length
                + leftLeg.getTriangles().length
                + rightLeg.getTriangles().length];

        Triangle[] headTri = transformTriangleArray(head.getTriangles(), transformMap.get(head));
        Triangle[] bodyTri = transformTriangleArray(body.getTriangles(), transformMap.get(body));
        Triangle[] leftArmTri = transformTriangleArray(leftArm.getTriangles(), transformMap.get(leftArm));
        Triangle[] leftHandTri = transformTriangleArray(leftHand.getTriangles(), transformMap.get(leftHand));
        Triangle[] rightArmTri = transformTriangleArray(rightArm.getTriangles(), transformMap.get(rightArm));
        Triangle[] rightHandTri = transformTriangleArray(rightHand.getTriangles(), transformMap.get(rightHand));
        Triangle[] leftLegTri = transformTriangleArray(leftLeg.getTriangles(), transformMap.get(leftLeg));
        Triangle[] rightLegTri = transformTriangleArray(rightLeg.getTriangles(), transformMap.get(rightLeg));
        int index = 0;
        for (int i = index; i < headTri.length; i++) triangles[i] = headTri[i];
        index += headTri.length;
        for (int i = index; i < bodyTri.length+index; i++) triangles[i] = bodyTri[i-index];
        index += bodyTri.length;
        for (int i = index; i < leftArmTri.length+index; i++) triangles[i] = leftArmTri[i-index];
        index += leftArmTri.length;
        for (int i = index; i < leftHandTri.length+index; i++) triangles[i] = leftHandTri[i-index];
        index += leftHandTri.length;
        for (int i = index; i < rightArmTri.length+index; i++) triangles[i] = rightArmTri[i-index];
        index += rightArmTri.length;
        for (int i = index; i < rightHandTri.length+index; i++) triangles[i] = rightHandTri[i-index];
        index += rightHandTri.length;
        for (int i = index; i < leftLegTri.length+index; i++) triangles[i] = leftLegTri[i-index];
        index += leftLegTri.length;
        for (int i = index; i < rightLegTri.length+index; i++) triangles[i] = rightLegTri[i-index];
        //index += rightLegTri.length;
        return triangles;
    }
}
