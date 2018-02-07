package particleswarmoptimization;

import java.util.Arrays;
import java.util.Random;

public class Mutator {
  private final Random random;

  public Mutator(Random random) {
    this.random = random;
  }

  public double[] mutate(double[] positions) {
    int n = positions.length;
    double[] newPositions = Arrays.copyOf(positions, n);
    int i = random.nextInt(n);
    int j = random.nextInt(n);
    while(i==j) {
      j = random.nextInt(n);
    }
    double temp =newPositions[i];
    newPositions[i] = newPositions[j];
    newPositions[j] = temp;
    return newPositions;
  }
}
