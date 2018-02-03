import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Player {

  private static final float PRECISION = 1f;
  private static final long COMPUTATION_TIME = 10;
  static int TRUCK_COUNT = 100;
  private static int TRUCK_VOLUME = 100;

  public static void main(String args[]) {
    new Player().launch(new Scanner(System.in));
  }

  void launch(Scanner in) {

    long startTime = System.nanoTime();

    int populationCount = 40;
    float crossOverRate = 0.4f;
    int crossOverPopulationCount = Math.round(crossOverRate * populationCount);
    float mutationRate = 0.8f;
    int mutationPopulationCount = Math.round(mutationRate * populationCount);

    List<Box> boxList = createBoxList(in);
    float meanWeight = (float)boxList.stream().mapToDouble(b -> b.weight).sum() / (float)TRUCK_COUNT;

    List<Container> containerList = new ArrayList<>(populationCount);
    for (int i = 0; i < populationCount; i++) {
      Container container = new Container(boxList);
      computeCost(container, meanWeight);
      containerList.add(container);
    }

    Random random = new Random();
    Container bestSol;

    int ite = 0;
    while (true) {

      //CrossOverComputer
      List<Container> crossOverSolutions = new ArrayList<>(crossOverPopulationCount);
      for (int k = 0; k < crossOverPopulationCount / 2; k++) {
        int i1 = random.nextInt(populationCount);
        Container solution1 = containerList.get(i1);

        int i2 = i1;
        while (i1 == i2) {
          i2 = random.nextInt(populationCount);
        }
        Container solution2 = containerList.get(i2);

        permuationCrossOver(crossOverSolutions, random, solution1, solution2);
      }
      crossOverSolutions.forEach(c -> computeCost(c, meanWeight));

      //MutationComputer
      List<Container> mutationSolutions = new ArrayList<>(mutationPopulationCount);
      for (int k = 0; k < mutationPopulationCount; k++) {
        //Select Parent Index
        int parentIndex = random.nextInt(populationCount);

        //Select Parent
        Container parent = containerList.get(parentIndex);

        //Apply MutationComputer
        permutationMutate(mutationSolutions, parent, random);
      }

      mutationSolutions.forEach(c -> computeCost(c, meanWeight));

      // Merge Population
      containerList.addAll(crossOverSolutions);
      containerList.addAll(mutationSolutions);

      // Sort Population
      containerList.sort(Comparator.comparingDouble(c -> c.cost));

      // Truncate Extra Members
      containerList = new ArrayList<>(containerList.subList(0, populationCount));

      // Update Best Solution Ever Found
      bestSol = containerList.get(0);
      Double bestCost = bestSol.cost;

      long computationTime = System.nanoTime() - startTime;
      ite++;
      if (ite % 1000 == 0) {
        System.err.println("Iteration " + ite++ + ": ComputationTime = " + computationTime / 1_000_000d + " ms Best Cost = " + bestCost);
      }

      if (computationTime > (COMPUTATION_TIME - 1) * 1_000_000_000) {
        break;
      }
    }

    DoubleSummaryStatistics doubleSummaryStatistics = bestSol.truckList.stream().mapToDouble(t -> t.weight).summaryStatistics();
    System.err.println("Max : " + doubleSummaryStatistics.getMax() + " min : " + doubleSummaryStatistics.getMin());
    System.err.println(bestSol.truckList.stream().filter(t -> t.volume > TRUCK_VOLUME).map(t -> String.valueOf(t.index))
                         .collect(Collectors.joining(" ")));
    String result = bestSol.truckList.stream()
      .map(t -> t.boxList.stream().map(b -> new Result(b.index, t.index)).collect(Collectors.toList()))
      .flatMap(List::stream)
      .sorted(Comparator.comparingInt(r -> r.boxIndex))
      .map(r -> String.valueOf(r.truckIndex))
      .collect(Collectors.joining(" "));

    System.out.println(result);
  }

  void permutationMutate(List<Container> mutationSolutions, Container container, Random random) {

    int mode = random.nextInt(3);
    List<Box> newBoxList = null;
    switch (mode) {
      case 0:
        //Swap
        newBoxList = doSwap(container.boxList, random);
        break;
      case 1:
        // Reversion
        newBoxList = doReversion(container.boxList, random);
        break;
      case 2:
        // Insertion
        newBoxList = doInsertion(container.boxList, random);
        break;
      default:
    }
    Container newContainer = new Container(newBoxList);
    mutationSolutions.add(newContainer);
  }

  private List<Box> doInsertion(List<Box> boxList, Random random) {

    int size = boxList.size();
    int j1 = random.nextInt(size);
    int j2 = random.nextInt(size);
    while (j2 == j1) {
      j2 = random.nextInt(size);
    }
    int i1 = Math.min(j1, j2);
    int i2 = Math.max(j1, j2);

    List<Box> newBoxes = IntStream.range(0, i1)
      .mapToObj(i -> boxList.get(i))
      .collect(Collectors.toList());
    IntStream.range(i1 + 1, i2).forEach(i -> newBoxes.add(boxList.get(i)));
    newBoxes.add(boxList.get(i1));
    IntStream.range(i2, size).forEach(i -> newBoxes.add(boxList.get(i)));

    return newBoxes;
  }

  private List<Box> doReversion(List<Box> boxes, Random random) {
    int j1 = random.nextInt(boxes.size());
    int j2 = random.nextInt(boxes.size());
    int i1 = Math.min(j1, j2);
    int i2 = Math.max(j1, j2);

    List<Box> newBoxes = IntStream.range(0, i1).mapToObj(i -> boxes.get(i)).collect(Collectors.toList());
    List<Box> boxesToReverse = IntStream.range(i1, i2)
      .mapToObj(i -> boxes.get(i))
      .collect(Collectors.toList());
    Collections.reverse(boxesToReverse);
    newBoxes.addAll(boxesToReverse);
    newBoxes.addAll(IntStream.range(i2, boxes.size()).mapToObj(i -> boxes.get(i)).collect(Collectors.toList()));
    return newBoxes;
  }

  private List<Box> doSwap(List<Box> positions, Random random) {
    List<Box> newBoxes = new ArrayList<>(positions);
    Collections.swap(newBoxes, random.nextInt(positions.size()), random.nextInt(positions.size()));
    return newBoxes;
  }

  void permuationCrossOver(List<Container> crossOverSolutions, Random random, Container positions1, Container positions2) {

    List<Box> boxList1 = positions1.boxList;
    List<Box> boxList2 = positions2.boxList;
    int c = random.nextInt(boxList1.size() - 1);
    List<Box> x11 = IntStream.range(0, c).mapToObj(i -> boxList1.get(i)).collect(Collectors.toList());
    List<Box> x12 = IntStream.range(c, boxList1.size()).mapToObj(i -> boxList1.get(i)).collect(Collectors.toList());

    List<Box> x21 = IntStream.range(0, c).mapToObj(i -> boxList2.get(i)).collect(Collectors.toList());
    List<Box> x22 = IntStream.range(c, boxList2.size()).mapToObj(i -> boxList2.get(i)).collect(Collectors.toList());

    Set<Box> r1 = new HashSet<>(x11);
    r1.retainAll(x22);
    Set<Box> r2 = new HashSet<>(x12);
    r2.retainAll(x21);

    Iterator<Box> r1Iterator = r1.iterator();
    Iterator<Box> r2Iterator = r2.iterator();
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

    Container container1 = new Container();
    container1.boxList.addAll(x11);
    container1.boxList.addAll(x22);

    Container container2 = new Container();
    container2.boxList.addAll(x21);
    container2.boxList.addAll(x12);

    crossOverSolutions.add(container1);
    crossOverSolutions.add(container2);
  }

  private void computeCost(Container container, float meanWeight) {
    container.truckList = addBoxInTrucks(container.boxList, meanWeight);
    container.cost = container.truckList.stream().mapToDouble(t -> Math.abs(t.weight - meanWeight)).sum();
    container.cost += container.truckList.stream().mapToDouble(t -> Math.max(t.volume - TRUCK_VOLUME, 0) * 100).sum();
  }

  List<Truck> addBoxInTrucks(List<Box> boxList, float meanWeight) {
    List<Truck> truckList = IntStream.range(0, TRUCK_COUNT).mapToObj(Truck::new).collect(Collectors.toList());
    float volumeLimit = TRUCK_VOLUME;
    Iterator<Truck> truckIterator = truckList.iterator();
    Truck truck = truckIterator.next();
    for (Box box : boxList) {
      while (!truck.addBox(box, meanWeight, volumeLimit)) {
        if (!truckIterator.hasNext()) {
          truckIterator = truckList.iterator();
          meanWeight = Float.MAX_VALUE;
          volumeLimit = Float.MAX_VALUE;
        }
        truck = truckIterator.next();
      }
    }
    return truckList;
  }

  private List<Box> createBoxList(Scanner in) {
    int boxCount = in.nextInt();
    List<Box> boxList = new ArrayList<>(boxCount);
    for (int i = 0; i < boxCount; i++) {
      Box box = new Box(i, in.nextFloat(), in.nextFloat());
      boxList.add(box);
    }
    return boxList;
  }

  public static class Container {
    List<Truck> truckList;
    List<Box> boxList;
    double cost;

    public Container(List<Box> boxList) {
      this.boxList = boxList;
    }

    public Container() {
      this.boxList = new ArrayList<>();
    }
  }

  public static class Box {

    public Box(int index, float weight, float volume) {
      this.index = index;
      this.weight = weight;
      this.volume = volume;
    }

    int index;
    float weight;
    float volume;

  }

  public class Truck {
    List<Box> boxList;
    float weight;
    float volume;
    int index;

    public Truck(int i) {
      this.boxList = new ArrayList<>(20);
      index = i;
    }

    public boolean canBeAdded(Box box, float limitWeight, float limitVolume) {
      return (volume + box.volume) < limitVolume && (weight + box.weight) < (limitWeight + PRECISION);
    }

    public boolean addBox(Box box, float limitWeight, float limitVolume) {
      if (!canBeAdded(box, limitWeight, limitVolume)) {
        return false;
      }
      volume += box.volume;
      weight += box.weight;
      boxList.add(box);
      return true;
    }
  }

  class Result {
    public Result(int boxIndex, int truckIndex) {
      this.boxIndex = boxIndex;
      this.truckIndex = truckIndex;
    }

    int boxIndex;
    int truckIndex;
  }
}