package androide.herlich.your.androideeegsa;

//model class poste.
public class poste {

    private String poste;
    private String x;
    private String y;

    public poste() {
    }

    public poste(String poste, String x, String y) {
        this.poste = poste;
        this.x = x;
        this.y = y;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
