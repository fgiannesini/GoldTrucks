import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Player {

    private static final float PRECISION = 1f;
    private static int TRUCK_COUNT = 100;
    private static int TRUCK_VOLUME = 100;

    public static void main(String args[]) {
        new Player().launch(new Scanner(System.in));
    }

    void launch(Scanner in) {
        List<Box> boxList = createBoxList(in);
        List<Truck> truckList = IntStream.range(0, TRUCK_COUNT).mapToObj(Truck::new).collect(Collectors.toList());
        double totalWeight = boxList.stream().mapToDouble(box -> box.weight).sum();
        float theoricalAverageWeight = (float) (totalWeight / (double) TRUCK_COUNT);

        Comparator<Box> boxComparator = Comparator.comparingDouble(box -> box.weight);
        boxList.sort(boxComparator.reversed());
        for (Truck truck : truckList) {
            System.err.println(truck.index);
            addBoxInTrucks(boxList, truck, theoricalAverageWeight, 0);
            System.err.println(truck.volume + " " + truck.weight + " " + truck.boxList.size());
        }

        DoubleSummaryStatistics truckWeightStatistics = truckList.stream().mapToDouble(t -> t.weight).summaryStatistics();
        System.err.println("theorical average : " + theoricalAverageWeight);
        System.err.println("init average : " + truckWeightStatistics.getAverage());
        System.err.println("init error : " + (truckWeightStatistics.getMax() - truckWeightStatistics.getMin()));

        String result = boxList.stream()
                .sorted(Comparator.comparingInt(box -> box.index))
                .map(box -> String.valueOf(box.truckIndex))
                .collect(Collectors.joining(" "));
        System.out.println(result);
    }

    private boolean addBoxInTrucks(List<Box> boxList, Truck truck, float theoricalAverageWeight, int boxIndex) {
        while (boxIndex < boxList.size()) {
            Box box = boxList.get(boxIndex);
            if (box.truckIndex != -1) {
                boxIndex++;
                continue;
            }

            boolean added = truck.addBox(box, theoricalAverageWeight);
            if (theoricalAverageWeight - truck.weight < PRECISION) {
                return true;
            }
            if (added) {
                boolean isFinished = addBoxInTrucks(boxList, truck, theoricalAverageWeight, boxIndex + 1);
                if (isFinished) {
                    return true;
                }
                truck.removeBox(box);
            }

            boxIndex++;
        }


        return false;
    }

    private void initBoxInTrucks(List<Box> boxList, List<Truck> truckList, float meanWeight) {
        Iterator<Truck> truckIterator = truckList.iterator();
        Truck truck = truckIterator.next();
        for (Box box : boxList) {
            while (!truck.addBox(box, meanWeight)) {
                if (!truckIterator.hasNext()) {
                    truckIterator = truckList.iterator();
                    meanWeight = Float.MAX_VALUE;
                }
                truck = truckIterator.next();
            }
        }
    }

    private List<Box> createBoxList(Scanner in) {
        int boxCount = in.nextInt();
        List<Box> boxList = new ArrayList<>(boxCount);
        for (int i = 0; i < boxCount; i++) {
            Box box = new Box();
            box.weight = in.nextFloat();
            box.volume = in.nextFloat();
            box.index = i;
            boxList.add(box);
        }
        return boxList;
    }

    class Box {
        int index;
        float weight;
        float volume;
        int truckIndex = -1;
    }

    class Truck {
        List<Box> boxList;
        float weight;
        float volume;
        int index;

        public Truck(int i) {
            this.boxList = new ArrayList<>(20);
            index = i;
        }

        public boolean canBeAdded(Box box, float limitWeight) {
            return (volume + box.volume) < TRUCK_VOLUME && (weight + box.weight) < (limitWeight + PRECISION);
        }

        public boolean addBox(Box box, float limitWeight) {
            if (!canBeAdded(box, limitWeight)) {
                return false;
            }
            volume += box.volume;
            weight += box.weight;
            boxList.add(box);
            box.truckIndex = index;
            return true;
        }

        public void removeBox(Box box) {
            volume -= box.volume;
            weight -= box.weight;
            boxList.remove(box);
            box.truckIndex = -1;
        }
    }
}