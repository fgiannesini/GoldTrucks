package particleswarmoptimization;

public class SolutionContainer {

    public double[] positions;
    public double cost;
    public Solution solution;
    public double[] velocities;

    public double[] bestPositions;
    public double bestCost;
    public Solution bestSolution;

    public SolutionContainer(int containerCount, int elementCount) {
        solution = new Solution(containerCount, elementCount);
    }
}
