package geneticalgorithm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class MutationComputerTest {

  @Test
  public void permutationMutate() {

    Random random = new Random();
    int boxSize = 10;
    int solutionContainerSize = 2;
    SolutionContainer[] solutionContainers = initSolutionContainerTab(boxSize, solutionContainerSize);

    MutationComputer mutationComputer = new MutationComputer(solutionContainers, random, 1, 2, 1);
    for (int i = 0; i < 100; i++) {
      solutionContainers[0].positions = IntStream.range(0, boxSize).toArray();
      mutationComputer.mutate();

      Arrays.stream(solutionContainers).peek(sc -> Arrays.sort(sc.positions)).forEach(sc -> Assert
        .assertArrayEquals(solutionContainers[0].positions, sc.positions));
    }
  }

  private SolutionContainer[] initSolutionContainerTab(int boxSize, int solutionContainerSize) {
    SolutionContainer[] solutionContainers = new SolutionContainer[solutionContainerSize];
    for (int i = 0; i < solutionContainers.length; i++) {
      SolutionContainer solutionContainer = new SolutionContainer(boxSize, boxSize * 2 - 1);
      solutionContainer.positions = new int[boxSize];
      solutionContainer.cost = 1;
      solutionContainers[i] = solutionContainer;
    }
    return solutionContainers;
  }

}