package mygeneticalgorithm;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;

public class PlayerTest {

  @Test
  public void launch1() {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("test-01.txt");
    new Player().launch(new Scanner(inputStream));
  }

  @Test
  public void launch6() {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("test-06.txt");
    new Player().launch(new Scanner(inputStream));
  }

  @Test
  public void launch_Simple() {
    String input = "30\n" +
                   "6 6\n" +
                   " 3 3\n" +
                   " 4 4\n" +
                   " 6 6\n" +
                   " 8 8\n" +
                   " 7 7\n" +
                   " 4 4\n" +
                   " 7 7\n" +
                   " 7 7\n" +
                   " 5 5\n" +
                   " 5 5\n" +
                   " 6 6\n" +
                   " 7 7\n" +
                   " 7 7\n" +
                   " 6 6\n" +
                   " 4 4\n" +
                   " 8 8\n" +
                   " 7 7\n" +
                   " 8 8\n" +
                   " 8 8\n" +
                   " 2 2\n" +
                   " 3 3\n" +
                   " 4 4\n" +
                   " 5 5\n" +
                   " 6 6\n" +
                   " 5 5\n" +
                   " 5 5\n" +
                   " 7 7\n" +
                   " 7 7\n" +
                   " 12 12";

    Player.TRUCK_COUNT = 7;
    Player.TRUCK_VOLUME = 30;
    Player player = new Player();
    player.launch(new Scanner(new ByteArrayInputStream(input.getBytes())));

  }

}