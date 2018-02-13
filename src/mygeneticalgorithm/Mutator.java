package mygeneticalgorithm;

import java.util.Random;

public class Mutator {

    private Random random;

    public Mutator(Random random) {
        this.random = random;
    }

    public void mutate(Solution solutionToRead, Solution solutionToWrite) {
        solutionToWrite.containerIndexes = solutionToRead.containerIndexes.clone();
        int[] containerIndexes = solutionToWrite.containerIndexes;
        int length = containerIndexes.length;
        for (int i = 0; i < length / 10; i++) {
            int i1 = random.nextInt(length);
            int i2 = random.nextInt(length);
            int temp = containerIndexes[i1];
            containerIndexes[i1] = containerIndexes[i2];
            containerIndexes[i2] = temp;
        }
    }
}
