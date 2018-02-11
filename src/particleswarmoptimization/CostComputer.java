package particleswarmoptimization;

import java.util.Arrays;
import java.util.Comparator;

public class CostComputer {

    private Model model;
    private int[] sep;
    private int[] from;
    private int[] to;
    private Position[] positions;

    public CostComputer(Model model) {
        this.model = model;
        sep = new int[this.model.n - 1];
        from = new int[model.n];
        to = new int[model.n];
        positions = new Position[model.n * 2 - 1];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = new Position();
        }
    }

    public void computeBinPackingCost(SolutionContainer solutionContainer) {
        for (int i = 0; i < solutionContainer.positions.length; i++) {
            positions[i].index = i;
            positions[i].value = solutionContainer.positions[i];
        }
        Arrays.sort(positions, Comparator.comparingDouble(p -> p.value));

        int sepIndex = 0;
        for (int i = 0; i < solutionContainer.positions.length; i++) {
            if (positions[i].index >= model.n) {
                sep[sepIndex++] = i;
            }
        }

        from[0] = 0;
        for (int i = 0; i < sepIndex; i++) {
            from[i + 1] = sep[i] + 1;
        }

        for (int i = 0; i < sepIndex; i++) {
            to[i] = sep[i] - 1;
        }
        to[model.n - 1] = model.n * 2 - 1;

        Solution solution = solutionContainer.solution;
        solution.bIndex = 0;
        solution.bPositions[solution.bIndex] = 0;
        for (int i = 0; i < model.n; i++) {
            int fromValue = from[i];
            int toValue = to[i];
            if (fromValue > toValue || fromValue >= solutionContainer.positions.length) {
                continue;
            }
            int currentPosition = solution.bPositions[solution.bIndex];
            for (int j = fromValue; j <= toValue; j++) {
                if (j >= solutionContainer.positions.length) {
                    continue;
                }
                solution.b[currentPosition++] = positions[j].index;
            }
            solution.bPositions[++solution.bIndex] = currentPosition;
        }

        for (int i = 0; i < solution.bIndex; i++) {
            solution.viol[i] = 0.0;
            for (int j = solution.bPositions[i]; j < solution.bPositions[i + 1]; j++) {
                int position = solution.b[j];
                solution.viol[i] += model.v[position];
            }
            solution.viol[i] = Math.max((solution.viol[i] / (float) model.vMax) - 1, 0d);
        }

        double violMean = 0;
        for (int i = 0; i < solution.bIndex; i++) {
            violMean += solution.viol[i];
        }
        violMean /= solution.bIndex;

        solution.meanViol = violMean;

        solution.nBin = solution.bIndex;

        solutionContainer.cost = solution.nBin + 2 * model.n * solution.meanViol;
    }
}
