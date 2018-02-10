package particleswarmoptimization;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

public class CostComputerTest {

    @Test
    public void computeBinPackingCostSize5() {
        Model model = new Model();
        model.v = new int[]{1, 3, 5, 2, 4};
        model.n = model.v.length;
        model.vMax = 5;
        SolutionContainer solutionContainer = new SolutionContainer(5, 9);
        solutionContainer.positions = IntStream.range(0, 9).mapToDouble(i -> i / 10d).toArray();
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
        solutionContainer.positions = IntStream.range(0, 59).mapToDouble(i -> i / 60d).toArray();
        new CostComputer(model).computeBinPackingCost(solutionContainer);
        int solutionCount = solutionContainer.solution.nBin;
        int positionLength = solutionContainer.solution.bPositions[solutionCount];
        Assert.assertArrayEquals(IntStream.range(0, 30).toArray(), Arrays.stream(solutionContainer.solution.b).limit(positionLength).sorted().toArray());
    }

}