package geneticalgorithm;

import org.junit.Test;

public class GAImplementationTest {

    @Test
    public void launch() {
        Model model = new Model();
        model.v = new int[]{6, 3, 4, 6, 8, 7, 4, 7, 7, 5, 5, 6, 7, 7, 6, 4, 8, 7, 8, 8, 2, 3, 4, 5, 6, 5, 5, 7, 7, 12};
        model.n = model.v.length;
        model.vMax = 30;
        new GAImplementation().launch(model);
    }
}