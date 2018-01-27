package geneticalgorithm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
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
        SolutionContainer solutionContainer = new SolutionContainer();
        solutionContainer.positions = Arrays.asList(2, 4, 5, 1, 0, 7, 8, 3, 6);
        new GAImplementation().computeBinPackingCost(solutionContainer, model);
        Assert.assertArrayEquals(IntStream.range(0, 5).toArray(), solutionContainer.solution.b.stream().flatMap(List::stream).mapToInt(Integer::intValue).sorted().toArray());
    }

    @Test
    public void computeBinPackingCostSize30() {
        Model model = new Model();
        model.v = new int[]{6, 3, 4, 6, 8, 7, 4, 7, 7, 5, 5, 6, 7, 7, 6, 4, 8, 7, 8, 8, 2, 3, 4, 5, 6, 5, 5, 7, 7, 12};
        model.n = model.v.length;
        model.vMax = 30;
        SolutionContainer solutionContainer = new SolutionContainer();
        solutionContainer.positions = Arrays.asList(37, 48, 56, 16, 33, 0, 7, 8, 58, 42, 30, 23, 4, 14, 44, 17, 45, 57, 20, 36, 50, 43, 15, 41, 40, 25, 53, 2, 47, 27, 11, 49, 52, 19, 38, 10, 39, 32, 51, 28, 26, 54, 46, 31, 3, 22, 13, 34, 5, 29, 55, 12, 6, 9, 21, 18, 1, 35, 24);
        new GAImplementation().computeBinPackingCost(solutionContainer, model);
        Assert.assertArrayEquals(IntStream.range(0, 30).toArray(), solutionContainer.solution.b.stream().flatMap(List::stream).mapToInt(Integer::intValue).sorted().toArray());
    }
}