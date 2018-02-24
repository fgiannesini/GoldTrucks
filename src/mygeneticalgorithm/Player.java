package mygeneticalgorithm;

import java.util.*;
import java.util.stream.Collectors;

public class Player {

    private static final long COMPUTATION_TIME = 50;
    static int TRUCK_COUNT = 100;
    static int TRUCK_VOLUME = 100;

    public static void main(String args[]) {
        new Player().launch(new Scanner(System.in));
    }

    void launch(Scanner in) {

        long startTime = System.nanoTime();

        Boxes boxes = createBoxes(in);

        int populationCount = 40;
        int mutationCount = 30;
        int crossOverCount = 15;

        Solution[] solutions = new Solution[populationCount + mutationCount + crossOverCount];

        //Init
        Random random = new Random();
        CostComputer costComputer = new CostComputer(boxes, TRUCK_COUNT, TRUCK_VOLUME);

        int[] randomContainers = random.ints(boxes.boxCount * populationCount, 0, TRUCK_COUNT).toArray();
        for (int i = 0; i < populationCount; i++) {
            Solution solution = new Solution(boxes.boxCount);
            System.arraycopy(randomContainers, i * boxes.boxCount, solution.containerIndexes, 0, solution.containerIndexes.length);
            costComputer.computeCost(solution);
            solutions[i] = solution;
        }

        for (int i = populationCount; i < populationCount + mutationCount + crossOverCount; i++) {
            solutions[i] = new Solution(boxes.boxCount);
        }

        Mutator mutator = new Mutator(random);
        CrossOverer crossOverer = new CrossOverer(random);

        int ite = 0;
        while (true) {

            for (int i = 0; i < crossOverCount; i++) {
                int crossOverIndex1 = random.nextInt(populationCount);
                int crossOverIndex2 = random.nextInt(populationCount);
                crossOverer.applyCrossOver(solutions[crossOverIndex1], solutions[crossOverIndex2], solutions[populationCount + i]);
            }

            for (int i = 0; i < mutationCount; i++) {
                int mutatedIndex = random.nextInt(populationCount);
                mutator.mutate(solutions[mutatedIndex], solutions[populationCount + crossOverCount + i]);
            }

            //Compute Cost
            for (int i = populationCount; i < populationCount + crossOverCount + mutationCount; i++) {
                costComputer.computeCost(solutions[i]);
            }

            Arrays.sort(solutions, Comparator.comparingDouble(s -> s.cost));

            long computationTime = System.nanoTime() - startTime;
            if (ite++ % 1000 == 0) {
                System.err.println("Iteration " + ite++ + ": ComputationTime = " + computationTime / 1_000_000d + " ms Best Cost = " + solutions[0].cost);
            }

            if (computationTime > (COMPUTATION_TIME - 1) * 1_000_000_000) {
                break;
            }
        }

        double[] truckWeights = new double[TRUCK_COUNT];
        double[] truckVolumes = new double[TRUCK_COUNT];
        int[] bestContainerIndexes = solutions[0].containerIndexes;
        for (int i = 0; i < bestContainerIndexes.length; i++) {
            int boxIndex = bestContainerIndexes[i];
            truckVolumes[boxIndex] += boxes.volumes[i];
            truckWeights[boxIndex] += boxes.weights[i];
        }

        System.err.println("truck Volumes > " + TRUCK_VOLUME + " " + Arrays.stream(truckVolumes).filter(v -> v >= TRUCK_VOLUME).mapToObj(String::valueOf)
                .collect(Collectors.joining(" ")));
        DoubleSummaryStatistics summaryStatistics = Arrays.stream(truckWeights).summaryStatistics();
        System.err.println("Max Weight : " + summaryStatistics.getMax());
        System.err.println("Min Weight : " + summaryStatistics.getMin());

        StringBuilder resultBuilder = new StringBuilder();
        for (int boxIndex : bestContainerIndexes) {
            resultBuilder.append(boxIndex).append(" ");
        }
        System.out.println(resultBuilder.toString().trim());
    }

    private Boxes createBoxes(Scanner in) {
        int boxCount = in.nextInt();
        Boxes boxes = new Boxes(boxCount);
        for (int i = 0; i < boxCount; i++) {
            boxes.addBox(i, in.nextFloat(), in.nextFloat());
        }
        return boxes;
    }

    class Boxes {

        double[] weights;
        double[] volumes;
        int boxCount;

        public Boxes(int boxCount) {
            this.boxCount = boxCount;
            this.weights = new double[boxCount];
            this.volumes = new double[boxCount];
        }

        void addBox(int index, double weight, double volume) {
            weights[index] = weight;
            volumes[index] = volume;
        }
    }

    class CostComputer {
        private final int containerCount;
        private Boxes boxes;
        private double[] volumes;
        private double[] weights;
        private int volumeLimit;

        public CostComputer(Boxes boxes, int containerCount, int volumeLimit) {
            this.boxes = boxes;
            this.containerCount = containerCount;
            volumes = new double[containerCount];
            weights = new double[containerCount];
            this.volumeLimit = volumeLimit;
        }

        public void computeCost(Solution solution) {
            Arrays.fill(volumes, 0);
            Arrays.fill(weights, 0);
            for (int i = 0; i < solution.containerIndexes.length; i++) {
                int boxIndex = solution.containerIndexes[i];
                volumes[boxIndex] += boxes.volumes[i];
                weights[boxIndex] += boxes.weights[i];
            }

            double maxWeight = Double.MIN_VALUE;
            double minWeight = Double.MAX_VALUE;
            double overVolume = 0;
            for (int i = 0; i < containerCount; i++) {
                maxWeight = Math.max(weights[i], maxWeight);
                minWeight = Math.min(weights[i], minWeight);
                overVolume += Math.max(volumes[i] - volumeLimit, 0);
            }
            solution.cost = overVolume * 1_000_000 + maxWeight - minWeight;
        }
    }

    class CrossOverer {
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

    class Mutator {

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

    class Solution {

        int[] containerIndexes;
        double cost;

        public Solution(int containerSize) {
            this.containerIndexes = new int[containerSize];
        }
    }
}
