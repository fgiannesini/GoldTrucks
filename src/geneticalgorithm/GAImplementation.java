package geneticalgorithm;

import java.util.*;
import java.util.stream.IntStream;

public class GAImplementation {

  public void launch(Model model) {
    int nVar = 2 * model.n - 1;
    int maxIt = 1000;
    int nPop = 40;
    float pc = 0.4f;
    int nc = 2 * Math.round(pc * nPop / 2);
    float pm = 0.8f;
    int nm = Math.round(pm * nPop);
    int containerCount = model.n;

    //Init
    Random random = new Random();
    SolutionContainer[] solutionContainers = new SolutionContainer[nPop + nc + nm];
    CostComputer costComputer = new CostComputer(model);

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

    Arrays.sort(solutionContainers,0,nPop,solutionContainerComparator);

    SolutionContainer bestSol = solutionContainers[0];

    CrossOverComputer crossOverComputer = new CrossOverComputer(solutionContainers, random, nPop, nPop + nc, nPop);
    MutationComputer mutationComputer = new MutationComputer(solutionContainers, random, nPop + nc, nPop + nc + nm, nPop);

    for (int i = 0; i < maxIt; i++) {

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

      System.out.println("Iteration " + i + " : Best Cost = " + bestSol.cost);
    }

    for (int i = 0; i < bestSol.solution.bIndex; i++) {
      int resultSum = 0;
      for (int j = bestSol.solution.bPositions[i]; j < bestSol.solution.bPositions[i + 1]; j++) {
        resultSum  += model.v[j];
      }
      System.out.println(resultSum);
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



}
