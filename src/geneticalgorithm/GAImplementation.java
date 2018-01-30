package geneticalgorithm;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GAImplementation {

    public void launch(Model model) {
        int nVar = 2 * model.n - 1;
        int maxIt = 1000;
        int nPop = 40;
        float pc = 0.4f;
        int nc = 2 * Math.round(pc * nPop / 2);
        float pm = 0.8f;
        int nm = Math.round(pm * nPop);

        //Init
        Random random = new Random();

        for (int j = 0; j < nPop; j++) {
            SolutionContainer solutionContainer = new SolutionContainer();
            solutionContainer.positions = IntStream.range(0, nVar).toArray();
            for (int i = 0; i < nVar; i++) {
                int randomPosition = random.nextInt(nVar);
                int temp = solutionContainer.positions[i];
                solutionContainer.positions[i] = solutionContainer.positions[randomPosition];
                solutionContainer.positions[randomPosition] = temp;
            }
            computeBinPackingCost(solutionContainer, model);
        }

        Comparator<SolutionContainer> solutionContainerComparator = Comparator.comparingDouble(s -> s.cost);
        solutionContainers.sort(solutionContainerComparator);

        SolutionContainer bestSol = solutionContainers.get(0);

        List<Double> bestCost = new ArrayList<>(maxIt);

        for (int i = 0; i < maxIt; i++) {

            //CrossOver
            List<SolutionContainer> crossOverSolutions = new ArrayList<>();
            for (int k = 0; k < nc / 2; k++) {
                int i1 = random.nextInt(nPop);
                SolutionContainer solution1 = solutionContainers.get(i1);

                int i2 = random.nextInt(nPop);
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
                int parentIndex = random.nextInt(nPop);

                //Select Parent
                SolutionContainer parent = solutionContainers.get(parentIndex);

                //Apply Mutation
                permutationMutate(mutationSolutions, parent.positions);

                //Evaluate Mutant
                int index = mutationSolutions.size() - 1;
                computeBinPackingCost(mutationSolutions.get(index), model);
            }

            // Merge Population
            solutionContainers = Stream.of(solutionContainers, crossOverSolutions, mutationSolutions)
                    .flatMap(List::stream)
                    .sorted(solutionContainerComparator)
                    .limit(nPop)
                    .collect(Collectors.toList());

            // Update Best Solution Ever Found
            bestSol = solutionContainers.get(0);

            // Update Best Cost Ever Found
            bestCost.add(bestSol.cost);

            System.out.println("Iteration " + i + " : Best Cost = " + bestSol.cost + " elements count = " + bestSol.solution.b.stream().flatMap(List::stream).count());
        }

        bestSol.solution.b.stream().map(aB -> aB.stream().mapToInt(j -> model.v[j]).sum())
                .forEach(System.out::println);
    }

    private void permutationMutate(List<SolutionContainer> mutationSolutions, int[] positions) {

        int mode = new Random().nextInt(3);
        SolutionContainer solutionContainer = new SolutionContainer();
        switch (mode) {
            case 0:
                //Swap
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
            default:
        }
        mutationSolutions.add(solutionContainer);
    }

    private int[] doInsertion(int[] positions) {

        Random random = new Random();
        int j1 = random.nextInt(positions.length);
        int j2 = random.nextInt(positions.length);
        while (j2 == j1) {
            j2 = random.nextInt(positions.length);
        }
        int i1 = Math.min(j1, j2);
        int i2 = Math.max(j1, j2);

        int[] newPositions = new int[positions.length];
        System.arraycopy(positions, 0, newPositions, 0, i1);
        System.arraycopy(positions, i1 + 1, newPositions, i1, i2 - i1);
        newPositions[i2] = positions[i1];
        System.arraycopy(positions, i2, newPositions, i2, positions.length - i2);

        return newPositions;
    }

    private List<Integer> doReversion(int[] positions) {
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
        newPositions.addAll(IntStream.range(i2, positions.size()).mapToObj(i -> positions.get(i)).collect(Collectors.toList()));
        return newPositions;
    }

    private int[] doSwap(int[] positions) {
        int[] newPositions = new int[positions.length];
        System.arraycopy(positions, 0, newPositions, 0, positions.length);
        Random random = new Random();
        Collections.swap(newPositions, random.nextInt(positions.size()), random.nextInt(positions.size()));
        return newPositions;
    }

    private void permuationCrossOver(List<SolutionContainer> crossOverSolutions, int[] positions1, int[] positions2) {

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

    public void computeBinPackingCost(SolutionContainer solutionContainer, Model model) {
        List<Integer> sep = new ArrayList<>();
        for (int i = 0; i < solutionContainer.positions.length; i++) {
            if (solutionContainer.positions[i] >= model.n) {
                sep.add(i);
            }
        }

        List<Integer> from = new ArrayList<>();
        from.add(0);
        from.addAll(sep.stream().map(i -> i + 1).collect(Collectors.toList()));

        List<Integer> to = new ArrayList<>();
        to.addAll(sep.stream().map(i -> i - 1).collect(Collectors.toList()));
        to.add(model.n * 2 - 1);

        List<List<Integer>> b = new ArrayList<>();
        for (int i = 0; i < model.n; i++) {
            List<Integer> bi = new ArrayList<>();
            for (int j = from.get(i); j <= to.get(i); j++) {
                if (j >= solutionContainer.positions.length) {
                    continue;
                }
                bi.add(solutionContainer.positions[j]);
            }
            if (!bi.isEmpty()) {
                b.add(bi);
            }
        }

        List<Float> viol = b.stream()
                .map(aB -> aB.stream().mapToInt(j -> model.v[j]).sum())
                .map(vi -> Math.max(((float) vi / (float) model.vMax) - 1, 0))
                .collect(Collectors.toList());

        solutionContainer.solution.b = b;
        viol.stream()
                .mapToDouble(Float::doubleValue)
                .average()
                .ifPresent(d -> solutionContainer.solution.meanViol = d);
        solutionContainer.solution.nBin = b.size();
        solutionContainer.solution.viol = viol;

        solutionContainer.cost = b.size() + 10 * model.n * solutionContainer.solution.meanViol;
    }
}
