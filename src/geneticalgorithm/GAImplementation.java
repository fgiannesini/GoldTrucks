package geneticalgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GAImplementation {

  public void launch(Model model) {
    int nVar = 2 * model.n - 1;
    int[] varSize = new int[]{1, nVar};
    int maxIt = 1000;
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
      solutionContainer.positions = IntStream.range(0, nVar)
        .boxed()
        .collect(Collectors.toList());
      Collections.shuffle(solutionContainer.positions);
      computeBinPackingCost(solutionContainer, model);
    }

    Comparator<SolutionContainer> solutionContainerComparator = Comparator.comparingDouble(s -> s.cost);
    solutionContainers.sort(solutionContainerComparator.reversed());

    SolutionContainer bestSol = solutionContainers.get(0);

    double worstCost = solutionContainers.stream().mapToDouble(s->s.cost).max().getAsDouble();

    List<Double> bestCost = new ArrayList<>(maxIt);
  }

  private void computeBinPackingCost(SolutionContainer solutionContainer, Model model) {
    List<Integer> sep = new ArrayList<>();
    for (int i = 0; i < solutionContainer.positions.size(); i++) {
      if (solutionContainer.positions.get(i) > model.n) {
        sep.add(i);
      }
    }

    List<Integer> from = new ArrayList<>();
    from.add(1);
    from.addAll(sep.stream().map(i -> i + 1).collect(Collectors.toList()));

    List<Integer> to = new ArrayList<>();
    to.addAll(sep.stream().map(i -> i - 1).collect(Collectors.toList()));
    to.add(model.n * 2 - 1);

    List<List<Integer>> b = new ArrayList<>();
    for (int i = 0; i < model.n; i++) {
      List<Integer> bi = new ArrayList<>();
      for (int j = from.get(i); j < to.get(i); j++) {
        bi.add(solutionContainer.positions.get(j));
      }
      if (!b.isEmpty()) {
        b.add(bi);
      }
    }

    List<Integer> viol = new ArrayList<>();
    for (List<Integer> aB : b) {
      int vi = aB.stream().mapToInt(j -> model.v[j]).sum();
      viol.add((int)Math.max(((float)vi / (float)model.vMax) - 1, 0));
    }

    solutionContainer.solution.b = b;
    solutionContainer.solution.meanViol = viol.stream().mapToInt(Integer::intValue).average().getAsDouble();
    solutionContainer.solution.nBin = b.size();
    solutionContainer.solution.viol = viol;

    solutionContainer.cost = b.size() + 10 * model.n * solutionContainer.solution.meanViol;
  }
}
