package particleswarmoptimization;

import java.util.Arrays;
import java.util.Random;

public class Mutator {
  private final Random random;
    private final CostComputer costComputer;

    public Mutator(Random random, CostComputer costComputer) {
    this.random = random;
        this.costComputer = costComputer;
  }

    private double[] mutatePositions(double[] positions) {
    int n = positions.length;
    double[] newPositions = Arrays.copyOf(positions, n);
    int i = random.nextInt(n);
    int j = random.nextInt(n);
    while(i==j) {
      j = random.nextInt(n);
    }
    double temp =newPositions[i];
    newPositions[i] = newPositions[j];
    newPositions[j] = temp;
    return newPositions;
  }

    public void performMutation(int mutationCount, SolutionContainer solutionContainer) throws CloneNotSupportedException {
        SolutionContainer bestSolutionContainer = solutionContainer;
        for (int k = 0; k < mutationCount; k++) {
            SolutionContainer mutatedSolutionContainer = new SolutionContainer(solutionContainer);
            mutatedSolutionContainer.positions = mutatePositions(solutionContainer.positions);
            costComputer.computeBinPackingCost(mutatedSolutionContainer);
            if (mutatedSolutionContainer.cost < bestSolutionContainer.cost) {
                bestSolutionContainer = mutatedSolutionContainer;
            }
        }

        if (solutionContainer.equals(bestSolutionContainer)) {
            return;
        }
        solutionContainer.cost = bestSolutionContainer.cost;
        solutionContainer.positions = bestSolutionContainer.positions;
        solutionContainer.solution = bestSolutionContainer.solution;
    }
}
