package mygeneticalgorithm;

public class CrossOverer {

  public void applyCrossOver(Solution solutionToRead1, Solution solutionToRead2, Solution solutionToWrite) {
    int[] stwContainerIndexes = solutionToWrite.containerIndexes;
    int[] str1ContainerIndexes = solutionToRead1.containerIndexes;
    int[] str2ContainerIndexes = solutionToRead2.containerIndexes;
    for (int i = 0; i < stwContainerIndexes.length; i++) {
      if (stwContainerIndexes[i] % 2 == 0) {
        stwContainerIndexes[i] = str1ContainerIndexes[i];
      }
      else {
        stwContainerIndexes[i] = str2ContainerIndexes[i];
      }
    }
  }
}
