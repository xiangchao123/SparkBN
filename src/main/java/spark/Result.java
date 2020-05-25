package spark;


import java.io.Serializable;



public class Result implements Serializable {
    private int son;
    private String parents;

    public String getParents() {
        return parents;
    }

    public void setParents(String parents) {
        this.parents = parents;
    }

    public int getSon() {
        return son;
    }

    public void setSon(int son) {
        this.son = son;
    }


}
