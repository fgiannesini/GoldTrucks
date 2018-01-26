package geneticalgorithm;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GAImplementation {

  public void launch(Model model) {
    int nVar = 2 * model.n - 1;
    int maxIt = 1000;
    int nPop = 40;
    float pc = 0.4f;
    int nc = 2 * Math.round(pc * nPop / 2);
    float pm = 0.8f;
    int nm = Math.round(pm * nPop);
    int beta = 5;

    //Init
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
    solutionContainerComparator = solutionContainerComparator.reversed();
    solutionContainers.sort(solutionContainerComparator);

    SolutionContainer bestSol = solutionContainers.get(0);

    double worstCost = solutionContainers.stream().mapToDouble(s -> s.cost).max().getAsDouble();

    List<Double> bestCost = new ArrayList<>(maxIt);

    for (int i = 0; i < maxIt; i++) {
      //Probabilities
      List<Double> p = computeProbabilities(beta, solutionContainers, worstCost);

      //CrossOver
      List<SolutionContainer> crossOverSolutions = new ArrayList<>();
      for (int k = 0; k < nc / 2; k++) {
        int i1 = rouletteWeelSelection(p);
        SolutionContainer solution1 = solutionContainers.get(i1);

        int i2 = rouletteWeelSelection(p);
        SolutionContainer solution2 = solutionContainers.get(i2);

        permuationCrossOver(crossOverSolutions, solution1.positions, solution2.positions);

        int index = crossOverSolutions.size() - 1;
        computeBinPackingCost(crossOverSolutions.get(index), model);
        computeBinPackingCost(crossOverSolutions.get(index - 1), model);
      }

      //Mutation
      List<SolutionContainer> mutationSolutions = new ArrayList<>();
      for (int k = 0; k < nm; k++) {
        //Select Parent Index
        int parentIndex = new Random().nextInt(nPop);

        //Select Parent
        SolutionContainer parent = solutionContainers.get(parentIndex);

        //Apply Mutation
        permutationMutate(mutationSolutions, parent.positions);

        //Evaluate Mutant
        int index = mutationSolutions.size() - 1;
        computeBinPackingCost(mutationSolutions.get(index), model);
      }

      // Merge Population
      solutionContainers.addAll(crossOverSolutions);
      solutionContainers.addAll(mutationSolutions);

      // Sort Population
      solutionContainers.sort(solutionContainerComparator);

      // Truancate Extra Memebrs
      solutionContainers = solutionContainers.subList(0, nPop);

      // Update Best Solution Ever Found
      bestSol = solutionContainers.get(0);

      // Update Worst Cost
      worstCost = solutionContainers.stream().mapToDouble(s -> s.cost).max().getAsDouble();

      // Update Best Cost Ever Found
      bestCost.add(bestSol.cost);

      System.out.println("Iteration " + i + " : Best Cost = " + bestSol.cost);
    }
  }

  private List<Double> computeProbabilities(int beta, List<SolutionContainer> solutionContainers, double worstCost) {
    List<Double> p = solutionContainers.stream()
      .map(s -> Math.exp(-beta * s.cost / worstCost))
      .collect(Collectors.toList());
    double pSum = p.stream().mapToDouble(Double::valueOf).sum();
    p = p.stream().map(d -> d / pSum).collect(Collectors.toList());
    return p;
  }

  private void permutationMutate(List<SolutionContainer> mutationSolutions, List<Integer> positions) {

    int mode = new Random().nextInt(2);
    SolutionContainer solutionContainer = new SolutionContainer();
    switch (mode) {
      case 0:
        // Swap
        solutionContainer.positions = doSwap(positions);
        break;

      case 1:
        // Reversion
        solutionContainer.positions = doReversion(positions);
        break;
      case 2:
        // Insertion
        solutionContainer.positions = doInsertion(positions);
        break;
    }
    mutationSolutions.add(solutionContainer);
  }

  private List<Integer> doInsertion(List<Integer> positions) {

    Random random = new Random();
    int j1 = random.nextInt(positions.size());
    int j2 = random.nextInt(positions.size());
    int i1 = Math.min(j1, j2);
    int i2 = Math.max(j1, j2);

    List<Integer> newPositions = IntStream.range(0, i1 - 1)
      .mapToObj(i -> positions.get(i))
      .collect(Collectors.toList());
    IntStream.range(i1 + 1, i2).forEach(i -> newPositions.add(positions.get(i)));
    newPositions.add(positions.get(i1));
    IntStream.range(i2 + 1, positions.size()).forEach(i -> newPositions.add(positions.get(i)));

    return newPositions;
  }

  private List<Integer> doReversion(List<Integer> positions) {
    Random random = new Random();
    int j1 = random.nextInt(positions.size());
    int j2 = random.nextInt(positions.size());
    int i1 = Math.min(j1, j2);
    int i2 = Math.max(j1, j2);

    List<Integer> newPositions = IntStream.range(0, i1).mapToObj(i -> positions.get(i)).collect(Collectors.toList());
    List<Integer> positionsToReverse = IntStream.range(i1, i2)
      .mapToObj(i -> positions.get(i))
      .collect(Collectors.toList());
    Collections.reverse(positionsToReverse);
    newPositions.addAll(positionsToReverse);
    newPositions.addAll(IntStream.range(12, positions.size()).mapToObj(i -> positions.get(i)).collect(Collectors.toList()));
    return newPositions;
  }

  private List<Integer> doSwap(List<Integer> positions) {
    List<Integer> newPositions = new ArrayList<>(positions);
    Random random = new Random();
    Collections.swap(newPositions, random.nextInt(positions.size()), random.nextInt(positions.size()));
    return newPositions;
  }

  private void permuationCrossOver(List<SolutionContainer> crossOverSolutions, List<Integer> positions1, List<Integer> positions2) {

    int nVar = positions1.size();
    int c = new Random().nextInt(nVar - 1);
    List<Integer> x11 = IntStream.range(0, c).mapToObj(i -> positions1.get(i)).collect(Collectors.toList());
    List<Integer> x12 = IntStream.range(c, positions1.size()).mapToObj(i -> positions1.get(i)).collect(Collectors.toList());

    List<Integer> x21 = IntStream.range(0, c).mapToObj(i -> positions2.get(i)).collect(Collectors.toList());
    List<Integer> x22 = IntStream.range(c, positions2.size()).mapToObj(i -> positions2.get(i)).collect(Collectors.toList());

    Set<Integer> r1 = new HashSet<>(x11);
    r1.retainAll(x22);
    Set<Integer> r2 = new HashSet<>(x12);
    r2.retainAll(x21);

    Iterator<Integer> r1Iterator = r1.iterator();
    Iterator<Integer> r2Iterator = r2.iterator();
    for (int i = 0; i < x11.size(); i++) {
      if (r1.contains(x11.get(i))) {
        x11.set(i, r2Iterator.next());
      }
    }

    for (int i = 0; i < x21.size(); i++) {
      if (r2.contains(x21.get(i))) {
        x21.set(i, r1Iterator.next());
      }
    }

    SolutionContainer solutionContainer1 = new SolutionContainer();
    solutionContainer1.positions.addAll(x11);
    solutionContainer1.positions.addAll(x22);

    SolutionContainer solutionContainer2 = new SolutionContainer();
    solutionContainer2.positions.addAll(x21);
    solutionContainer2.positions.addAll(x12);

    crossOverSolutions.add(solutionContainer1);
    crossOverSolutions.add(solutionContainer2);
  }

  private int rouletteWeelSelection(List<Double> p) {
    double random = new Random().nextDouble();
    double cumul = 0;
    for (int i = 0; i < p.size(); i++) {
      cumul += p.get(i);
      if (cumul > random) {
        return i;
      }
    }
    return 0;
  }

  private void computeBinPackingCost(SolutionContainer solutionContainer, Model model) {
    List<Integer> sep = new ArrayList<>();
    for (int i = 0; i < solutionContainer.positions.size(); i++) {
      if (solutionContainer.positions.get(i) >= model.n - 1) {
        sep.add(i);
      }
    }

    List<Integer> from = new ArrayList<>();
    from.add(0);
    from.addAll(sep.stream().map(i -> i + 1).collect(Collectors.toList()));

    List<Integer> to = new ArrayList<>();
    to.addAll(sep.stream().map(i -> i - 1).collect(Collectors.toList()));
    to.add(model.n * 2 - 2);

    List<List<Integer>> b = new ArrayList<>();
    for (int i = 0; i < model.n; i++) {
      List<Integer> bi = new ArrayList<>();
      for (int j = from.get(i); j < to.get(i); j++) {
        bi.add(solutionContainer.positions.get(j));
      }
      if (!bi.isEmpty()) {
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
