package spark;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xiangchao on 2020/5/24.
 */
public class MatrixPar implements Serializable {
    private Integer integer;
    private Matrix matrix;

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }
}
