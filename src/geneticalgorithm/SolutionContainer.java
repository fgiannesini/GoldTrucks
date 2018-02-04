package geneticalgorithm;

public class SolutionContainer {

    public int[] positions;
    public double cost;
    public Solution solution;

    public SolutionContainer(int containerCount, int elementCount) {
        solution = new Solution(containerCount, elementCount);
    }
}
