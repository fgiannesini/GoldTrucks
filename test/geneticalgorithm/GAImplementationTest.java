package geneticalgorithm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class GAImplementationTest {

  @Test
  public void launch() {
    Model model = new Model();
    model.v = new int[]{6, 3, 4, 6, 8, 7, 4, 7, 7, 5, 5, 6, 7, 7, 6, 4, 8, 7, 8, 8, 2, 3, 4, 5, 6, 5, 5, 7, 7, 12};
    model.n = model.v.length;
    model.vMax = 30;
    new GAImplementation().launch(model);
  }

  @Test
  public void computeBinPackingCostSize5() {
    Model model = new Model();
    model.v = new int[]{1, 3, 5, 2, 4};
    model.n = model.v.length;
    model.vMax = 5;
    SolutionContainer solutionContainer = new SolutionContainer(5, 9);
    solutionContainer.positions = new int[]{2, 4, 5, 1, 0, 7, 8, 3, 6};
    new CostComputer(model).computeBinPackingCost(solutionContainer);
    int solutionCount = solutionContainer.solution.nBin;
    int positionLength = solutionContainer.solution.bPositions[solutionCount];
    Assert.assertArrayEquals(IntStream.range(0, 5).toArray(), Arrays.stream(solutionContainer.solution.b).limit(positionLength).sorted().toArray());
  }

  @Test
  public void computeBinPackingCostSize30() {
    Model model = new Model();
    model.v = new int[]{6, 3, 4, 6, 8, 7, 4, 7, 7, 5, 5, 6, 7, 7, 6, 4, 8, 7, 8, 8, 2, 3, 4, 5, 6, 5, 5, 7, 7, 12};
    model.n = model.v.length;
    model.vMax = 30;
    SolutionContainer solutionContainer = new SolutionContainer(30, 59);
    solutionContainer.positions = new int[]{37, 48, 56, 16, 33, 0, 7, 8, 58, 42, 30, 23, 4, 14, 44, 17, 45, 57, 20, 36, 50, 43, 15, 41, 40, 25, 53, 2, 47, 27, 11, 49, 52, 19, 38, 10, 39, 32, 51, 28, 26, 54, 46, 31, 3, 22, 13, 34, 5, 29, 55, 12, 6, 9, 21, 18, 1, 35, 24};
    new CostComputer(model).computeBinPackingCost(solutionContainer);
    int solutionCount = solutionContainer.solution.nBin;
    int positionLength = solutionContainer.solution.bPositions[solutionCount];
    Assert.assertArrayEquals(IntStream.range(0, 30).toArray(), Arrays.stream(solutionContainer.solution.b).limit(positionLength).sorted().toArray());
  }

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

      Arrays.stream(solutionContainers).peek(sc -> Arrays.sort(sc.positions)).forEach(sc -> Assert.assertArrayEquals(solutionContainers[0].positions, sc.positions));
    }
  }

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

      Arrays.stream(solutionContainers).peek(sc -> Arrays.sort(sc.positions)).forEach(sc -> Assert.assertArrayEquals(solutionContainers[0].positions, sc.positions));
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