package particleswarmoptimization;

public class Solution implements Cloneable {

    public int[] bPositions;
    public int bIndex;
    int nBin;
    int[] b;
    double[] viol;
    double meanViol;

    public Solution(int containerCount, int elementCount) {
        bPositions = new int[containerCount * 2];
        b = new int[elementCount];
        viol = new double[containerCount];
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Solution solution = new Solution(viol.length, b.length);
        solution.nBin = this.nBin;
        System.arraycopy(this.b, 0, solution.b, 0, b.length);
        System.arraycopy(this.bPositions, 0, solution.bPositions, 0, bPositions.length);
        solution.bIndex = this.bIndex;
        System.arraycopy(this.viol, 0, solution.viol, 0, viol.length);
        solution.meanViol = this.meanViol;
        return solution;
    }
}
