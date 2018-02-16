package mygeneticalgorithm;

import org.junit.Test;

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

}