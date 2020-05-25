package spark;

import java.io.Serializable;
import java.util.List;

public class Matrix implements Serializable {

    private List<List<Integer>> arrayLists;
    private Integer integer;

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    public List<List<Integer>> getArrayLists() {
        return arrayLists;
    }



    public void setArrayLists(List<List<Integer>> arrayLists) {
        this.arrayLists = arrayLists;
    }
}
