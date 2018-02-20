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
    int newSolutionsCount = 5;

    Solution[] solutions = new Solution[populationCount + mutationCount + crossOverCount + newSolutionsCount];

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

    for (int i = populationCount; i < populationCount + mutationCount + crossOverCount + newSolutionsCount; i++) {
      solutions[i] = new Solution(boxes.boxCount);
    }

    Mutator mutator = new Mutator(random);
    CrossOverer crossOverer = new CrossOverer();

    int ite = 0;
    while (true) {

      //CrossOver 90784 -> 79868
      for (int i = 0; i < crossOverCount; i++) {
        int crossOverIndex1 = random.nextInt(populationCount);
        int crossOverIndex2 = random.nextInt(populationCount);
        crossOverer.applyCrossOver(solutions[crossOverIndex1], solutions[crossOverIndex2], solutions[populationCount + i]);
      }

      //Mutate 81740 -> 55584
      for (int i = 0; i < mutationCount; i++) {
        int mutatedIndex = random.nextInt(populationCount);
        mutator.mutate(solutions[mutatedIndex], solutions[populationCount + crossOverCount + i]);
      }

      //Create new 84998 -> 60649
      int[] newRandomContainers = random.ints(boxes.boxCount * newSolutionsCount, 0, TRUCK_COUNT).toArray();
      for (int i = 0; i < newSolutionsCount; i++) {
        int index = populationCount + crossOverCount + mutationCount + i;
        System.arraycopy(newRandomContainers, i * boxes.boxCount, solutions[index].containerIndexes, 0, solutions[index].containerIndexes.length);
      }

      //Compute Cost
      for (int i = populationCount; i < populationCount + crossOverCount + mutationCount + newSolutionsCount; i++) {
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

}
