package geneticalgorithm;

import java.util.Random;

public class CrossOverComputer {
    private SolutionContainer[] solutionContainers;
    private final Random random;
    private final int beginIndex;
    private final int endIndex;
    private int nPop;
    private double[] probabilities;
    private int[] x11;
    private int[] x12;
    private int[] x21;
    private int[] x22;
    private int[] r1;
    private int[] r2;

    public CrossOverComputer(SolutionContainer[] solutionContainers, Random random, int beginIndex, int endIndex, int nPop) {
        this.solutionContainers = solutionContainers;
        this.random = random;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        this.nPop = nPop;
        probabilities = new double[nPop];
        int nVar = solutionContainers[0].positions.length;
        x11 = new int[nVar];
        x12 = new int[nVar];
        x21 = new int[nVar];
        x22 = new int[nVar];
        r1 = new int[nVar];
        r2 = new int[nVar];
    }

    void computeCrossOver() {
        computeProbabilities();

        for (int k = beginIndex; k < endIndex ; k+=2) {
            int i1 = rouletteWheelSelection();
            SolutionContainer solution1 = solutionContainers[i1];

            int i2 = rouletteWheelSelection();
            SolutionContainer solution2 = solutionContainers[i2];

            permutationCrossOver(solution1.positions, solution2.positions, k);
        }
    }

    private void computeProbabilities() {
        double worstCost = solutionContainers[nPop - 1].cost;
        double sumP = 0;
        for (int s = 0; s < nPop; s++) {
            probabilities[s] = Math.exp(-5 * solutionContainers[s].cost / worstCost);
            sumP += probabilities[s];
        }
        probabilities[0] /= sumP;
        for (int s = 1; s < nPop; s++) {
            probabilities[s] = probabilities[s - 1] + probabilities[s] / sumP;
        }
    }

    private int rouletteWheelSelection() {
        double r = random.nextDouble();
        for (int s = 0; s < probabilities.length; s++) {
            if (probabilities[s] > r) {
                return s;
            }
        }
        return 0;
    }

    void permutationCrossOver(int[] positions1, int[] positions2, int solutionIndex) {

        int nVar = positions1.length;
        int c = random.nextInt(nVar - 1);

        System.arraycopy(positions1, 0, x11, 0, c);
        System.arraycopy(positions1, c, x12, 0, nVar - c);

        System.arraycopy(positions2, 0, x21, 0, c);
        System.arraycopy(positions2, c, x22, 0, nVar - c);

        int r1Index = 0;
        int r2Index = 0;

        for (int i = 0; i < c; i++) {
            for (int j = 0; j < nVar - c; j++) {
                if (x11[i] == x22[j]) {
                    r1[r1Index++] = x11[i];
                    break;
                }
            }
        }
        for (int i = 0; i < c; i++) {
            for (int j = 0; j < nVar - c; j++) {
                if (x12[j] == x21[i]) {
                    r2[r2Index++] = x12[j];
                    break;
                }
            }
        }

        int r1Iterator = 0;
        int r2Iterator = 0;

        for (int i = 0; i < c; i++) {
            for (int j = 0; j < r1Index; j++) {
                if (r1[j] == x11[i]) {
                    x11[i] = r2[r2Iterator++];
                    break;
                }
            }
        }

        for (int i = 0; i < c; i++) {
            for (int j = 0; j < r2Index; j++) {
                if (r2[j] == x21[i]) {
                    x21[i] = r1[r1Iterator++];
                    break;
                }
            }
        }

        SolutionContainer solutionContainer1 = solutionContainers[solutionIndex];
        System.arraycopy(x11, 0, solutionContainer1.positions, 0, c);
        System.arraycopy(x22, 0, solutionContainer1.positions, c, nVar - c);

        SolutionContainer solutionContainer2 = solutionContainers[solutionIndex + 1];
        System.arraycopy(x21, 0, solutionContainer2.positions, 0, c);
        System.arraycopy(x12, 0, solutionContainer2.positions, c, nVar - c);
    }
}
