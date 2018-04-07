package World;

import Core.Shader;
import World.Material;
import World.SceneEntity;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static Core.GLListener.gl;

/**
 * Created by (User name) on 7/25/2017.
 */
public abstract class TriangleMesh extends SceneEntity {

    protected int vao = 0;
    protected int elements = 0;
    protected int[] buffers;

    public abstract void init();

    public void render(){
        if (vao != 0){
            gl.glBindVertexArray(vao);
            gl.glDrawElements(GL.GL_TRIANGLES, elements, GL.GL_UNSIGNED_INT, 0);
            gl.glBindVertexArray(0);
        }
        else{
            System.err.println("Rendering unsuccessful; vao = 0");
            System.exit(-1);
        }
    }

    public void render(Shader shader, Matrix4f m){
        System.out.println("This is an error. Why have you come here?");
    }
    public void render(Shader shader, Vector3f v1, Vector3f v2){System.out.println("This is an error. Why have you come here?");}

    public void renderWithLines(Shader shader, Vector3f color, Vector3f lineColor){
        Material m = new Material();

        gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_FILL);
        m.Kd = color;
        m.setUniforms(shader);
        render();

        gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_LINE);
        m.Kd = lineColor;
        m.setUniforms(shader);
        render();

        //reset for normal rendering.
        gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_FILL);
    }

    public void renderOnlyLines(Shader shader, Vector3f lineColor){
        Material m = new Material();

        gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_LINE);
        m.Kd = lineColor;
        m.setUniforms(shader);
        render();

        //reset
        gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_FILL);
    }

    final public void initGpuVertexArrays(
            IntBuffer order,          // the order the lines (in a triangle) are drawn.
            FloatBuffer points,         // the location of each point in 3D space
            FloatBuffer normals,        // the normal vectors
            FloatBuffer colors,         // the color data
            FloatBuffer textureCoords   // the texture coordinate data.
    )
    {
        if(order == null || points == null){
            System.err.println("initGpuVertexArrays: the index data and position data must exist.");
            System.exit(-1);
        }

        elements = order.capacity();

        int bufferCount = 2;    //minimum needed, for order and points buffers. max is 5, see parameter list.
        if( normals != null ) bufferCount++;
        if( colors != null ) bufferCount++;
        if( textureCoords != null ) bufferCount++;

        createBuffers(bufferCount);     //clears buffers and reinitializes it. (???)
        IntBuffer intBuffer = GLBuffers.newDirectIntBuffer(buffers);
        //intBuffer = {0, 0, 0 or null, 0 or null, 0 or null}
        gl.glGenBuffers(bufferCount, intBuffer);
        //intBuffer = {1, 2, 3 or null, 4 or null, 5 or null}

        int bufIndex =0;
        int bufName = intBuffer.get(bufIndex);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufName);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, order.capacity() * Integer.BYTES, order, GL.GL_STATIC_DRAW );

        bufIndex++;
        bufName = intBuffer.get(bufIndex);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufName);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, points.capacity() * Float.BYTES, points, GL.GL_STATIC_DRAW );

        if(normals != null) {
            bufIndex++;
            bufName = intBuffer.get(bufIndex);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufName);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, normals.capacity() * Float.BYTES, normals, GL.GL_STATIC_DRAW);
        }

        if(colors != null) {
            bufIndex++;
            bufName = intBuffer.get(bufIndex);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufName);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, colors.capacity() * Float.BYTES, colors, GL.GL_STATIC_DRAW);

        }

        if(textureCoords != null) {
            bufIndex++;
            bufName = intBuffer.get(bufIndex);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufName);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, textureCoords.capacity() * Float.BYTES, textureCoords, GL.GL_STATIC_DRAW);
        }

        IntBuffer vaoIDs = GLBuffers.newDirectIntBuffer(new int[] {0});
        gl.glGenVertexArrays(1, vaoIDs);

        vao = vaoIDs.get(0);
        gl.glBindVertexArray(vao);

        bufIndex = 0;
        bufName = intBuffer.get(bufIndex);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, bufName);

        bufIndex++;
        bufName = intBuffer.get(bufIndex);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufName);
        gl.glVertexAttribPointer(1,3,GL.GL_FLOAT,false,0,0);
        gl.glEnableVertexAttribArray(1);    //this parameter matches the layout value in vertex shader

        if(normals != null){
            bufIndex++;
            bufName = intBuffer.get(bufIndex);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufName);
            gl.glVertexAttribPointer(2,3,GL.GL_FLOAT,false,0,0);
            gl.glEnableVertexAttribArray(2);
        }

        if(colors != null){
            bufIndex++;
            bufName = intBuffer.get(bufIndex);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufName);
            gl.glVertexAttribPointer(3,4,GL.GL_FLOAT,false,0,0);
            gl.glEnableVertexAttribArray(3);
        }

        if(textureCoords != null){
            bufIndex++;
            bufName = intBuffer.get(bufIndex);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufName);
            gl.glVertexAttribPointer(4,2,GL.GL_FLOAT,false,0,0);
            gl.glEnableVertexAttribArray(4);
        }

        gl.glBindVertexArray(0);
    }

    private void createBuffers(int num){
        ArrayList<Integer> buffersTemp = new ArrayList<>();
        for(int i=0; i < num; i++) {
            buffersTemp.add(buffersTemp.size(), 0);
        }
        buffers = new int[buffersTemp.size()];  //clears the buffers
        for(int i=0;i<buffers.length;i++){
            buffers[i] = buffersTemp.get(i);
        }
    }





}
