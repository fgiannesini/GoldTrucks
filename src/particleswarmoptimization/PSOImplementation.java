package particleswarmoptimization;

import java.util.Random;
import java.util.stream.IntStream;

public class PSOImplementation {

  public void launch(Model model) throws CloneNotSupportedException {
    int nVar = 2 * model.n - 1;
    int maxIt = 1000;
    int nPop = 50;
    int particuleMutationCount = 2;
    int globalBestMutation = 5;

    int varMin = 0;
    int varMax = 1;
    int w = 1;
    double wdamp = 0.99;
    double c1 = 1.5;
    double c2 = 2.0;
    double velMax = 0.1 * (varMax - varMin);
    double velMin = -velMax;
    int containerCount = model.n;

    //Init
    Random random = new Random();
    SolutionContainer[] solutionContainers = new SolutionContainer[nPop];
    CostComputer costComputer = new CostComputer(model);
    Mutator mutator = new Mutator(random);

    SolutionContainer globalBest = new SolutionContainer(containerCount, nVar);
    globalBest.cost = Double.MAX_VALUE;

    for (int j = 0; j < nPop; j++) {
      SolutionContainer solutionContainer = new SolutionContainer(containerCount, nVar);
      solutionContainer.positions = IntStream.range(0, nVar).mapToDouble(i -> random.nextDouble()).toArray();
      solutionContainer.velocities = new double[nVar];
      costComputer.computeBinPackingCost(solutionContainer);
      solutionContainer.bestCost = solutionContainer.cost;
      System.arraycopy(solutionContainer.positions, 0, solutionContainer.bestPositions, 0, nVar);
      solutionContainer.bestSolution = (Solution)solutionContainer.solution.clone();
      if (solutionContainer.cost < globalBest.cost) {
        globalBest.cost = solutionContainer.bestCost;
        System.arraycopy(solutionContainer.bestPositions, 0, globalBest.bestPositions, 0, nVar);
        globalBest.solution = (Solution)solutionContainer.bestSolution.clone();
      }
      solutionContainers[j] = solutionContainer;
    }

    double[] bestCosts = new double[maxIt];

    for (int i = 0; i < maxIt; i++) {
      for (int j = 0; j < nPop; j++) {
        SolutionContainer solutionContainer = solutionContainers[j];
        double[] velocities = solutionContainer.velocities;
        for (int k = 0; k < velocities.length; k++) {
          //Update velocities
          double velocity = w * velocities[k]
                            + c1 * random.nextDouble() * (solutionContainer.bestPositions[k] - solutionContainer.positions[k])
                            + c2 * random.nextDouble() * (globalBest.positions[k] - solutionContainer.positions[k]);

          //Apply Velocity Limits
          velocity = Math.max(velocity, velMin);
          velocity = Math.min(velocity, velMax);

          //Update Position
          double position = solutionContainer.positions[k];
          position += velocity;

          //Velocity Mirror Effect
          if (position < varMin || position > varMax) {
            velocity *= -1;
          }

          //Apply Position Limits
          position = Math.max(position, varMin);
          position = Math.min(position, varMax);

          solutionContainer.positions[k] = position;
          velocities[k] = velocity;
        }

        //Evaluation
        costComputer.computeBinPackingCost(solutionContainer);

        //Perform Mutation
        for (int k = 0; k < particuleMutationCount; k++) {
          SolutionContainer mutatedSolutionContainer = new SolutionContainer(containerCount, nVar);
          mutatedSolutionContainer.solution = (Solution)solutionContainer.solution.clone();
          mutatedSolutionContainer.velocities = solutionContainer.velocities;

          mutatedSolutionContainer.bestSolution = solutionContainer.bestSolution;
          mutatedSolutionContainer.bestCost = solutionContainer.bestCost;
          mutatedSolutionContainer.bestPositions = solutionContainer.bestPositions;

          mutatedSolutionContainer.positions = mutator.mutate(solutionContainer.positions);
          costComputer.computeBinPackingCost(solutionContainer);

          if (mutatedSolutionContainer.cost > solutionContainer.cost) {
            solutionContainers[j] = mutatedSolutionContainer;
          }
        }
      }
    }

    for (int i = 0; i < bestSol.solution.bIndex; i++) {
      int resultSum = 0;
      for (int j = bestSol.solution.bPositions[i]; j < bestSol.solution.bPositions[i + 1]; j++) {
        resultSum += model.v[j];
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
