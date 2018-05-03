package World.WorldObjects;

import Core.Shader;
import World.AbstractShapes.Cylinder;
import World.AbstractShapes.Triangle;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Cactus extends CompositeShape {

    private float size;
    private float height;
    private Cylinder trunk, leftSide,leftArm,rightSide, rightArm;

    public Cactus(Vector3f location, int size){
        this.position = location;
        this.size = size / 2.0f;
        this.height = this.size * 2;

        trunk = new Cylinder(0.1f * this.size, height);
        leftSide = new Cylinder(0.1f * this.size, height / 5);
        rightSide = new Cylinder(0.1f * this.size, height / 5);
        leftArm = new Cylinder(0.1f * this.size, height / 2);
        rightArm = new Cylinder(0.1f * this.size, height / 3);
    }

    @Override
    public void init() {
        trunk.init();
        leftSide.init();
        rightSide.init();
        leftArm.init();
        rightArm.init();
    }

    @Override
    public void render() {
        Matrix4f transform = new Matrix4f();

        transform.translate(this.getPosition());
        transform.translate(0,height/2, 0);
        transform.rotateX((float)Math.toRadians(90));
        shader.setUniform("ObjectToWorld", transform);
        transformMap.put(trunk, transform);
        trunk.render();

        transform = new Matrix4f().translate(this.getPosition());
        transform.translate(new Vector3f(0, height * 0.25f, height/(5 * 2)));
        shader.setUniform("ObjectToWorld", transform);
        transformMap.put(leftSide, transform);
        leftSide.render();

        transform = new Matrix4f().translate(this.getPosition());
        transform.translate(new Vector3f(0, height * 0.5f, -height/(5 * 2)));
        shader.setUniform("ObjectToWorld", transform);
        transformMap.put(rightSide, transform);
        rightSide.render();

        transform = new Matrix4f().translate(this.getPosition());
        transform.translate(0, height * (0.25f * 2), height/5);
        transform.rotateX((float)Math.toRadians(90));
        shader.setUniform("ObjectToWorld", transform);
        transformMap.put(leftArm, transform);
        leftArm.render();


        transform = new Matrix4f().translate(this.getPosition());
        transform.translate(0, height * (0.66f), -height/5);
        transform.rotateX((float)Math.toRadians(90));
        shader.setUniform("ObjectToWorld", transform);
        transformMap.put(rightArm, transform);
        rightArm.render();

    }

    @Override
    public float getLength() {
        return 0.2f * size;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public float getWidth() {
        return height * 2 / 5;
    }

    public Vector3f getPositionalOffset(){
        return new Vector3f(0,height/2,0);
    }

    @Override
    public Triangle[] getTriangles() {
        triangles = new Triangle[
                trunk.getTriangles().length
                        + leftSide.getTriangles().length
                        + leftArm.getTriangles().length
                        + rightSide.getTriangles().length
                        + rightArm.getTriangles().length];

        Triangle[] array1 = transformTriangleArray(trunk.getTriangles(), transformMap.get(trunk));
        Triangle[] array2 = transformTriangleArray(leftSide.getTriangles(), transformMap.get(leftSide));
        Triangle[] array3 = transformTriangleArray(leftArm.getTriangles(), transformMap.get(leftArm));
        Triangle[] array4 = transformTriangleArray(rightSide.getTriangles(), transformMap.get(rightSide));
        Triangle[] array5 = transformTriangleArray(rightArm.getTriangles(), transformMap.get(rightArm));
        int index = 0;
        for (int i = index; i < array1.length; i++) triangles[i] = array1[i];
        index += array1.length;
        for (int i = index; i < array2.length+index; i++) triangles[i] = array2[i-index];
        index += array2.length;
        for (int i = index; i < array3.length+index; i++) triangles[i] = array3[i-index];
        index += array3.length;
        for (int i = index; i < array4.length+index; i++) triangles[i] = array4[i-index];
        index += array4.length;
        for (int i = index; i < array5.length+index; i++) triangles[i] = array5[i-index];
        //index += array5.length;
        return triangles;
    }
}
