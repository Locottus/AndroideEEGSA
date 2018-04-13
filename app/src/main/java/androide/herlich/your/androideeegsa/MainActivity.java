package androide.herlich.your.androideeegsa;
//Herlich Steven Gonzalez Zambrano 2017
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.BoolRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.query.QueryTask;

public class MainActivity extends AppCompatActivity {

    LocationManager milocManager;
    MyLocationService milocListener;

    MapView mMapView;
    GraphicsLayer measures;
    String activos = "http://200.35.168.116/giscorp/rest/services/Movilidad/ActivosRedMobil/MapServer";
                   // http://200.35.168.116/giscorp/rest/services/EEGSA/CLIENTES/MapServer/0
    //String activos = "http://200.35.168.116/giscorp/rest/services/EEGSA/POSTES_EEGSA/MapServer/";
    GraphicsLayer graphicsLayer;

    String PosteLabel;
    int PosteRuta;
    boolean boolQuery = true;
    boolean demon = true;
    boolean refresher = false;
    boolean zoomer = false;
    boolean ruta = false;

    String Longitud = null;
    String Latitud = null;
    String Altitud = null;
    int MapID = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        milocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        milocListener = new MyLocationService();
        milocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, milocListener);
        demon = true;
        demonio();

        mMapView = new MapView(this);
        mMapView = (MapView) findViewById(R.id.map);
        measures = new GraphicsLayer();
        graphicsLayer = new GraphicsLayer();


        mMapView.addLayer(new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"));
        mMapView.addLayer(new ArcGISDynamicMapServiceLayer(activos));
        mMapView.addLayer(graphicsLayer);
        mMapView.addLayer(measures);


        mMapView.enableWrapAround(true);
        mMapView.setAllowRotationByPinch(true);
        mMapView.setEsriLogoVisible(true);


        // get the map settings
        SharedPreferences settings = getSharedPreferences("mapPreference", 0);
        String mapState = settings.getString("mapstate", null);
        if (mapState != null) {
            mMapView.restoreState(mapState);
        }
    }

    private void gps_setting() {
        if (refresher) {
            refresher = false;
            zoomer = false;
            ToastMSG("rastreador GPS apagado.");
        } else {
            refresher = true;
            zoomer = true;
            ToastMSG("rastreador GPS encendido.");
        }
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
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
                //buscarPoste();
                ToastMSG("This option no longer works. service disabled by host.");
                return true;
                case R.id.Menuxy:
                mostrarXY();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void mostrarXY() {
        String msg = "(" + Longitud + "," + Latitud + ")";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Coordenadas actuales (Longitud,Latitud)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        //m_Text = input.getText().toString();
        input.setText(msg);
        builder.setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void buscarPoste() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Numero de Poste ");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PosteLabel = input.getText().toString();
                    if (PosteLabel.length() > 1) {
                        refresher = false;
                        ToastMSG("rastreador GPS apagado.");
                        MapID = mMapView.getSpatialReference().getID();
                        //buscamos el poste
                        String[] queryParams = {activos + "/0", " POSTE =  " + PosteLabel};
                        AsyncQueryTask ayncQuery = new AsyncQueryTask();
                        ayncQuery.execute(queryParams);
                    }
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }catch(Exception ex)
        {
            ToastMSG("hubo un error al consultar el servicio.");
        }
    }

    public void SalirGis() {
        YesNoBox("Salir de Aplicacion?", "Confirmar Operacion");
    }

    public void YesNoBox(String Msg, String Title) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set title
        alertDialogBuilder.setTitle(Title);

        alertDialogBuilder
                .setMessage(Msg)
                //.setCancelable(true)

                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        demon = false;
                        System.exit(0);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
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

    private void demonio() {

        try {
            Thread t1 = new Thread(new Runnable() {
                public void run() {

                    while (demon) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (refresher ) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    //Your UI code here
                                    //ToastMSG("INSIDE background process running!!");
                                    Latitud = String.valueOf(milocListener.latitud);
                                    Longitud = String.valueOf(milocListener.longitud);
                                    Altitud = String.valueOf(milocListener.altitud);
                                    tracking();
                                }
                            });
                        }
                    }

                }
            });
            t1.start();
        } catch (Exception ex)
        {
        }
    }

    public void rutaPoste(String Postex, String Postey) {
        String uri = "geo:0,0?q=" + Postey + "," + Postex;
        startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
    }

    public void tracking() {
        {
            Point mapPoint = (Point) GeometryEngine.project(Double.parseDouble(Longitud), Double.parseDouble(Latitud), SpatialReference.create(mMapView.getSpatialReference().getID()));

            try {
                //borramos todos los puntos anteriores
                int[] arr = measures.getGraphicIDs();
                for (int i = 0; i < arr.length; i++)
                    measures.removeGraphic(arr[i]);

            } catch (Exception ex) {
                //MessageBox(ex.getMessage(),"err");
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
        super.onDestroy();

    }


    @Override
    protected void onPause() {
        super.onPause();
        refresher = false;
        mMapView.pause();

        SharedPreferences settings = getSharedPreferences("mapPreference", 0);
        SharedPreferences.Editor ed = settings.edit();
        ed.putString("mapstate", mMapView.retainState());
        ed.putBoolean("refresher",refresher);
        ed.apply();

        milocManager.removeUpdates(milocListener);

        //ToastMSG("Tracker detenido");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //refresher = true;
        mMapView.unpause();
        milocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        milocListener = new MyLocationService();
        milocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, milocListener);

    }


    public class AsyncQueryTask extends AsyncTask<String, Void, FeatureSet> {
        public FeatureResult results = null;


        @Override
        protected void onPostExecute(FeatureSet r)
        {
            String Postex = null;
            String Postey = null;
            try{
               // mMapView.removeAll();

                // Define a new marker symbol for the result graphics
                SimpleMarkerSymbol sms =  new SimpleMarkerSymbol(Color.RED, 15, SimpleMarkerSymbol.STYLE.CIRCLE);
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
                        extent.merge((Point)graphic.getGeometry());
                        // add it to the layer
                        graphicsLayer.addGraphic(new Graphic(graphic.getGeometry(), sms));
                        Postex = String.valueOf(((Point) graphic.getGeometry()).getX());
                        Postey = String.valueOf(((Point) graphic.getGeometry()).getY());
                        mMapView.zoomToScale((Point) graphic.getGeometry() , 1904.357886);

                    }
                }
                // Set the map extent to the envelope containing the result graphics
                mMapView.setExtent(extent, 100);


            }catch(Exception ex){}

            String msg = "Elementos encontrados: " + String.valueOf(results.featureCount());
            Context context = getApplicationContext();
            CharSequence text = msg;
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            if (ruta)
            {
                ruta = false;
                rutaPoste(Postex, Postey);
            }

        }

        @Override
        protected FeatureSet doInBackground(String... queryParams)
        {
            if (queryParams == null || queryParams.length <= 1)
                return null;

            try {
                String url = queryParams[0];
                Query query = new Query();
                query.setOutFields(new String[] { "*" });
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
}