package geneticalgorithm;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GAImplementation {

    public void launch(Model model) {
        int nVar = 2 * model.n - 1;
        int[] varSize = new int[]{1, nVar};
        int MaxIt = 1000;
        int nPop = 40;
        float pc = 0.4f;
        int nc = 2 * Math.round(pc * nPop / 2);
        float pm = 0.8f;
        int nm = Math.round(pm * nPop);
        int beta = 5;

        List<SolutionContainer> solutionContainers = IntStream.range(0, nPop)
                .mapToObj(i -> new SolutionContainer())
                .collect(Collectors.toList());

        for (SolutionContainer solutionContainer : solutionContainers) {
            solutionContainer.solutions = IntStream.range(0, nVar)
                    .mapToObj(Integer::valueOf)
                    .collect(Collectors.toList());
            Collections.shuffle(solutionContainer.solutions);
            binPackingCost(solutionContainer, model);
        }


    }

    private void binPackingCost(SolutionContainer solutionContainer, Model model) {

    }
}
