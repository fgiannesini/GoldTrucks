package particleswarmoptimization;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


class Player {

    private static final long COMPUTATION_TIME = 50;
    static int TRUCK_COUNT = 100;
    private static int TRUCK_VOLUME = 100;

    public static void main(String args[]) throws CloneNotSupportedException {
        new Player().launch(new Scanner(System.in));
    }

    void launch(Scanner in) throws CloneNotSupportedException {

        long startTime = System.nanoTime();

        Boxes boxes = createBoxes(in);

        int containerCount = TRUCK_COUNT;
        int nVar = 2 * boxes.boxCount - 1;
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

        //Init
        Random random = new Random();
        SolutionContainer[] solutionContainers = new SolutionContainer[nPop];
        CostComputer costComputer = new CostComputer(boxes);
        Mutator mutator = new Mutator(random, costComputer);

        SolutionContainer globalBest = new SolutionContainer(nVar);
        globalBest.cost = Double.MAX_VALUE;

        for (int j = 0; j < nPop; j++) {
            SolutionContainer solutionContainer = new SolutionContainer(nVar);
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

        int ite = 0;
        while (true) {

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

            long computationTime = System.nanoTime() - startTime;

            if (ite++ % 1000 == 0) {
                System.err.println("Iteration " + ite++ + ": ComputationTime = " + computationTime / 1_000_000d + " ms Best Cost = " + globalBest.cost);
            }

            if (computationTime > (COMPUTATION_TIME - 1) * 1_000_000_000) {
                break;
            }
        }

        int[] truckByBoxIndex = new int[boxes.boxCount];
        double[] truckWeights = new double[globalBest.solution.bIndex];
        double[] truckVolumes = new double[globalBest.solution.bIndex];
        for (int i = 0; i < globalBest.solution.bIndex; i++) {
            for (int j = globalBest.solution.bPositions[i]; j < globalBest.solution.bPositions[i + 1]; j++) {
                int position = globalBest.solution.b[j];
                truckByBoxIndex[position] = i;
                truckVolumes[i] += boxes.volumes[position];
                truckWeights[i] += boxes.weights[position];
            }
        }

        System.err.println("Trucks count : " + globalBest.solution.bIndex);
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

    public static class CostComputer {

        private Boxes boxes;
        private int[] sep;
        private int[] from;
        private int[] to;
        private Position[] positions;

        public CostComputer(Boxes boxes) {
            this.boxes = boxes;
            sep = new int[this.boxes.boxCount - 1];
            from = new int[boxes.boxCount];
            to = new int[boxes.boxCount];
            positions = new Position[boxes.boxCount * 2 - 1];
            for (int i = 0; i < positions.length; i++) {
                positions[i] = new Position();
            }
        }

        public void computeBinPackingCost(SolutionContainer solutionContainer) {
            for (int i = 0; i < solutionContainer.positions.length; i++) {
                positions[i].index = i;
                positions[i].value = solutionContainer.positions[i];
            }
            Arrays.sort(positions, Comparator.comparingDouble(p -> p.value));

            int sepIndex = 0;
            for (int i = 0; i < solutionContainer.positions.length; i++) {
                if (positions[i].index >= boxes.boxCount) {
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
                    solution.b[currentPosition++] = positions[j].index;
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

    public static class Mutator {
        private final Random random;
        private final CostComputer costComputer;

        public Mutator(Random random, CostComputer costComputer) {
            this.random = random;
            this.costComputer = costComputer;
        }

        private double[] mutatePositions(double[] positions) {
            int n = positions.length;
            double[] newPositions = Arrays.copyOf(positions, n);
            int i = random.nextInt(n);
            int j = random.nextInt(n);
            while (i == j) {
                j = random.nextInt(n);
            }
            double temp = newPositions[i];
            newPositions[i] = newPositions[j];
            newPositions[j] = temp;
            return newPositions;
        }

        public void performMutation(int mutationCount, SolutionContainer solutionContainer) throws CloneNotSupportedException {
            SolutionContainer bestSolutionContainer = solutionContainer;
            for (int k = 0; k < mutationCount; k++) {
                SolutionContainer mutatedSolutionContainer = new SolutionContainer(solutionContainer);
                mutatedSolutionContainer.positions = mutatePositions(solutionContainer.positions);
                costComputer.computeBinPackingCost(mutatedSolutionContainer);
                if (mutatedSolutionContainer.cost < bestSolutionContainer.cost) {
                    bestSolutionContainer = mutatedSolutionContainer;
                }
            }

            if (solutionContainer.equals(bestSolutionContainer)) {
                return;
            }
            solutionContainer.cost = bestSolutionContainer.cost;
            solutionContainer.positions = bestSolutionContainer.positions;
            solutionContainer.solution = bestSolutionContainer.solution;
        }
    }

    public static class Position {
        int index;
        double value;
    }

    public static class Solution implements Cloneable {

        public int[] bPositions;
        public int bIndex;
        int nBin;
        int[] b;
        double[] viol;
        double meanViol;

        public Solution(int elementCount) {
            bPositions = new int[elementCount];
            b = new int[elementCount];
            viol = new double[elementCount];
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            Solution solution = new Solution(b.length);
            solution.nBin = this.nBin;
            System.arraycopy(this.b, 0, solution.b, 0, b.length);
            System.arraycopy(this.bPositions, 0, solution.bPositions, 0, bPositions.length);
            solution.bIndex = this.bIndex;
            System.arraycopy(this.viol, 0, solution.viol, 0, viol.length);
            solution.meanViol = this.meanViol;
            return solution;
        }
    }


    public static class SolutionContainer {

        public double[] positions;
        public double cost;
        public Solution solution;
        public double[] velocities;

        public double[] bestPositions;
        public double bestCost;
        public Solution bestSolution;

        public SolutionContainer(int elementCount) {
            solution = new Solution(elementCount);
        }

        public SolutionContainer(SolutionContainer solutionContainer) throws CloneNotSupportedException {
            solution = (Solution) solutionContainer.solution.clone();
            velocities = solutionContainer.velocities;
        }
    }
}