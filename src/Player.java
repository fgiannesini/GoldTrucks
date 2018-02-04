import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


class Player {

  private static final long COMPUTATION_TIME = 50;
  static int TRUCK_COUNT = 100;
  private static int TRUCK_VOLUME = 100;

  public static void main(String args[]) {
    new Player().launch(new Scanner(System.in));
  }

  void launch(Scanner in) {

    long startTime = System.nanoTime();

    Boxes boxes = createBoxes(in);

    int nVar = 2 * boxes.boxCount - 1;
    int nPop = 40;
    float pc = 0.4f;
    int nc = 2 * Math.round(pc * nPop / 2);
    float pm = 0.8f;
    int nm = Math.round(pm * nPop);
    int containerCount = TRUCK_COUNT;

    //Init
    Random random = new Random();
    SolutionContainer[] solutionContainers = new SolutionContainer[nPop + nc + nm];
    CostComputer costComputer = new CostComputer(boxes);

    for (int j = 0; j < nPop; j++) {
      SolutionContainer solutionContainer = new SolutionContainer(containerCount, nVar);
      solutionContainer.positions = IntStream.range(0, nVar).toArray();
      randomizePositions(random, solutionContainer.positions);
      costComputer.computeBinPackingCost(solutionContainer);
      solutionContainers[j] = solutionContainer;
    }

    for (int j = nPop; j < nPop + nc + nm; j++) {
      SolutionContainer solutionContainer = new SolutionContainer(containerCount, nVar);
      solutionContainer.positions = new int[nVar];
      solutionContainers[j] = solutionContainer;
    }

    Comparator<SolutionContainer> solutionContainerComparator = Comparator.comparingDouble(s -> s.cost);

    Arrays.sort(solutionContainers, 0, nPop, solutionContainerComparator);

    CrossOverComputer crossOverComputer = new CrossOverComputer(solutionContainers, random, nPop, nPop + nc, nPop);
    MutationComputer mutationComputer = new MutationComputer(solutionContainers, random, nPop + nc, nPop + nc + nm, nPop);

    SolutionContainer bestSol;

    int ite = 0;
    while (true) {

      //CrossOverComputer
      crossOverComputer.computeCrossOver();

      //MutationComputer
      mutationComputer.mutate();

      // Sort Population
      for (int scIndex = nPop; scIndex < nPop + nc + nm; scIndex++) {
        costComputer.computeBinPackingCost(solutionContainers[scIndex]);
      }
      Arrays.sort(solutionContainers, solutionContainerComparator);

      // Update Best Solution Ever Found
      bestSol = solutionContainers[0];
      
      long computationTime = System.nanoTime() - startTime;

      if (ite++ % 1000 == 0) {
        System.err.println("Iteration " + ite++ + ": ComputationTime = " + computationTime / 1_000_000d + " ms Best Cost = " + bestSol.cost);
      }

      if (computationTime > (COMPUTATION_TIME - 1) * 1_000_000_000) {
        break;
      }
    }

    int[] truckByBoxIndex = new int[boxes.boxCount];
    double[] truckWeights = new double[bestSol.solution.bIndex];
    double[] truckVolumes = new double[bestSol.solution.bIndex];
    for (int i = 0; i < bestSol.solution.bIndex; i++) {
      for (int j = bestSol.solution.bPositions[i]; j < bestSol.solution.bPositions[i + 1]; j++) {
        int position = bestSol.solution.b[j];
        truckByBoxIndex[position] = i;
        truckVolumes[i] += boxes.volumes[position];
        truckWeights[i] += boxes.weights[position];
      }
    }

    System.err.println("Trucks count : " + bestSol.solution.bIndex);
    System.err.println("truck Volumes > 100" + Arrays.stream(truckVolumes).filter(v -> v >= 100).mapToObj(String::valueOf).collect(Collectors.joining(" ")));
    DoubleSummaryStatistics summaryStatistics = Arrays.stream(truckWeights).summaryStatistics();
    System.err.println("Max Weight : " + summaryStatistics.getMax());
    System.err.println("Min Weight : " + summaryStatistics.getMin());

    StringBuilder resultBuilder = new StringBuilder();
    for (int boxIndex : truckByBoxIndex) {
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

  private void randomizePositions(Random random, int[] positions) {
    for (int i = 0; i < positions.length; i++) {
      int randomPosition = random.nextInt(positions.length);
      int temp = positions[i];
      positions[i] = positions[randomPosition];
      positions[randomPosition] = temp;
    }
  }

  public static class Boxes {

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

  static class CostComputer {

    private Boxes boxes;
    private int[] sep;
    private int[] from;
    private int[] to;

    public CostComputer(Boxes boxes) {
      this.boxes = boxes;
      sep = new int[this.boxes.boxCount - 1];
      from = new int[boxes.boxCount];
      to = new int[boxes.boxCount];
    }

    public void computeBinPackingCost(SolutionContainer solutionContainer) {
      int sepIndex = 0;
      for (int i = 0; i < solutionContainer.positions.length; i++) {
        if (solutionContainer.positions[i] >= boxes.boxCount) {
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
      to[boxes.boxCount - 1] = boxes.boxCount * 2 - 1;

      Solution solution = solutionContainer.solution;
      solution.bIndex = 0;
      solution.bPositions[solution.bIndex] = 0;
      for (int i = 0; i < boxes.boxCount; i++) {
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

      double maxWeight = Double.MIN_VALUE;
      double minWeight = Double.MAX_VALUE;
      for (int i = 0; i < solution.bIndex; i++) {
        solution.viol[i] = 0;
        double weight = 0;
        for (int j = solution.bPositions[i]; j < solution.bPositions[i + 1]; j++) {
          int position = solution.b[j];
          solution.viol[i] += boxes.volumes[position];
          weight += boxes.weights[position];
        }
        solution.viol[i] = Math.max((solution.viol[i] / (float) TRUCK_VOLUME) - 1, 0d);
        maxWeight = Math.max(maxWeight, weight);
        minWeight = Math.min(minWeight, weight);
      }

      double violMean = 0;
      for (int i = 0; i < solution.bIndex; i++) {
        violMean += solution.viol[i];
      }
      violMean /= solution.bIndex;

      solution.meanViol = violMean;

      solution.nBin = solution.bIndex;

      solutionContainer.cost = maxWeight - minWeight + boxes.boxCount * solution.meanViol + Math.abs(TRUCK_COUNT - solution.nBin) * 1000;
    }
  }

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

    public void computeCrossOver() {
      computeProbabilities();

      for (int k = beginIndex; k < endIndex; k += 2) {
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
          doSwap(positions, positionsToMutate);
          break;
        case 1:
          // Reversion
          doReversion(positions, positionsToMutate);
          break;
        case 2:
          // Insertion
          doInsertion(positions, positionsToMutate);
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

  public class Solution {

    int nBin;
    int[] b;
    public int[] bPositions;
    public int bIndex;
    double[] viol;
    double meanViol;

    public Solution(int containerCount, int elementCount) {
      bPositions = new int[elementCount];
      b = new int[elementCount];
      viol = new double[elementCount];
    }
  }

  public class SolutionContainer {

    public int[] positions;
    public double cost;
    public Solution solution;

    public SolutionContainer(int containerCount, int elementCount) {
      solution = new Solution(containerCount, elementCount);
    }
  }
}