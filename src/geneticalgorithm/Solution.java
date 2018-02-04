package geneticalgorithm;

public class Solution {

  int nBin;
  int[] b;
  public int[] bPositions;
  public int bIndex;
  double[] viol;
  double meanViol;

  public Solution(int containerCount, int elementCount) {
    bPositions = new int[containerCount * 2];
    b = new int[elementCount];
    viol = new double[containerCount];
  }
}
