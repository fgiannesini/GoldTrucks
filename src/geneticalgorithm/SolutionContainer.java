package geneticalgorithm;

import java.util.ArrayList;
import java.util.List;

public class SolutionContainer {

    int[] positions;
    double cost;
    Solution solution;

    public SolutionContainer(int containerCount, int elementCount) {
        solution = new Solution(containerCount, elementCount);
    }
}
