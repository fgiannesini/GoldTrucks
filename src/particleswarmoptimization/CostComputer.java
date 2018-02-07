package particleswarmoptimization;

import java.util.Arrays;

public class CostComputer {

    private Model model;
    private int[] sep;
    private int[] from;
    private int[] to;

    public CostComputer(Model model) {
        this.model = model;
        sep = new int[this.model.n - 1];
        from = new int[model.n];
        to = new int[model.n];
    }

    public void computeBinPackingCost(SolutionContainer solutionContainer) {
        Arrays.sort(solutionContainer.positions);

        int sepIndex = 0;
        for (int i = 0; i < solutionContainer.positions.length; i++) {
            if (solutionContainer.positions[i] >= 0.5) {
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
                solution.b[currentPosition++] = solutionContainer.positions[j];
            }
            solution.bPositions[++solution.bIndex] = currentPosition;
        }

        for (int i = 0; i < solution.bIndex; i++) {
            solution.viol[i] = 0;
            for (int j = solution.bPositions[i]; j < solution.bPositions[i + 1]; j++) {
                double position = solution.b[j];
                solution.viol[i] += model.v[(int)position];
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
