package Utilities;

import java.util.ArrayList;

public class BiMap<K,V> {

    private ArrayList<K> keys;
    private ArrayList<V> values;


    public BiMap() {
        keys = new ArrayList<>();
        values = new ArrayList<>();

        if (keys.size() != values.size()) error("Length Mismatch error at BiMap.BiMap(): BiMap is unbalanced.");
    }

    public ArrayList<K> getKeyFromValue(V value){
        int matches = 0;
        int[] matchIndex = new int[values.size()];
        for (int i = 0; i < values.size(); i++){
            if (values.get(i).equals(value)){
                matches++;
                matchIndex[i] = i+1;
            }
        }
        if (matches == 0) return null;
        ArrayList<K> array = new ArrayList<>();
        for (int a = 0,i = 0; i < matchIndex.length; i++){
            if (matchIndex[i] != 0) array.add(a++, keys.get(matchIndex[i]-1) );
        }
        return array;
    }

    public K getFirstKeyFromValue(V value){
        ArrayList<K> occurences = getKeyFromValue(value);
        if (occurences == null) return null;
        else return occurences.get(0);
    }


    public ArrayList<V> getValueFromKey(K key){
        int matches = 0;
        int[] matchIndex = new int[keys.size()];
        for (int i = 0; i < keys.size(); i++){
            if (keys.get(i).equals(key)){
                matches++;
                matchIndex[i] = i+1;
            }
        }
        if (matches == 0) return null;
        ArrayList<V> array = new ArrayList<>();
        for (int a = 0,i = 0; i < matchIndex.length; i++){
            if (matchIndex[i] != 0) array.add(a++, values.get(matchIndex[i]-1) );
        }
        return array;
    }

    public V getFirstValueFromKey(K key){
        ArrayList<V> occurences = getValueFromKey(key);
        if (occurences == null) return null;
        else return occurences.get(0);
    }

    public void removeKey(K key){
        for (int i = 0; i < keys.size(); i++){
            if (keys.get(i).equals(key)){
                keys.remove(i);
                values.remove(i);
            }
        }

        if (keys.size() != values.size()) error("Length Mismatch error at BiMap.removeKey(): BiMap is unbalanced.");
    }

    public void removeValue(V value){
        for (int i = 0; i < values.size(); i++){
            if (values.get(i).equals(value)){
                keys.remove(i);
                values.remove(i);
            }
        }

        if (keys.size() != values.size()) error("Length Mismatch error at BiMap.removeValue(): BiMap is unbalanced.");
    }

    public void removePair(K key, V value){
        for (int i = 0; i < values.size(); i++){
            if (values.get(i).equals(value) && keys.get(i).equals(key)){
                keys.remove(i);
                values.remove(i);
            }
        }

        if (keys.size() != values.size()) error("Length Mismatch error at BiMap.removePair(): BiMap is unbalanced.");
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

    public ArrayList<Integer> getKeyIndex(K key){
        int matches = 0;
        int[] matchIndex = new int[keys.size()];
        for (int i = 0; i < keys.size(); i++){
            if (keys.get(i).equals(key)){
                matches++;
                matchIndex[i] = i+1;
            }
        }
        if (matches == 0) return null;
        ArrayList<Integer> array = new ArrayList<>();
        for (int a = 0,i = 0; i < matchIndex.length; i++){
            if (matchIndex[i] != 0) array.add(a++, matchIndex[i]-1);
        }
        return array;
    }
    public int getFirstKeyIndex(K key){
        //return keys.indexOf(key);
        ArrayList<Integer> indices = getKeyIndex(key);
        if (indices == null) return -1;
        else return indices.get(0);
    }

    public ArrayList<Integer> getValueIndex(V value) {
        int matches = 0;
        int[] matchIndex = new int[values.size()];
        for (int i = 0; i < values.size(); i++){
            if (values.get(i).equals(value)){
                matches++;
                matchIndex[i] = i+1;
            }
        }
        if (matches == 0) return null;
        ArrayList<Integer> array = new ArrayList<>();
        for (int a = 0,i = 0; i < matchIndex.length; i++){
            if (matchIndex[i] != 0) array.add(a++, matchIndex[i]-1);
        }
        return array;
    }

    public int getFirstValueIndex(V value){
        //return keys.indexOf(value);
        ArrayList<Integer> indices = getValueIndex(value);
        if (indices == null) return -1;
        else return indices.get(0);
    }

    private void error(String message){
        System.err.println(message);
        System.exit(-1);
    }
}