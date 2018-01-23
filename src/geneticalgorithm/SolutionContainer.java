package geneticalgorithm;

import java.util.ArrayList;
import java.util.List;

public class SolutionContainer {

    List<Integer> positions;
    List<Float> costs;
    List<Integer> solutions;

    public SolutionContainer() {
        this.costs = new ArrayList<>();
        this.positions = new ArrayList<>();
        this.solutions = new ArrayList<>();
    }
}
