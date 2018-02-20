package mygeneticalgorithm;

import java.util.Random;

public class Mutator {

  private Random random;

  public Mutator(Random random) {
    this.random = random;
  }

  public void mutate(Solution solutionToRead, Solution solutionToWrite) {
    System.arraycopy(solutionToRead.containerIndexes, 0, solutionToWrite.containerIndexes, 0, solutionToRead.containerIndexes.length);

    int[] containerIndexes = solutionToWrite.containerIndexes;

    int length = containerIndexes.length;
    int permutationCount = random.nextInt(containerIndexes.length);
    int[] randomIndex = random.ints(permutationCount * 2, 2, length).toArray();
    for (int i = 0; i < permutationCount; i++) {
      int i1 = randomIndex[2 * i];
      int i2 = randomIndex[2 * i + 1];

      int temp = containerIndexes[i1];
      containerIndexes[i1] = containerIndexes[i2];
      containerIndexes[i2] = temp;
    }
  }
}
