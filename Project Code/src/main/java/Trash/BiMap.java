package Trash;

import java.util.ArrayList;

public class BiMap<K,V> {

    private ArrayList<K> keys;
    private ArrayList<V> values;


    public BiMap() {
        keys = new ArrayList<>();
        values = new ArrayList<>();

        if (keys.size() != values.size()) error("Length Mismatch error at BiMap.BiMap(): BiMap is unbalanced.");
    }

    public K getKeyFromValue(V value){
        int i = values.indexOf(value);
        if (i != -1) return keys.get(values.indexOf(value));
        else return null;
    }

    //TODO: figure out how to fix bug where if there are duplicates in the data structure, how to get the right one.

    public V getValueFromKey(K key){
        int i = keys.indexOf(key);
        if (i != -1) return values.get(i);
        else return null;
    }

    public void removeKey(K key){
        int i = keys.indexOf(key);
        if (i == -1) return;    //key was not in the arraylist
        keys.remove(i);
        values.remove(i);

        if (keys.size() != values.size()) error("Length Mismatch error at BiMap.removeKey(): BiMap is unbalanced.");
    }

    public void removeValue(V value){
        int i = values.indexOf(value);
        if (i == -1) return;    //value was not in the arraylist
        keys.remove(i);
        values.remove(i);

        if (keys.size() != values.size()) error("Length Mismatch error at BiMap.removeValue(): BiMap is unbalanced.");
    }

    public void put(K key, V value){
        keys.add(key);
        values.add(value);

        if (keys.size() != values.size()) error("Length Mismatch error at BiMap.put(): BiMap is unbalanced.");
    }

    public int size(){
        if (keys.size() != values.size()) error("Length Mismatch error at BiMap.size(): BiMap is unbalanced.");

        return keys.size();
    }

    public ArrayList<K> keySet() {
        return keys;
    }
    public ArrayList<V> valueSet() {
        return values;
    }

    public int getKeyIndex(K key){ return keys.indexOf(key);}
    public int getValueIndex(V value) { return values.indexOf(value);}

    private void error(String message){
        System.err.println(message);
        System.exit(-1);
    }
}
