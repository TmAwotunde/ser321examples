package taskone;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

class StringList {
    
    List<String> strings = Collections.synchronizedList(new ArrayList<String>());

    public void add(String str) {
        int pos = strings.indexOf(str);
        if (pos < 0) {
            strings.add(str);
        }
    }

    public boolean contains(String str) {
        return strings.contains(str);
    }

    public int size() {
        return strings.size();
    }

    public String get(int index){
        if (index >= 0 && index < strings.size()){
            return strings.get(index);
        }
        return null;
    }

    public void set(int index, String data) {
        strings.set(index, data);
    }

    public void sort(){
        Collections.sort(strings);
    }

    public String toString() {
        return strings.toString();
    }
}