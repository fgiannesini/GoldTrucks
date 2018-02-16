package mygeneticalgorithm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.stream.IntStream;

public class MutatorTest {
  @Test
  public void mutate() throws Exception {
    Random random = new Random();
    int containerSize = 100;
    Solution solutionToRead = new Solution(containerSize);
    IntStream.range(0, containerSize).forEach(i -> solutionToRead.containerIndexes[i] = i);
    Solution solutionToWrite = new Solution(containerSize);

    Mutator mutator = new Mutator(random);
    boolean hasMutated = false;
    for (int j = 0; j < 100; j++) {
      mutator.mutate(solutionToRead, solutionToWrite);
      hasMutated = hasMutated || IntStream.range(0, containerSize).anyMatch(
        i -> solutionToRead.containerIndexes[i] != solutionToWrite.containerIndexes[i]);

      Assert.assertArrayEquals(
        IntStream.of(solutionToRead.containerIndexes).sorted().toArray(),
        IntStream.of(solutionToWrite.containerIndexes).sorted().toArray()
      );

    }
    Assert.assertTrue(hasMutated);
  }

}