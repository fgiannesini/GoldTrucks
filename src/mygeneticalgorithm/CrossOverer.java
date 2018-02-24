package mygeneticalgorithm;

import java.util.Random;

public class CrossOverer {
    private Random random;

    public CrossOverer(Random random) {
        this.random = random;
    }

    public void applyCrossOver(Solution solutionToRead1, Solution solutionToRead2, Solution solutionToWrite) {
        int[] stwContainerIndexes = solutionToWrite.containerIndexes;
        int[] str1ContainerIndexes = solutionToRead1.containerIndexes;
        int[] str2ContainerIndexes = solutionToRead2.containerIndexes;

        int index = random.nextInt(stwContainerIndexes.length);
        System.arraycopy(str1ContainerIndexes, 0, stwContainerIndexes, 0, index);
        System.arraycopy(str2ContainerIndexes, index, stwContainerIndexes, index, stwContainerIndexes.length - index);
    }

}
