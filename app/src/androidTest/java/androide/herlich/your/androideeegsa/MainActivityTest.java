package androide.herlich.your.androideeegsa;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MainActivityTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void checkPole(){
        MainActivity m = new MainActivity();
        poste p = new poste();
        p = m.fGetPole("272135");
        assertEquals(p.getPoste(),"272135");
    }

    @After
    public void tearDown() throws Exception {
    }
}