package World.WorldObjects;

import Core.Shader;
import World.AbstractShapes.Cone;
import World.AbstractShapes.Cylinder;
import World.AbstractShapes.Triangle;
import World.TriangleMesh;
import World.Material;
import World.WorldObjects.CompositeShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Tree extends CompositeShape {
    Cylinder trunk;
    Cone top;
    Vector3f location;

    public Tree(Cylinder cyl, Cone cone, Vector3f v){
        trunk = cyl;
        top = cone;
        location = v;
        setPosition(location);
    }

    @Override
    public void init(){
        trunk.init();
        top.init();
    }

    @Override
    public void render(){
        System.err.println("Call Tree.render(Shader) instead.");
        System.exit(-1);
    }

    public void render(Shader shader){
        Matrix4f rotation = new Matrix4f();
        rotation.rotateX((float)Math.toRadians(-90), rotation);

        Matrix4f attachment = new Matrix4f();
        attachment.translate(new Vector3f(0,0,1), attachment);

        Matrix4f deploy = new Matrix4f();
        deploy.translate(location);
        Matrix4f forCylinders = new Matrix4f(deploy);
        Matrix4f forCones = new Matrix4f(deploy);

        Material m = new Material();
        //draw Trunk
        m.Kd = new Vector3f(0.38f, 0.2f, 0.07f); //brown
        m.Ka = m.Kd;
        m.setUniforms(shader);

        Matrix4f temp = new Matrix4f();
        forCylinders.translate(0,trunk.getHeight()/2.0f, 0);
        forCylinders.mul(rotation, temp);
        shader.setUniform("ObjectToWorld", temp);
        transformMap.put(trunk, temp);
        trunk.render();

        //draw Top
        m = new Material();
        m.Kd = new Vector3f(0.1f, 0.6f, 0.1f); //green
        m.Ka = m.Kd;
        m.setUniforms(shader);

        forCones.translate(0,top.getHeight()/2.0f + trunk.getHeight() - 1, 0);
        forCones.mul(rotation.mul(attachment), temp);
        shader.setUniform("ObjectToWorld", temp);
        transformMap.put(top, temp);
        top.render();
    }

    @Override
    public Triangle[] getTriangles() {
        triangles = new Triangle[trunk.getTriangles().length + top.getTriangles().length];
        Triangle[] cylTri = transformTriangleArray(trunk.getTriangles(), transformMap.get(trunk));
        Triangle[] icoTri = transformTriangleArray(top.getTriangles(), transformMap.get(top));
        for (int i = 0; i < cylTri.length; i++) triangles[i] = cylTri[i];
        for (int i = cylTri.length; i < icoTri.length+cylTri.length; i++) triangles[i] = icoTri[i-cylTri.length];
        return triangles;
    }

    @Override
    public float getLength(){
        return top.getLength();
    }

    @Override
    public float getHeight(){
        return top.getHeight() + trunk.getHeight();
    }

    @Override
    public float getWidth(){
        return top.getWidth();
    }

    public Vector3f getPositionalOffset(){
        return new Vector3f(0,(trunk.getHeight() + top.getHeight() )/ 2.0f,0);
    }
}
