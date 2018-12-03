package androide.herlich.your.androideeegsa;

import java.util.ArrayList;

public class arregloPostes {

    private ArrayList<poste> grid = new ArrayList<>();

    public arregloPostes() {
        cargaThread();
    }

    public poste getPoste(String p) {
        poste ret = new poste();
        ret.setPoste("0");
        ret.setX("0");
        ret.setY("0");
        int i = 0;
        while (i < grid.size()) {
            if (grid.get(i).getPoste().equals(p)) {
                ret.setPoste(grid.get(i).getPoste());
                ret.setX(grid.get(i).getX());
                ret.setY(grid.get(i).getY());
                return ret;
            }
            i++;
        }

        return ret;
    }

    private void cargaThread() {
        try {
            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    cargaArreglo();
                }
            });
            t1.start();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void cargaArreglo() {
        grid.add(new poste("115202", "-90.640469873", "14.7248394110001"));

    }
}