package geneticalgorithm;

import java.util.List;

public class Solution {

  int nBin;
  int[] b;
  int[] bPositions;
  int bIndex;
  double[] viol;
  double meanViol;

  public Solution(int containerCount, int elementCount) {
    bPositions = new int[containerCount * 2];
    b = new int[elementCount];
    viol = new double[containerCount];
  }
}
