package geneticalgorithm;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    new GAImplementation().computeBinPackingCost(solutionContainer, model);
    Arrays.sort(solutionContainer.solution.b);
    Assert.assertArrayEquals(IntStream.range(0, 5).toArray(), solutionContainer.solution.b);
  }

  @Test
  public void computeBinPackingCostSize30() {
    Model model = new Model();
    model.v = new int[]{6, 3, 4, 6, 8, 7, 4, 7, 7, 5, 5, 6, 7, 7, 6, 4, 8, 7, 8, 8, 2, 3, 4, 5, 6, 5, 5, 7, 7, 12};
    model.n = model.v.length;
    model.vMax = 30;
    SolutionContainer solutionContainer = new SolutionContainer(30, 59);
    solutionContainer.positions = new int[]{37, 48, 56, 16, 33, 0, 7, 8, 58, 42, 30, 23, 4, 14, 44, 17, 45, 57, 20, 36, 50, 43, 15, 41, 40, 25, 53, 2, 47, 27, 11, 49, 52, 19, 38, 10, 39, 32, 51, 28, 26, 54, 46, 31, 3, 22, 13, 34, 5, 29, 55, 12, 6, 9, 21, 18, 1, 35, 24};
    new GAImplementation().computeBinPackingCost(solutionContainer, model);
    Arrays.sort(solutionContainer.solution.b);
    Assert.assertArrayEquals(IntStream.range(0, 30).toArray(), solutionContainer.solution.b);
  }

  @Test
  public void permutationCrossOver() {
    GAImplementation gaImplementation = new GAImplementation();
    Random random = new Random();
    int boxSize = 10;

    SolutionContainer container1 = new SolutionContainer(10,19);
    SolutionContainer container2 = new SolutionContainer(10,19);

    for (int i = 0; i < 100; i++) {
      container1.positions = IntStream.range(0, boxSize).toArray();
      container2.positions = IntStream.range(0, boxSize).toArray();

      List<SolutionContainer> crossOverSolution = new ArrayList<>(2);
      gaImplementation.permuationCrossOver(crossOverSolution, container1.positions, container2.positions, random);

      Assert.assertEquals(2, crossOverSolution.size());

      crossOverSolution.forEach(s -> Assert.assertEquals(boxSize, s.positions.length));
      Assert.assertArrayEquals(
        IntStream.concat(Arrays.stream(container1.positions), Arrays.stream(container2.positions)).sorted().toArray(),
        IntStream.concat(Arrays.stream(crossOverSolution.get(0).positions), Arrays.stream(crossOverSolution.get(1).positions)).sorted().toArray()
      );
    }
  }

  @Test
  public void permutationMutate() {
    GAImplementation gaImplementation = new GAImplementation();
    Random random = new Random();
    SolutionContainer container1 = new SolutionContainer(10,19);
    int boxSize = 10;

    for (int i = 0; i < 100; i++) {
      container1.positions = IntStream.range(0, boxSize).toArray();

      List<SolutionContainer> mutateSolution = new ArrayList<>(1);
      gaImplementation.permutationMutate(mutateSolution, container1.positions, random);
      Assert.assertEquals(1, mutateSolution.size());
      mutateSolution.forEach(s -> Assert.assertEquals(boxSize, s.positions.length));
      Assert.assertArrayEquals(
        Arrays.stream(container1.positions).sorted().toArray(),
        Arrays.stream(mutateSolution.get(0).positions).sorted().toArray()
      );
    }
  }
}