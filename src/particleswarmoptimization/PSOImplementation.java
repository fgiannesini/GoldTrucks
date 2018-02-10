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
        Mutator mutator = new Mutator(random, costComputer);

        SolutionContainer globalBest = new SolutionContainer(containerCount, nVar);
        globalBest.cost = Double.MAX_VALUE;

        for (int j = 0; j < nPop; j++) {
            SolutionContainer solutionContainer = new SolutionContainer(containerCount, nVar);
            solutionContainer.positions = IntStream.range(0, nVar).mapToDouble(i -> random.nextDouble()).toArray();
            solutionContainer.velocities = new double[nVar];
            costComputer.computeBinPackingCost(solutionContainer);
            solutionContainer.bestCost = solutionContainer.cost;
            solutionContainer.bestPositions = solutionContainer.positions.clone();
            solutionContainer.bestSolution = (Solution) solutionContainer.solution.clone();
            if (solutionContainer.cost < globalBest.cost) {
                globalBest.cost = solutionContainer.cost;
                globalBest.positions = solutionContainer.positions.clone();
                globalBest.solution = (Solution) solutionContainer.solution.clone();
            }
            solutionContainers[j] = solutionContainer;
        }

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
                mutator.performMutation(particuleMutationCount, solutionContainer);

                //Update Personal Best
                SolutionContainer currentSolutionContainer = solutionContainers[j];
                if (currentSolutionContainer.cost < currentSolutionContainer.bestCost) {
                    currentSolutionContainer.bestCost = currentSolutionContainer.cost;
                    System.arraycopy(currentSolutionContainer.positions, 0, currentSolutionContainer.bestPositions, 0, nVar);
                    currentSolutionContainer.bestSolution = (Solution) currentSolutionContainer.solution.clone();
                }

                //Update global best
                if (currentSolutionContainer.cost < globalBest.cost) {
                    globalBest.cost = currentSolutionContainer.cost;
                    System.arraycopy(currentSolutionContainer.positions, 0, globalBest.positions, 0, nVar);
                    globalBest.solution = (Solution) currentSolutionContainer.solution.clone();
                }


            }

            mutator.performMutation(globalBestMutation, globalBest);

            w *= wdamp;

            System.out.println("Iteration " + i + " cost " + globalBest.cost);
        }

        displaySolution(model, globalBest);
    }

    private void displaySolution(Model model, SolutionContainer globalBest) {
        for (int i = 0; i < globalBest.solution.bIndex; i++) {
            int resultSum = 0;
            for (int j = globalBest.solution.bPositions[i]; j < globalBest.solution.bPositions[i + 1]; j++) {
                resultSum += model.v[j];
            }
            System.out.println(resultSum);
        }
    }


}
