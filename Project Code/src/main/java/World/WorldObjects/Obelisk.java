package World.WorldObjects;

import Core.Shader;
import World.AbstractShapes.Prism;
import World.AbstractShapes.Pyramid;
import World.AbstractShapes.Triangle;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Obelisk extends CompositeShape{

    private final float RATIO = 0.8f;

    private Pyramid pyramid;
    private Prism prism;
    private float height, baseLength;
    private Vector3f positionalOffset;

    public Obelisk(Vector3f position, float baseLength, float height){
        this.baseLength = baseLength;
        this.height = height;
        this.position = position;

        positionalOffset = new Vector3f(0,height/2,0);

        prism = new Prism(Prism.getRadiusFromSideLength(baseLength, 4),height * RATIO,4);
        pyramid = new Pyramid(baseLength, height * (1-RATIO));
    }

    @Override
    public void render() {

        Matrix4f mat = new Matrix4f();
        //pyramid top
        mat.translate(this.getPosition());
        mat.rotateY((float)Math.toRadians(45), mat);
        mat.translate(new Vector3f(0,height*RATIO+pyramid.getHeight()/2,0));

        shader.setUniform("ObjectToWorld", mat);
        transformMap.put(pyramid, mat);
        pyramid.render();

        //prism body
        mat = new Matrix4f();
        mat.translate(this.getPosition());
        mat.translate(new Vector3f(0,(height*RATIO)/2,0));
        mat.rotateX((float)Math.toRadians(-90), mat);

        shader.setUniform("ObjectToWorld", mat);
        transformMap.put(prism, mat);
        prism.render();
    }

    @Override
    public void init() {
        prism.init();
        pyramid.init();
    }

    @Override
    public float getLength() {
        return baseLength;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public float getWidth() {
        return baseLength;
    }

    public Vector3f getPositionalOffset(){
        return positionalOffset;
    }

    @Override
    public Triangle[] getTriangles() {
        triangles = new Triangle[prism.getTriangles().length + pyramid.getTriangles().length];
        Triangle[] array1 = transformTriangleArray(prism.getTriangles(), transformMap.get(prism));
        Triangle[] array2 = transformTriangleArray(pyramid.getTriangles(), transformMap.get(pyramid));
        for (int i = 0; i < array1.length; i++) triangles[i] = array1[i];
        for (int i = array1.length; i < array2.length+array1.length; i++) triangles[i] = array2[i-array1.length];
        return triangles;
    }
}
