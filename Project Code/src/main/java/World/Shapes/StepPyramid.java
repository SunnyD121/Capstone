package World.Shapes;

import Core.Shader;
import World.TriangleMesh;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

/**
 * Created by (User name) on 8/12/2017.
 */
public class StepPyramid extends TriangleMesh implements CompositeShape{

    private ArrayList<RectangularPrism> prisms;
    private float baselength;
    private float stepheight;
    private float stepshrink;
    private boolean mirrored;

    public StepPyramid(Vector3f location, float baselength, float stepheight, float stepshrink, boolean mirrored){
        if (stepshrink <= 0)
            throw new IllegalArgumentException("StepPyramid: Invalid stepshrink. Values must be above zero.");
        setPosition(location);
        this.baselength = baselength;
        this.stepheight = stepheight;
        this.stepshrink = stepshrink;
        this.mirrored = mirrored;
        prisms = new ArrayList<>();
    }

    @Override
    public float getLength() {
        return baselength;
    }

    @Override
    public float getHeight() {
        return prisms.size() * stepheight;
    }

    @Override
    public float getWidth() {
        return baselength;
    }

    @Override
    public void init(){
        createPrisms();
        if (mirrored) createPrisms();
    }

    @Override
    public void render(){
        throw new IllegalArgumentException("Don't call StepPyramid.render(). Call StepPyramid.render(Shader, Matrix4f)");
    }

    public void render(Shader shader, Matrix4f ObjectToWorld){
        drawPrisms(shader, ObjectToWorld);
        if (mirrored) drawMirroredPrisms(shader, ObjectToWorld);

    }

    private void createPrisms(){
        float temp =  baselength;
        while (temp > 0){
            prisms.add(new RectangularPrism(temp, temp, stepheight));
            //prisms.at(0).init(gl);	//dunno why this doesn't work. oh well.
            temp -= stepshrink;
        }
        for (RectangularPrism p : prisms){
            p.init();
        }
    }

    private void drawPrisms(Shader shader, Matrix4f ObjectToWorld){
        Matrix4f stepUp;
        Matrix4f OTWcopy;
        int limit = prisms.size();
        if (mirrored) limit /= 2;
        for (int i = 0; i < limit; i++) {
            stepUp = new Matrix4f();
            OTWcopy = new Matrix4f(ObjectToWorld);
            stepUp.translate(0, i * stepheight, 0);
            stepUp = OTWcopy.mul(stepUp);
            shader.setUniform("ObjectToWorld", stepUp);
            prisms.get(i).render();

        }
        //reset ObjectToWorld
        shader.setUniform("ObjectToWorld", ObjectToWorld);
    }

    private void drawMirroredPrisms(Shader shader, Matrix4f ObjectToWorld){
        Matrix4f stepDown;
        Matrix4f OTWcopy;
        int limit = prisms.size();
        limit /= 2;
        for(int i = limit; i < prisms.size(); i++){
            stepDown = new Matrix4f();
            OTWcopy = new Matrix4f(ObjectToWorld);
            stepDown.translate(0, (i-limit) * -stepheight, 0);
            stepDown = OTWcopy.mul(stepDown);
            shader.setUniform("ObjectToWorld", stepDown);
            prisms.get(i).render();
        }
        //reset ObjectToWorld
        shader.setUniform("ObjectToWorld", ObjectToWorld);
    }

    public float getBaseRectHeight(){
        return stepheight;
    }

    public Vector3f getPositionalOffset(){
        float heightComponent = getHeight() / 2.0f;
        if (mirrored) heightComponent = 0;
        return new Vector3f(0, heightComponent, 0);
    }

}
