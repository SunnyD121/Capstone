package Utilities;

import Core.CollisionDetectionSystem.BoundingBox;
import Core.CollisionDetectionSystem.FixedBoundingBox;
import org.joml.Vector3f;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class UtilitiesTest1 {

    @Test
    public void boxLineIntersectionTest() throws Exception {
        FixedBoundingBox box;
        Vector3f A, B;

        //(1,1,1) -> (3,5,3)
        box = new FixedBoundingBox(new Vector3f(1,1,1), new Vector3f(3,5,3), null);
        A = new Vector3f(2,3,-1);
        B = new Vector3f(2,3,6);
        assertTrue("z-axis->out-out, 1, true", testFunction(box, A, B));
        assertTrue("z-axis->out-out, 2, true", testFunction(box, B, A));

        //(1,1,1) -> (3,5,3)
        box = new FixedBoundingBox(new Vector3f(1,1,1), new Vector3f(3,5,3), null);
        A = new Vector3f(-1,3,2);
        B = new Vector3f(6,3,2);
        assertTrue("x-axis->out-out, 1, true", testFunction(box, A, B));
        assertTrue("x-axis->out-out, 2, true", testFunction(box, B, A));

        //(1,1,1) -> (3,5,3)
        box = new FixedBoundingBox(new Vector3f(1,1,1), new Vector3f(3,5,3), null);
        A = new Vector3f(2,3,2);
        B = new Vector3f(2,3,6);
        assertTrue("z-axis->in-out, 1, true", testFunction(box, A, B));
        assertTrue("z-axis->out-in, 2, true", testFunction(box, B, A));

        //(1,1,1) -> (3,5,3)
        box = new FixedBoundingBox(new Vector3f(1,1,1), new Vector3f(3,5,3), null);
        A = new Vector3f(2,3,2);
        B = new Vector3f(6,3,2);
        assertTrue("x-axis->in-out, 1, true", testFunction(box, A, B));
        assertTrue("x-axis->out-in, 2, true", testFunction(box, B, A));

        //(1,1,1) -> (3,5,3)
        box = new FixedBoundingBox(new Vector3f(1,1,1), new Vector3f(3,5,3), null);
        A = new Vector3f(-1,3,-1);
        B = new Vector3f(-1,3,6);
        assertFalse("z-axis->no intersect, false", testFunction(box, A, B));

        //(1,1,1) -> (3,5,3)
        box = new FixedBoundingBox(new Vector3f(1,1,1), new Vector3f(3,5,3), null);
        A = new Vector3f(-1,3,-1);
        B = new Vector3f(6,3,-1);
        assertFalse("x-axis->no intersect, false", testFunction(box, A, B));

        //(1,1,1) -> (3,5,3)
        box = new FixedBoundingBox(new Vector3f(1,1,1), new Vector3f(3,5,3), null);
        A = new Vector3f(1.5f,3,0.5f);
        B = new Vector3f(4,3,1.5f);
        assertTrue("diagonal_1->out-out, 1, true", testFunction(box, A, B));
        assertTrue("diagonal_1->out-out, 2, true", testFunction(box, B, A));

        //(1,1,1) -> (3,5,3)
        box = new FixedBoundingBox(new Vector3f(1,1,1), new Vector3f(3,5,3), null);
        A = new Vector3f(3.5f,3,1.5f);
        B = new Vector3f(1.5f,3,6f);
        assertTrue("diagonal_2->out-out, 1, true", testFunction(box, A, B));
        assertTrue("diagonal_2->out-out, 2, true", testFunction(box, B, A));

        //(1,1,1) -> (3,5,3)
        box = new FixedBoundingBox(new Vector3f(1,1,1), new Vector3f(3,5,3), null);
        A = new Vector3f(2.5f,3,3.5f);
        B = new Vector3f(-2,3,1.5f);
        assertTrue("diagonal_3->out-out, 1, true", testFunction(box, A, B));
        assertTrue("diagonal_3->out-out, 2, true", testFunction(box, B, A));

        //(1,1,1) -> (3,5,3)
        box = new FixedBoundingBox(new Vector3f(1,1,1), new Vector3f(3,5,3), null);
        A = new Vector3f(0.5f,3,2.5f);
        B = new Vector3f(2.5f,3,-2);
        assertTrue("diagonal_4->out-out, 1, true", testFunction(box, A, B));
        assertTrue("diagonal_4->out-out, 2, true", testFunction(box, B, A));

        //(1,1,1) -> (3,5,3)
        box = new FixedBoundingBox(new Vector3f(1,1,1), new Vector3f(3,5,3), null);
        A = new Vector3f(1.5f,3,1.5f);
        B = new Vector3f(6,3,2.5f);
        assertTrue("diagonal_5->in-out, 1, true", testFunction(box, A, B));
        assertTrue("diagonal_5->out-in, 2, true", testFunction(box, B, A));

        //(1,1,1) -> (3,5,3)
        box = new FixedBoundingBox(new Vector3f(1,1,1), new Vector3f(3,5,3), null);
        A = new Vector3f(10f,3,2f);
        B = new Vector3f(2,3,10f);
        assertFalse("diagonal->no intersect, 1, false", testFunction(box, A, B));

    }

    private boolean testFunction(BoundingBox box, Vector3f A, Vector3f B){
        return Utilities.boxLineIntersection(box, A, B);
    }

}
