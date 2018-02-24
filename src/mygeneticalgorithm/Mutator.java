package mygeneticalgorithm;

import java.util.Random;

public class Mutator {

    private Random random;

    public Mutator(Random random) {
        this.random = random;
    }

    public void mutate(Solution solutionToRead, Solution solutionToWrite) {
        System.arraycopy(solutionToRead.containerIndexes, 0, solutionToWrite.containerIndexes, 0, solutionToRead.containerIndexes.length);

        int[] containerIndexes = solutionToWrite.containerIndexes;

        int length = containerIndexes.length;
        int i1 = random.nextInt(length);
        int i2 = random.nextInt(length);
        while (i1 == i2) {
            i2 = random.nextInt(length);
        }

        int temp = containerIndexes[i1];
        containerIndexes[i1] = containerIndexes[i2];
        containerIndexes[i2] = temp;
    }
}
