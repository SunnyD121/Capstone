package Utilities;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class BiMapTest {

    private static BiMap<Integer, Character> map;
    private BiMap<String, Float> testMap;
    @BeforeClass
    public static void createDataStructureGlobal(){
        map = new BiMap<Integer, Character>();
        map.put(7,'a');     //0
        map.put(6,'s');
        map.put(5,'d');
        map.put(3,'f');
        map.put(9,'g');
        map.put(1,'h');
        map.put(2,'j');
        map.put(4,'k');
        map.put(8,'k');
        map.put(11,'p');
        map.put(17,'o');    //10

        map.put(25,'w');    //11
        map.put(25,'x');
        map.put(75,'y');
        map.put(50,'y');
        map.put(66,'z');
        map.put(66,'z');    //16
    }

    @Before
    public void createDataStructureLocal(){
        testMap = new BiMap<String, Float>();
    }

    @Test
    public void getKeyFromValue() throws Exception {
        assertEquals(1,  (int)map.getKeyFromValue('h').get(0));
        assertEquals(4, (int)map.getKeyFromValue('k').get(0));
        assertEquals(11, (int)map.getKeyFromValue('p').get(0));

        assertEquals(75, (int)map.getKeyFromValue('y').get(0));
        assertEquals(50, (int)map.getKeyFromValue('y').get(1));
        assertEquals(66, (int)map.getKeyFromValue('z').get(0));
        assertEquals(66, (int)map.getKeyFromValue('z').get(1));

        assertNull(map.getKeyFromValue('B'));
    }

    @Test
    public void getValueFromKey() throws Exception {
        assertEquals('a', (char)map.getValueFromKey(7).get(0));
        assertEquals('a', (char)map.getValueFromKey(7).get(0));
        assertEquals('s', (char)map.getValueFromKey(6).get(0));
        assertEquals('f', (char)map.getValueFromKey(3).get(0));

        assertEquals('w', (char)map.getValueFromKey(25).get(0));
        assertEquals('x', (char)map.getValueFromKey(25).get(1));
        assertEquals('z', (char)map.getValueFromKey(66).get(0));
        assertEquals('z', (char)map.getValueFromKey(66).get(1));

        assertNull(map.getValueFromKey(-1));
    }

    @Test
    public void removeKey() throws Exception {
        testMap.put("one", 1f);
        testMap.put("two", 2f);
        testMap.put("three", 3f);
        testMap.put("four", 4f);
        testMap.put("one", 7f);

        assertEquals(5, testMap.size());

        testMap.removeKey("two");
        assertEquals(4, testMap.size());
        assertNull(testMap.getValueFromKey("two"));

        testMap.removeKey("one");
        assertEquals(2, testMap.size());
        assertNull(testMap.getValueFromKey("one"));
    }

    @Test
    public void removeValue() throws Exception {
        testMap.put("one", 1f);
        testMap.put("two", 2f);
        testMap.put("three", 3f);
        testMap.put("four", 4f);
        testMap.put("seven", 1f);

        assertEquals(5, testMap.size());

        testMap.removeValue(2f);
        assertEquals(4, testMap.size());
        assertNull(testMap.getKeyFromValue(2f));

        testMap.removeValue(1f);
        assertEquals(2, testMap.size());
        assertNull(testMap.getKeyFromValue(1f));
    }

    @Test
    public void removePair() throws Exception{
        testMap.put("one", 1f);
        testMap.put("two", 2f);
        testMap.put("three", 3f);
        testMap.put("four", 4f);
        testMap.put("seven", 1f);

        assertEquals(5, testMap.size());

        testMap.removePair("three", 3f);
        assertEquals(4, testMap.size());
        assertNull(testMap.getKeyFromValue(3f));

        testMap.removePair("one", 1f);
        assertEquals(3, testMap.size());
        assertEquals("seven", testMap.getKeyFromValue(1f).get(0));

        testMap.removePair("seven", 45f);
        assertEquals(3, testMap.size());
        assertEquals("seven", testMap.getKeyFromValue(1f).get(0));
    }

    @Test
    public void put() throws Exception {
        assertEquals(17, map.size());

        assertEquals(0, testMap.size());
        testMap.put("Alpha", 0.3f);
        assertEquals(1, testMap.size());
        assertEquals("Alpha", testMap.getKeyFromValue(0.3f).get(0));
        assertEquals(0.3f, testMap.getValueFromKey("Alpha").get(0), 0);
    }

    @Test
    public void size() throws Exception {
        assertEquals(17, map.size());
        assertNotEquals(0, map.size());

        assertEquals(0, testMap.size());
        testMap.put("Hello", 8.0f);
        assertEquals(1, testMap.size());
    }

    @Test
    public void keySet() throws Exception {
        ArrayList<Integer> keys = map.keySet();
        assertEquals(17, keys.size());

        assertEquals(3, (int)keys.get(3));
    }

    @Test
    public void valueSet() throws Exception {
        ArrayList<Character> values = map.valueSet();
        assertEquals(17, values.size());

        assertEquals('f', (char)values.get(3));
    }

    @Test
    public void getKeyIndex() throws Exception {
        assertNull(map.getKeyIndex(-1));

        assertEquals(5, (int)map.getKeyIndex(1).get(0));
        assertEquals(11, (int)map.getKeyIndex(25).get(0));
        assertEquals(12, (int)map.getKeyIndex(25).get(1));
        assertEquals(15, (int)map.getKeyIndex(66).get(0));
        assertEquals(16, (int)map.getKeyIndex(66).get(1));

    }

    @Test
    public void getValueIndex() throws Exception {
        assertNull(map.getValueIndex('B'));

        assertEquals(5, (int)map.getValueIndex('h').get(0));
        assertEquals(13, (int)map.getValueIndex('y').get(0));
        assertEquals(14, (int)map.getValueIndex('y').get(1));
        assertEquals(15, (int)map.getValueIndex('z').get(0));
        assertEquals(16,(int) map.getValueIndex('z').get(1));
    }

}