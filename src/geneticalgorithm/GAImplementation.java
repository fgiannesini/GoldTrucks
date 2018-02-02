package geneticalgorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GAImplementation {

  public void launch(Model model) {
    int nVar = 2 * model.n - 1;
    int maxIt = 1000;
    int nPop = 40;
    float pc = 0.4f;
    int nc = 2 * Math.round(pc * nPop / 2);
    float pm = 0.8f;
    int nm = Math.round(pm * nPop);
    int containerCount = 10;

    //Init
    Random random = new Random();
    List<SolutionContainer> solutionContainers = new ArrayList<>();

    for (int j = 0; j < nPop; j++) {
      SolutionContainer solutionContainer = new SolutionContainer(containerCount, nVar);
      solutionContainer.positions = IntStream.range(0, nVar).toArray();
      randomizePositions(random, solutionContainer.positions);
      computeBinPackingCost(solutionContainer, model);
      solutionContainers.add(solutionContainer);
    }

    Comparator<SolutionContainer> solutionContainerComparator = Comparator.comparingDouble(s -> s.cost);

    solutionContainers.sort(solutionContainerComparator);

    SolutionContainer bestSol = solutionContainers.get(0);

    for (int i = 0; i < maxIt; i++) {

      //CrossOver
      double[] p = computeProbabilities(nPop, solutionContainers);

      List<SolutionContainer> crossOverSolutions = new ArrayList<>();
      for (int k = 0; k < nc / 2; k++) {
        int i1 = rouletteWheelSelection(random, p);
        SolutionContainer solution1 = solutionContainers.get(i1);

        int i2 = rouletteWheelSelection(random, p);
        SolutionContainer solution2 = solutionContainers.get(i2);

        permuationCrossOver(crossOverSolutions, solution1.positions, solution2.positions, random);

        int index = crossOverSolutions.size() - 1;
        computeBinPackingCost(crossOverSolutions.get(index), model);
        computeBinPackingCost(crossOverSolutions.get(index - 1), model);
      }

      //Mutation
      List<SolutionContainer> mutationSolutions = new ArrayList<>();
      for (int k = 0; k < nm; k++) {
        //Select Parent Index
        int parentIndex = random.nextInt(nPop);

        //Select Parent
        SolutionContainer parent = solutionContainers.get(parentIndex);

        //Apply Mutation
        permutationMutate(mutationSolutions, parent.positions, random);

        //Evaluate Mutant
        int index = mutationSolutions.size() - 1;
        computeBinPackingCost(mutationSolutions.get(index), model);
      }

      // Merge Population
      solutionContainers = Stream.of(solutionContainers, crossOverSolutions, mutationSolutions)
        .flatMap(List::stream)
        .sorted(solutionContainerComparator)
        .limit(nPop)
        .collect(Collectors.toList());

      // Update Best Solution Ever Found
      bestSol = solutionContainers.get(0);

      System.out.println("Iteration " + i + " : Best Cost = " + bestSol.cost);
    }

    for (int i = 0; i < bestSol.solution.bIndex; i++) {
      System.out.println(bestSol.solution.viol[i]);
    }
  }

  private void randomizePositions(Random random, int[] positions) {
    for (int i = 0; i < positions.length; i++) {
      int randomPosition = random.nextInt(positions.length);
      int temp = positions[i];
      positions[i] = positions[randomPosition];
      positions[randomPosition] = temp;
    }
  }

  private double[] computeProbabilities(int nPop, List<SolutionContainer> solutionContainers) {
    double worstCost = solutionContainers.get(nPop - 1).cost;
    double sumP = 0;
    double[] p = new double[nPop];
    for (int s = 0; s < nPop; s++) {
      p[s] = Math.exp(-5 * solutionContainers.get(s).cost / worstCost);
      sumP += p[s];
    }
    p[0] /= sumP;
    for (int s = 1; s < nPop; s++) {
      p[s] = p[s - 1] / sumP;
    }
    return p;
  }

  private int rouletteWheelSelection(Random random, double[] p) {
    double r = random.nextDouble();
    for (int s = 0; s < p.length; s++) {
      if (p[s] > r) {
        return s;
      }
    }
    return 0;
  }

  void permutationMutate(List<SolutionContainer> mutationSolutions, int[] positions, Random random) {

    int mode = random.nextInt(3);
    SolutionContainer solutionContainer = new SolutionContainer(10, positions.length);
    switch (mode) {
      case 0:
        //Swap
        solutionContainer.positions = doSwap(positions, random);
        break;
      case 1:
        // Reversion
        solutionContainer.positions = doReversion(positions, random);
        break;
      case 2:
        // Insertion
        solutionContainer.positions = doInsertion(positions, random);
        break;
      default:
    }
    mutationSolutions.add(solutionContainer);
  }

  private int[] doInsertion(int[] positions, Random random) {

    int j1 = random.nextInt(positions.length);
    int j2 = random.nextInt(positions.length);
    while (j2 == j1) {
      j2 = random.nextInt(positions.length);
    }
    int i1 = Math.min(j1, j2);
    int i2 = Math.max(j1, j2);

    int[] newPositions = new int[positions.length];
    System.arraycopy(positions, 0, newPositions, 0, i1);
    System.arraycopy(positions, i1 + 1, newPositions, i1, i2 - i1 - 1);
    newPositions[i2 - 1] = positions[i1];
    System.arraycopy(positions, i2, newPositions, i2, positions.length - i2);

    return newPositions;
  }

  private int[] doReversion(int[] positions, Random random) {
    int j1 = random.nextInt(positions.length);
    int j2 = random.nextInt(positions.length);
    int i1 = Math.min(j1, j2);
    int i2 = Math.max(j1, j2);

    int[] newPositions = new int[positions.length];
    System.arraycopy(positions, 0, newPositions, 0, i1);
    for (int i = 0; i < i2 - i1; i++) {
      newPositions[i1 + i] = positions[i2 - i - 1];
    }
    System.arraycopy(positions, i2, newPositions, i2, positions.length - i2);

    return newPositions;
  }

  private int[] doSwap(int[] positions, Random random) {
    int[] newPositions = new int[positions.length];
    System.arraycopy(positions, 0, newPositions, 0, positions.length);
    int i = random.nextInt(positions.length);
    int j = random.nextInt(positions.length);
    newPositions[j] = positions[i];
    newPositions[i] = positions[j];
    return newPositions;
  }

  void permuationCrossOver(List<SolutionContainer> crossOverSolutions, int[] positions1, int[] positions2, Random random) {

    int nVar = positions1.length;
    int c = random.nextInt(nVar - 1);

    int[] x11 = new int[c];
    System.arraycopy(positions1, 0, x11, 0, x11.length);
    int[] x12 = new int[nVar - c];
    System.arraycopy(positions1, c, x12, 0, x12.length);

    int[] x21 = new int[c];
    System.arraycopy(positions2, 0, x21, 0, x21.length);
    int[] x22 = new int[nVar - c];
    System.arraycopy(positions2, c, x22, 0, x22.length);

    int[] r1 = new int[nVar];
    int r1Index = 0;

    int[] r2 = new int[nVar];
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

    SolutionContainer solutionContainer1 = new SolutionContainer(10, positions1.length);
    solutionContainer1.positions = new int[nVar];
    System.arraycopy(x11, 0, solutionContainer1.positions, 0, c);
    System.arraycopy(x22, 0, solutionContainer1.positions, c, nVar - c);
    crossOverSolutions.add(solutionContainer1);

    SolutionContainer solutionContainer2 = new SolutionContainer(10, positions2.length);
    solutionContainer2.positions = new int[nVar];
    System.arraycopy(x21, 0, solutionContainer2.positions, 0, c);
    System.arraycopy(x12, 0, solutionContainer2.positions, c, nVar - c);

    crossOverSolutions.add(solutionContainer2);
  }

  public void computeBinPackingCost(SolutionContainer solutionContainer, Model model) {
    int[] sep = new int[model.n - 1];
    int sepIndex = 0;
    for (int i = 0; i < solutionContainer.positions.length; i++) {
      if (solutionContainer.positions[i] >= model.n) {
        sep[sepIndex++] = i;
      }
    }

    int[] from = new int[model.n];
    from[0] = 0;
    for (int i = 0; i < sepIndex; i++) {
      from[i + 1] = sep[i] + 1;
    }

    int[] to = new int[model.n];
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
        solution.viol[i] += model.v[j];
      }
      solution.viol[i] = Math.max((solution.viol[i] / (float)model.vMax) - 1, 0d);
    }

    double violMean = 0;
    for (double aViol : solution.viol) {
      violMean += aViol;
    }
    violMean /= solution.bIndex;

    solution.meanViol = violMean;

    solution.nBin = solution.bIndex;

    solutionContainer.cost = solution.nBin + 10 * model.n * solution.meanViol;
  }
}
