package mygeneticalgorithm;

import java.util.*;
import java.util.stream.Collectors;

public class Player {

    private static final long COMPUTATION_TIME = 50;
    static int TRUCK_COUNT = 100;
    private static int TRUCK_VOLUME = 100;

    public static void main(String args[]) {
        new Player().launch(new Scanner(System.in));
    }


    void launch(Scanner in) {

        long startTime = System.nanoTime();

        Boxes boxes = createBoxes(in);

        int populationCount = 40;
        int mutationCount = 20;
        int crossOverCount = 20;

        Solution[] solutions = new Solution[populationCount + mutationCount + crossOverCount];

        //Init
        Random random = new Random();
        CostComputer costComputer = new CostComputer(boxes, TRUCK_COUNT, TRUCK_VOLUME);

        for (int i = 0; i < populationCount; i++) {
            Solution solution = new Solution(boxes.boxCount);
            for (int j = 0; j < solution.containerIndexes.length; j++) {
                solution.containerIndexes[j] = random.nextInt(TRUCK_COUNT);
            }
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

            //CrossOver

            //Mutate
            for (int i = 0; i < mutationCount; i++) {
                int mutatedIndex = random.nextInt(populationCount);
                mutator.mutate(solutions[mutatedIndex], solutions[populationCount + i]);
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

        double[] truckWeights = new double[TRUCK_VOLUME];
        double[] truckVolumes = new double[TRUCK_VOLUME];
        int[] bestContainerIndexes = solutions[0].containerIndexes;
        for (int i = 0; i < bestContainerIndexes.length; i++) {
            int boxIndex = bestContainerIndexes[i];
            truckVolumes[boxIndex] += boxes.volumes[i];
            truckWeights[boxIndex] += boxes.weights[i];
        }

        System.err.println("truck Volumes > 100" + Arrays.stream(truckVolumes).filter(v -> v >= 100).mapToObj(String::valueOf).collect(Collectors.joining(" ")));
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

}
