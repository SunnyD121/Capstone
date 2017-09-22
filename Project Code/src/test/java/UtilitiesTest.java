import Core.Utilities;
import com.jogamp.opengl.util.GLBuffers;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.junit.Assert.*;

/**
 * Created by (User name) on 8/3/2017.
 */
public class UtilitiesTest {

    private static FloatBuffer floats;
    private static IntBuffer ints, test;
    private static ByteBuffer bytes;

    @BeforeClass
    public static void setup(){
        floats = GLBuffers.newDirectFloatBuffer(new float[]
                {0.1f, 2.0f, 3.7f, 7.3f, -10.4f}
                );
        ints = GLBuffers.newDirectIntBuffer(new int[]
                {0,2,5,7,-3}
                );
        test = GLBuffers.newDirectIntBuffer(new int[] {});
        bytes = GLBuffers.newDirectByteBuffer(new byte[]
                {72,69,76,76,79, 32, 87,79,82,76,68}
                );
    }

    @Test
    public void fBufToString() throws Exception {
        String string = Utilities.fBufToString(floats);
        assertEquals("fbuf: ","{0.1, 2.0, 3.7, 7.3, -10.4}", string );
    }

    @Test
    public void iBufToString() throws Exception {
        String string = Utilities.iBufToString(ints);
        assertEquals("ibuf: ","{0, 2, 5, 7, -3}", string );

        assertEquals("empty ibuf: ","{}", Utilities.iBufToString(test));
    }

    @Test
    public void bBufToString() throws Exception {
        String string = Utilities.bBufToString(bytes);
        assertEquals("bbuf: ", "{72, 69, 76, 76, 79, 32, 87, 79, 82, 76, 68}",string);
    }

    @Test
    public void bytesToString() throws Exception {
        String output = Utilities.bytesToString(bytes);
        assertEquals("bytes: ","HELLO WORLD", output);
    }

}