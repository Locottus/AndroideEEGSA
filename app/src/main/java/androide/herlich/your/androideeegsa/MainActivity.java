package androide.herlich.your.androideeegsa;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {


    //arregloPostes poles;
    LocationManager milocManager;
    MyLocationService milocListener;

    MapView mMapView;
    GraphicsLayer measures, mPostes;
    GraphicsLayer graphicsLayer;

    String PosteLabel;
    boolean demon = true;
    boolean refresher = false;
    boolean zoomer = false;
    boolean ruta = false;

    String Longitud = null;
    String Latitud = null;
    int MapID = 0;

    boolean goGoogleMaps;

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void backgroundToast(final Context context, final String msg) {

        if (context != null && msg != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        milocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        milocListener = new MyLocationService();

        //poles = new arregloPostes();
        checkAccess();
        createGPS();
        demon = true;
        dThread();

        mMapView = new MapView(this);
        mMapView = findViewById(R.id.map);
        measures = new GraphicsLayer();
        mPostes = new GraphicsLayer();
        graphicsLayer = new GraphicsLayer();

        mMapView.addLayer(graphicsLayer);
        mMapView.addLayer(measures);
        mMapView.addLayer(mPostes);

        mMapView.enableWrapAround(true);
        mMapView.setAllowRotationByPinch(true);
        mMapView.setEsriLogoVisible(true);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get the map settings
        SharedPreferences settings = getSharedPreferences("mapPreference", 0);
        String mapState = settings.getString("mapstate", null);
        if (mapState != null) {
            mMapView.restoreState(mapState);
        }
    }

    private void createGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == 0)
            milocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, milocListener);
    }

    public poste fGetPole(String nPoste) {
        BufferedReader reader = null;
        poste p = new poste();
        p.setY("0");
        p.setX("0");
        p.setPoste("0");
        String[] archivos = {
                "xab.txt", "xac.txt", "xad.txt", "xae.txt", "xaf.txt"
        };

        try {
            for (int i = 0; i < archivos.length; i++) {
                reader = new BufferedReader(
                        new InputStreamReader(getAssets().open(archivos[i]), "UTF-8"));

                // do reading, usually loop until end of file reading
                String mLine;
                String[] tmp;
                while ((mLine = reader.readLine()) != null) {
                    //process line
                    tmp = mLine.split(",");
                    System.out.println(mLine);
                    if (tmp[0].equals(nPoste)) {
                        p.setPoste(tmp[0]);
                        p.setX(tmp[1]);
                        p.setY(tmp[2]);
                        return p;
                    }
                }
            }
        } catch (IOException e) {
            //log the exception
            System.out.println(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                    System.out.println(e.getMessage());
                }
            }
        }
        return p;
    }

    private void checkAccess() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.VIBRATE
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    private void gps_setting() {
        if (refresher) {
            refresher = false;
            zoomer = false;
            ToastMSG(getString(R.string.GpsOff));
        } else {
            refresher = true;
            zoomer = true;
            ToastMSG(getString(R.string.GpsOn));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.MenuGPS:
                gps_setting();
                return true;
            case R.id.MenuSalir:
                SalirGis();
                return true;
            case R.id.MenuBuscar:
                goGoogleMaps = false;
                buscarPoste();
                return true;
            case R.id.Menuxy:
                mostrarXY();
                return true;
            case R.id.MenuGM:
                goGoogleMaps = true;
                buscarPoste();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //demo function for another release.
    private void googleMaps(String x, String y) {
        try {
            //https://developers.google.com/maps/documentation/urls/android-intents
            //-90.5473612,14.51742544 --> van latitud, longitud PosteLabel
            /*Uri gmmIntentUri = Uri.parse("geo:" + y + "," + x + "(" + PosteLabel + ")");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);*/
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("www.google.com").appendPath("maps").appendPath("dir").appendPath("").appendQueryParameter("api", "1")
                    .appendQueryParameter("destination", y + "," + x);
            String url = builder.build().toString();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
    }

    private void mostrarXY() {
        String msg = "(" + Longitud + "," + Latitud + ")";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.posicion));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        //m_Text = input.getText().toString();
        input.setText(msg);
        builder.setNegativeButton(getString(R.string.cerrar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void buscarPoste() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.poste));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.acepatar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PosteLabel = input.getText().toString();
                if (PosteLabel.length() > 1) {
                    refresher = false;
                    ToastMSG(getString(R.string.GpsOff));
                    MapID = mMapView.getSpatialReference().getID();
                    buscaThread();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancelar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void buscaThread() {
        try {
            final Context context = getApplicationContext();
            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    backgroundToast(context, getString(R.string.buscando));
                    poste p = new poste();
                    p = fGetPole(PosteLabel);
                    if (!p.getPoste().equals("0"))
                        localizarPoste(p.getX(), p.getY());
                    else
                        backgroundToast(context, getString(R.string.noEncontrado));

                }
            });
            t1.start();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void localizarPoste(String x, String y) {
        Point mapPoint = GeometryEngine.project(Double.parseDouble(x), Double.parseDouble(y),
                SpatialReference.create(mMapView.getSpatialReference().getID()));
        try {
            mPostes.addGraphic(new Graphic(mapPoint, new SimpleMarkerSymbol(Color.BLUE, 20, SimpleMarkerSymbol.STYLE.DIAMOND)));
            mMapView.zoomToScale(mapPoint, 1904.357886);
            mMapView.centerAt(mapPoint, true);

            if (goGoogleMaps)
                googleMaps(x, y);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        if (ruta) {
            ruta = false;
            rutaPoste(x, y);
        }
    }


    public void SalirGis() {
        YesNoBox(getString(R.string.salir), getString(R.string.confirmar));
    }

    public void YesNoBox(String Msg, String Title) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set title
        alertDialogBuilder.setTitle(Title);

        alertDialogBuilder
                .setMessage(Msg)
                //.setCancelable(true)

                .setCancelable(false)
                .setPositiveButton(getString(R.string.si), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        demon = false;
                        System.exit(0);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                })
        ;
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    private void dThread() {

        try {
            Thread t1 = new Thread(new Runnable() {
                public void run() {

                    while (demon) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (refresher) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    //Your UI code here
                                    //ToastMSG("INSIDE background process running!!");
                                    Latitud = String.valueOf(milocListener.latitud);
                                    Longitud = String.valueOf(milocListener.longitud);
                                    tracking();
                                }
                            });
                        }
                    }

                }
            });
            t1.start();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void rutaPoste(String Postex, String Postey) {
        String uri = "geo:0,0?q=" + Postey + "," + Postex;
        startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
    }

    public void tracking() {
        {
            Point mapPoint = GeometryEngine.project(Double.parseDouble(Longitud), Double.parseDouble(Latitud), SpatialReference.create(mMapView.getSpatialReference().getID()));

            try {
                //borramos todos los puntos anteriores
                int[] arr = measures.getGraphicIDs();
                for (int i = 0; i < arr.length; i++)
                    measures.removeGraphic(arr[i]);

            } catch (Exception ex) {
                //MessageBox(ex.getMessage(),"err");
                System.out.println(ex.getMessage());
            }

            try {
                measures.addGraphic(new Graphic(mapPoint, new SimpleMarkerSymbol(Color.BLACK, 15, SimpleMarkerSymbol.STYLE.CROSS)));
                if (zoomer) {
                    //mMapView.zoomTo(mapPoint,1904);
                    mMapView.zoomToScale(mapPoint, 1904.357886);
                    zoomer = false;
                    //ToastMSG(String.valueOf(mMapView.getScale()) + " map Scale");
                }
                mMapView.centerAt(mapPoint, true);
            } catch (Exception ex) {
                //MessageBox(ex.getMessage(),"err");
                System.out.println(ex.getMessage());
            }

        }
    }

    public void ToastMSG(String msg) {
        Context context = getApplicationContext();
        CharSequence text = msg;
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        demon = false;
        refresher = false;
        milocManager.removeUpdates(milocListener);
        saveMapPrefs();
        super.onDestroy();

    }


    @Override
    protected void onPause() {
        super.onPause();
        refresher = false;
        mMapView.pause();
        saveMapPrefs();
        milocManager.removeUpdates(milocListener);
    }

    private void saveMapPrefs() {
        SharedPreferences settings = getSharedPreferences("mapPreference", 0);
        SharedPreferences.Editor ed = settings.edit();
        ed.putString("mapstate", mMapView.retainState());
        ed.putBoolean("refresher", refresher);
        ed.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //refresher = true;
        mMapView.unpause();
        milocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        milocListener = new MyLocationService();
        createGPS();

    }

/*
    public class AsyncQueryTask extends AsyncTask<String, Void, FeatureSet> {
        public FeatureResult results = null;


        @Override
        protected void onPostExecute(FeatureSet r) {
            String Postex = null;
            String Postey = null;
            try {
                // mMapView.removeAll();

                // Define a new marker symbol for the result graphics
                SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.RED, 15, SimpleMarkerSymbol.STYLE.CIRCLE);
                // Envelope to focus on the map extent on the results
                Envelope extent = new Envelope();

                // iterate through results
                for (Object element : results) {
                    // if object is feature cast to feature
                    if (element instanceof Feature) {
                        Feature feature = (Feature) element;
                        // convert feature to graphic
                        Graphic graphic = new Graphic(feature.getGeometry(), sms, feature.getAttributes());
                        // merge extent with point
                        extent.merge((Point) graphic.getGeometry());
                        // add it to the layer
                        graphicsLayer.addGraphic(new Graphic(graphic.getGeometry(), sms));
                        Postex = String.valueOf(((Point) graphic.getGeometry()).getX());
                        Postey = String.valueOf(((Point) graphic.getGeometry()).getY());
                        mMapView.zoomToScale((Point) graphic.getGeometry(), 1904.357886);

                    }
                }
                // Set the map extent to the envelope containing the result graphics
                mMapView.setExtent(extent, 100);


            } catch (Exception ex) {
            }

            String msg = "Elementos encontrados: " + String.valueOf(results.featureCount());
            Context context = getApplicationContext();
            CharSequence text = msg;
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            if (ruta) {
                ruta = false;
                rutaPoste(Postex, Postey);
            }

        }

        @Override
        protected FeatureSet doInBackground(String... queryParams) {
            if (queryParams == null || queryParams.length <= 1)
                return null;

            try {
                String url = queryParams[0];
                Query query = new Query();
                query.setOutFields(new String[]{"*"});
                String whereClause = queryParams[1];
                SpatialReference sr = SpatialReference.create(MapID);
                query.setOutSpatialReference(sr);
                query.setReturnGeometry(true);
                query.setWhere(whereClause);

                QueryTask qTask = new QueryTask(url);
                QueryParameters qParameters = new QueryParameters();
                qParameters.setOutSpatialReference(sr);
                qParameters.setReturnGeometry(true);
                qParameters.setWhere(whereClause);
                try {
                    results = qTask.execute(qParameters);

                    //return results;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
    */

}