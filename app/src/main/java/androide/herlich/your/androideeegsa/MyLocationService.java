package androide.herlich.your.androideeegsa;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Herlich on 8/26/2017.
 */

public class MyLocationService implements android.location.LocationListener{

    public  double latitud;
    public  double longitud;
    public  float velocidad;
    public  double altitud;
    public  long tiempo;

    @Override
    public void onLocationChanged(Location location) {

        longitud =location.getLongitude();
        latitud  = location.getLatitude();
        velocidad = location.getSpeed();
        altitud = location.getAltitude();
        tiempo = location.getTime();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
