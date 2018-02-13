package mygeneticalgorithm;

public class Boxes {

    double[] weights;
    double[] volumes;
    int boxCount;

    public Boxes(int boxCount) {
        this.boxCount = boxCount;
        this.weights = new double[boxCount];
        this.volumes = new double[boxCount];
    }

    void addBox(int index, double weight, double volume) {
        weights[index] = weight;
        volumes[index] = volume;
    }

}
