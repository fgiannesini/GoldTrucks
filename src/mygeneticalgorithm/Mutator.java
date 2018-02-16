package mygeneticalgorithm;

import java.util.Random;

public class Mutator {

  private Random random;

  public Mutator(Random random) {
    this.random = random;
  }

  public void mutate(Solution solutionToRead, Solution solutionToWrite) {
    solutionToWrite.containerIndexes = solutionToRead.containerIndexes.clone();
    int[] containerIndexes = solutionToWrite.containerIndexes;

    int length = containerIndexes.length;
    int[] randomIndex = random.ints(length * 2, 2, length).toArray();
    for (int i = 0; i < length; i++) {
      int i1 = randomIndex[2 * i];
      int i2 = randomIndex[2 * i + 1];

      int temp = containerIndexes[i1];
      containerIndexes[i1] = containerIndexes[i2];
      containerIndexes[i2] = temp;
    }
  }
}
