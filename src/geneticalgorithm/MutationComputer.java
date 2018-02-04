package geneticalgorithm;

import java.util.Random;

public class MutationComputer {

    private SolutionContainer[] solutionContainers;
    private final Random random;
    private final int beginIndex;
    private final int endIndex;
    private int nPop;

    public MutationComputer(SolutionContainer[] solutionContainers, Random random, int beginIndex, int endIndex, int nPop) {
        this.solutionContainers = solutionContainers;
        this.random = random;
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        this.nPop = nPop;
    }

    public void mutate() {

        for (int k = beginIndex; k < endIndex; k++) {
            //Select Parent Index
            int parentIndex = random.nextInt(nPop);

            //Select Parent
            SolutionContainer parent = solutionContainers[parentIndex];

            //Apply MutationComputer
            permutationMutate(parent.positions, solutionContainers[k].positions);
        }
    }

    private void permutationMutate(int[] positions, int[] positionsToMutate) {

        int mode = random.nextInt(3);
        switch (mode) {
            case 0:
                //Swap
                doSwap(positions,positionsToMutate);
                break;
            case 1:
                // Reversion
                doReversion(positions,positionsToMutate);
                break;
            case 2:
                // Insertion
                doInsertion(positions,positionsToMutate);
                break;
            default:
        }
    }

    private void doInsertion(int[] positions, int[] mutatedPositions) {

        int j1 = random.nextInt(positions.length);
        int j2 = random.nextInt(positions.length);
        while (j2 == j1) {
            j2 = random.nextInt(positions.length);
        }
        int i1 = Math.min(j1, j2);
        int i2 = Math.max(j1, j2);

        System.arraycopy(positions, 0, mutatedPositions, 0, i1);
        System.arraycopy(positions, i1 + 1, mutatedPositions, i1, i2 - i1 - 1);
        mutatedPositions[i2 - 1] = positions[i1];
        System.arraycopy(positions, i2, mutatedPositions, i2, positions.length - i2);
    }

    private void doReversion(int[] positions, int[] mutatedPositions) {
        int j1 = random.nextInt(positions.length);
        int j2 = random.nextInt(positions.length);
        int i1 = Math.min(j1, j2);
        int i2 = Math.max(j1, j2);

        System.arraycopy(positions, 0, mutatedPositions, 0, i1);
        for (int i = 0; i < i2 - i1; i++) {
            mutatedPositions[i1 + i] = positions[i2 - i - 1];
        }
        System.arraycopy(positions, i2, mutatedPositions, i2, positions.length - i2);
    }

    private void doSwap(int[] positions, int[] mutatedPositions) {
        System.arraycopy(positions, 0, mutatedPositions, 0, positions.length);
        int i = random.nextInt(positions.length);
        int j = random.nextInt(positions.length);
        mutatedPositions[j] = positions[i];
        mutatedPositions[i] = positions[j];
    }
}
