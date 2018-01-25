package geneticalgorithm;

import java.util.ArrayList;
import java.util.List;

public class SolutionContainer {

    List<Integer> positions;
    double cost;
    Solution solution;

    public SolutionContainer() {
        this.positions = new ArrayList<>();
        solution = new Solution();
    }
}
