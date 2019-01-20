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


    @Test
    public void checkX(){
        MainActivity m = new MainActivity();
        poste p = new poste();
        p = m.fGetPole("272135");
        assertEquals(p.getX(),"-90.59012594");
    }



    @Test
    public void checkY(){
        MainActivity m = new MainActivity();
        poste p = new poste();
        p = m.fGetPole("272135");
        assertEquals(p.getY(),"14.48396236");
    }


    @After
    public void tearDown() throws Exception {
    }
}