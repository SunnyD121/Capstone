package World.WorldObjects;

import Core.Shader;
import World.AbstractShapes.Cylinder;
import World.AbstractShapes.IcoSphere;
import World.AbstractShapes.Triangle;
import World.Material;
import World.SceneEntity;
import World.TriangleMesh;
import World.WorldObjects.CompositeShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;

public class Lamp extends CompositeShape {

    private Cylinder cyl;
    private IcoSphere ico;
    private float lampHeight;
    private Vector3f location;


    public Lamp(Cylinder cyl, IcoSphere ico, Vector4f data){
        this.cyl = cyl;
        this.ico = ico;
        lampHeight = data.w;
        location = new Vector3f(data.x, data.y, data.z);
        setPosition(location);
    }

    @Override
    public float getLength() {
        return ico.getLength();
    }

    @Override
    public float getHeight() {
        return cyl.getHeight() + ico.getHeight() / 2.0f;
    } // but the bulb is down by a radius.

    @Override
    public float getWidth() {
        return ico.getLength();
    }

    public float getLightHeight(){
        return cyl.getHeight() ;//+ (ico.getHeight()/2.0f);
    }

    @Override
    public void init(){
        cyl.init();
        ico.init();
    }

    @Override
    public void render(){
        System.err.println("Call Lamp.render(Shader) instead.");
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
        Matrix4f forSpheres = new Matrix4f(deploy);

        Material m = new Material();
        //draw Lamp post
        m.Kd = new Vector3f(0);
        m.Ka = m.Kd;
        m.setUniforms(shader);

        Matrix4f temp = new Matrix4f();
        forCylinders.translate(0,cyl.getHeight()/2.0f, 0);
        forCylinders.mul(rotation, temp);
        shader.setUniform("ObjectToWorld", temp);
        transformMap.put(cyl, temp);
        cyl.render();

        //draw Lamp bulb
        m = new Material();
        m.Kd = new Vector3f(100.0f);
        m.Ka = m.Kd;
        m.Ks = new Vector3f(1.0f);
        m.Le = new Vector3f(1.0f);
        m.shine = 1.0f;
        m.setUniforms(shader);
        forSpheres.translate(0, cyl.getHeight(), 0, temp);
        shader.setUniform("ObjectToWorld", temp);
        transformMap.put(ico, temp);
        ico.render();
    }

    @Override
    public Triangle[] getTriangles(){
        triangles = new Triangle[cyl.getTriangles().length + ico.getTriangles().length];
        Triangle[] cylTri = transformTriangleArray(cyl.getTriangles(), transformMap.get(cyl));
        Triangle[] icoTri = transformTriangleArray(ico.getTriangles(), transformMap.get(ico));
        for (int i = 0; i < cylTri.length; i++) triangles[i] = cylTri[i];
        for (int i = cylTri.length; i < icoTri.length+cylTri.length; i++) triangles[i] = icoTri[i-cylTri.length];
        return triangles;
    }

    public Vector3f getPositionalOffset() {
        return new Vector3f(0, (cyl.getHeight() + ico.getHeight() / 2.0f) / 2.0f, 0);
    }
}
