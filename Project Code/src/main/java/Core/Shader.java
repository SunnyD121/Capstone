package Core;

import Utilities.Utilities;
import com.jogamp.opengl.util.GLBuffers;
import com.sun.istack.internal.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static Core.GLListener.gl;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_INFO_LOG_LENGTH;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;

/**
 * Created by (User name) on 7/27/2017.
 */
public class Shader {

    private int m_programID = 0;

    private final int VERTEX = gl.GL_VERTEX_SHADER;
    private final int FRAGMENT = gl.GL_FRAGMENT_SHADER;
    private final int GEOMETRY = gl.GL_GEOMETRY_SHADER;
    private final int TESS_CONTROL = gl.GL_TESS_CONTROL_SHADER;
    private final int TESS_EVALUATION = gl.GL_TESS_EVALUATION_SHADER;

    public Shader(){

    }

    public void compileStageFile(final String fileName) throws ShaderException{
        int stage = VERTEX;
        if( endsWith(fileName, ".vert") || endsWith(fileName, "_vert.glsl") ||
                endsWith(fileName, ".vert.glsl") )
        {
            stage = VERTEX;
        }
        else if( endsWith(fileName, ".frag") || endsWith(fileName, "_frag.glsl") ||
                endsWith(fileName, ".frag.glsl") )
        {
            stage = FRAGMENT;
        }
        else if( endsWith(fileName, ".geom") || endsWith(fileName, "_geom.glsl") ||
                endsWith(fileName, ".geom.glsl") )
        {
            stage = GEOMETRY;
        }
        else if( endsWith(fileName, ".tcs") || endsWith(fileName, "_tcs.glsl") ||
                endsWith(fileName, ".tcs.glsl") )
        {
            stage = TESS_CONTROL;
        }
        else if( endsWith(fileName, ".tes") || endsWith(fileName, "_tes.glsl") ||
                endsWith(fileName, ".tes.glsl") )
        {
            stage = TESS_EVALUATION;
        }
        else
        {
            throw new ShaderException("Unable to determine shader stage from file name", null);
        }

        compileStageFile(stage, fileName);
    }
    public void compileStageFile(int stage, final String fileName) throws ShaderException{
        String code = getFileContents(fileName);
        compileStage(stage, code, fileName);
    }
    public void compileStage(int stage, final String code, @Nullable String fileName) throws ShaderException {
        if (fileName == null)
            fileName = "";
        int stageId = gl.glCreateShader(stage);

        String[] pCode = new String[]{code};    //TODO: might not be necessary, investigate later
        gl.glShaderSource(stageId, 1, pCode, null);
        gl.glCompileShader(stageId);

        IntBuffer status = GLBuffers.newDirectIntBuffer(new int[]{0});
        gl.glGetShaderiv(stageId, GL_COMPILE_STATUS, status);
        if (0 == status.get(0))     //C translation: if (false == status)
        {
            String log = generateLogs(stageId);
            String msg = "Failed to compile shader: " + fileName + "\n";
            throw new ShaderException(msg, log);
        } else {
            if(m_programID == 0)
            {
                m_programID = gl.glCreateProgram();
            }
            gl.glAttachShader(m_programID, stageId);
        }
    }
    public void link() throws ShaderException{
        if(m_programID == 0){
            throw new ShaderException("Cannot link shader without any stages.", null);
        }
        gl.glLinkProgram(m_programID);

        IntBuffer status = GLBuffers.newDirectIntBuffer(new int[]{0});
        gl.glGetProgramiv(m_programID, GL_LINK_STATUS, status);
        if (0 == status.get(0))     //C translation: if (false == status)
        {
            String log = generateLogs(m_programID);
            throw new ShaderException("Shader link failed: \n", log);
        }

    }
    public void use() throws ShaderException{
        if(m_programID == 0)
            throw new ShaderException("Shader has not been compiled/linked.", null);

        gl.glUseProgram(m_programID);
    }

    public void setUniform(final String name, final Vector4f val){
        setUniform(name, val.x, val.y, val.z, val.w);
    }
    public void setUniform(final String name, float x, float y, float z, float w){
        gl.glUniform4f(getUniformLocation(name), x,y,z,w);
    }
    public void setUniform(final String name, final Vector3f val){
        setUniform(name, val.x, val.y, val.z);
    }
    public void setUniform(final String name, float x, float y, float z){
        gl.glUniform3f(getUniformLocation(name), x,y,z);
    }
    public void setUniform(final String name, final Matrix4f val){
        FloatBuffer fb = GLBuffers.newDirectFloatBuffer(16);
        val.get(fb);
        gl.glUniformMatrix4fv(getUniformLocation(name), 1, false, fb);
    }
    public void setUniform(final String name, final Matrix3f val){
        FloatBuffer fb = GLBuffers.newDirectFloatBuffer(9);
        val.get(fb);
        gl.glUniformMatrix3fv(getUniformLocation(name), 1, false, fb);
    }
    public void setUniform(final String name, int val){
        gl.glUniform1i(getUniformLocation(name), val);
    }
    public void setUniform(final String name, float val){
        gl.glUniform1f(getUniformLocation(name), val);
    }

    private String getFileContents(final String fileName) throws ShaderException{
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(fileName));
        }
        catch (FileNotFoundException e){
            String msg = "Unable to open shader file:  "+e.getMessage();
            throw new ShaderException(msg, null);
        }

        String code = "";
        String line = "";
        do{
            try {line = reader.readLine();}
            catch(IOException e){
                System.err.println("reader.readLine() failed:\n" + e.getMessage());
                System.exit(-1);
            }
            if (line == null)   //'==' acceptable; not comparing value.
                break;
            if ("".equals(line.trim()))
                code += "\n";
            else
                code += line + "\n";
        }
        while(!line.equals(null));
        return code;
    }
    private boolean endsWith(final String str, final String end){
        if (str.length() < end.length()) return false;
        String endStr = str.substring(str.length() - end.length(), str.length());
        return endStr.equals(end);
    }
    private int getUniformLocation(final String name){
        return gl.glGetUniformLocation(m_programID, name);
    }

    private String generateLogs(int stageId){
        IntBuffer logLen = GLBuffers.newDirectIntBuffer(new int[1]);
        gl.glGetShaderiv(stageId, GL_INFO_LOG_LENGTH, logLen);

        ByteBuffer bytes = GLBuffers.newDirectByteBuffer(new byte[logLen.get(0)]);
        gl.glGetShaderInfoLog(stageId, logLen.get(0), null, bytes);

        gl.glDeleteShader(stageId);
        return Utilities.bytesToString(bytes);
    }

    public int getProgramID(){
        return m_programID;
    }

    public class ShaderException extends Exception{
        private String m_log;
        private String m_msg;

        public ShaderException(final String msg, @Nullable String log){
            if (log == null){
                 log = "";
            }
            m_log = log;
            m_msg = msg;
        }
        public final String getOpenGLLog(){
            return m_log;
        }
        public final String what(){
            return m_msg;
        }


    }


}
