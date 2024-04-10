package taskone;

import java.util.List;
import java.util.ArrayList;

class StringList {
    
    List<String> strings = new ArrayList<String>();

    synchronized public void add(String str) {
        int pos = strings.indexOf(str);
        if (pos < 0) {
            strings.add(str);
        }
    }
    synchronized public void clear(){
        for(int i = 0; i < size(); i++){
            strings.clear();
        }
    }
    synchronized public int indexOf(String str){
        if(contains(str)){
            return strings.indexOf(str);
        }
        return -1;
    }
    synchronized public String delete(int idx){
        if(idx < 0 || idx > size()){
            return "";
        }else{
            String item = strings.get(idx);
            strings.remove(idx);
            return item;//true
        }
    }
    synchronized public boolean prepend(String str,int idx){
        if(idx > size() || idx < 0){
            System.out.println("Out of bound");
            return false;
        }else{
            String appended = strings.get(idx);
//        String newStr = appended.concat(str);
            String newStr = appended.concat(" ").concat(str);
            strings.set(idx,newStr);
            return true;
        }


    }

    public boolean contains(String str) {
        return strings.contains(str);
    }

    public int size() {
        return strings.size();
    }

    public String toString() {
        return strings.toString();
    }
}