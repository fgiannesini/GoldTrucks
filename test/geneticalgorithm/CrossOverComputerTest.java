package geneticalgorithm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class CrossOverComputerTest {

  @Test
  public void permutationCrossOver() {
    Random random = new Random();
    int boxSize = 10;

    SolutionContainer[] solutionContainers = initSolutionContainerTab(boxSize, 4);
    CrossOverComputer crossOverComputer = new CrossOverComputer(solutionContainers, random, 2, 4, 2);

    for (int i = 0; i < 100; i++) {
      solutionContainers[0].positions = IntStream.range(0, boxSize).toArray();
      solutionContainers[1].positions = IntStream.range(0, boxSize).toArray();
      crossOverComputer.computeCrossOver();

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