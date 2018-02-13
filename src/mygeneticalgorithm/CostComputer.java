package mygeneticalgorithm;

import java.util.Arrays;

public class CostComputer {
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
        solution.cost = overVolume * 100 + maxWeight - minWeight;
    }
}
