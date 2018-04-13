package your.herlich.Androide;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Editable;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.esri.android.map.event.OnStatusChangedListener;

//parte de intents y uris
import android.content.Intent;
import android.net.Uri;


public class EEGSAActivity extends Activity {
	MapView mMapView;
	GraphicsLayer measures;
	String activos = "http://200.35.168.116/giscorp/rest/services/Movilidad/ActivosRedMobil/MapServer";
	//String activos = "http://servgisweb/giscorp/rest/services/Movilidad/ActivosRedMobil/MapServer";

	
	public static String Latitud;
	public static String Longitud;
	
	public static String Poste_Busqueda;
	
	int rastrea = 0;
	int existe = 0;
	int escala = 1;
	int zoomer = 0;
	

    final static int HAS_RESULTS = 1;
    final static int NO_RESULT = 2;
    final static int CLEAR_RESULT = 3;
	
	ProgressDialog progress;
	Button queryButton;
	Button TrackButton;
	GraphicsLayer graphicsLayer;
	boolean boolQuery = true;
	
  
    LocationManager milocManager;
    LocationListener milocListener;
    
    WakeLock mWakeLock;


    //double Postex, Postey;
    String PosteLabel;
    int PosteRuta;
    
    
    
	public class MiLocationListener implements LocationListener
	{
	
		public void onLocationChanged(Location loc)
	
		{
	
		Double lat = loc.getLatitude();
		Double lon = loc.getLongitude();
			
		Latitud = lat.toString(); 
		Longitud = lon.toString();

		 tracking();
		}
		
		public void onProviderDisabled(String provider)
		{
			//Toast.makeText( getApplicationContext(),"Gps Desactivado",Toast.LENGTH_SHORT ).show();
		}
		public void onProviderEnabled(String provider)
		{
			//Toast.makeText( getApplicationContext(),"Gps Activado",Toast.LENGTH_SHORT ).show();
		}
		public void onStatusChanged(String provider, int status, Bundle extras){}
		
	}
	
    public void YesNoBox(String Msg, String Title)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set title
        alertDialogBuilder.setTitle(Title);

        alertDialogBuilder
                .setMessage(Msg)
                //.setCancelable(true)

                .setCancelable(false)
                .setPositiveButton("Si",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                    	
                        System.exit(0);
                        
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
   
    		//invoke gps uncoment to enable it
    		milocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    		milocListener = new MiLocationListener();
    		milocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, milocListener);
    		
    		
        
        System.out.println("Desarrollado por Herlich Gonzalez 2014");
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK , "RunGisMovil");//.FULL_WAKE_LOCK
        
		mMapView = new MapView(this);
		mMapView = (MapView)findViewById(R.id.map);
		measures = new GraphicsLayer( );
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
		         if (mapState != null){
		        	 mMapView.restoreState(mapState);
		         }

		 
		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

	    private static final long serialVersionUID = 1L;

	            public void onStatusChanged(Object source, STATUS status) {
	                if (source == mMapView && status == STATUS.INITIALIZED) {
	                	
	                	
	                    SimpleRenderer sr = new SimpleRenderer( new SimpleFillSymbol(Color.RED));
	                    graphicsLayer.setRenderer(sr);
	                    
	                    boolean doQuery = false;
	                    for (Layer layer : mMapView.getLayers()) {
	                        if (layer instanceof ArcGISTiledMapServiceLayer) {
	                            ArcGISTiledMapServiceLayer tiledLayer = (ArcGISTiledMapServiceLayer) layer;

	                            if (tiledLayer.getUrl().equals(activos + "/0")) {
	                                doQuery = true;
	                                break;
	                            }
	                        }
	                    }
	                    if (!doQuery) {
	                        //Toast toast = Toast.makeText(AttributeQuery.this,    "URL for query does not exist any more",   Toast.LENGTH_LONG);
	                        //Toast.makeText( getApplicationContext(),"URL for query does not exist any more",Toast.LENGTH_LONG).show();
	                        //toast.show();
	                    } else {
	                        queryButton.setEnabled(true);
	                    }
	                	
	                }
	            }
	        });	
		
	
		//Toast.makeText( getApplicationContext(),"Desarrollado por Herlich Gonzalez 2014",Toast.LENGTH_LONG).show();

        queryButton = (Button) findViewById(R.id.button3);
        queryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	PosteRuta = 0;
            	buscarPoste();
            }
        });
        

        TrackButton = (Button) findViewById(R.id.button5);
        TrackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	PosteRuta = 1;
            	buscarPoste();
            }
        });

        
        
 
   //esto bloquea la orientacion del aparato
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            //setContentView(R.layout.portraitStart);
        	setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else
        {
            //setContentView(R.layout.landscapeStart);
        	setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    
    }


    
  public void XYPUT(View view){
	  AlertDialog.Builder alert = new AlertDialog.Builder(this);

	 alert.setTitle("buscar coordenads (x,y) ");
	 alert.setMessage("Ejemplo Ingreso -90.734289,14.56012");

		 // Set an EditText view to get user input 
	 final EditText input = new EditText(this);
	 alert.setView(input);
		 
	 alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	 public void onClick(DialogInterface dialog, int whichButton) {
     Editable value = input.getText();
     String[] x = value.toString().split(",");
		   
	   try {
			//busca_poste(value.toString());
		   Point mapPoint = (Point) GeometryEngine.project(Double.parseDouble(x[0]) , Double.parseDouble(x[1]) , SpatialReference.create(mMapView.getSpatialReference().getID()));
		   measures.addGraphic(new Graphic(mapPoint,new SimpleMarkerSymbol(Color.BLACK,15,STYLE.DIAMOND)));
		   mMapView.zoomTo(mapPoint, 5);
		   mMapView.zoomToScale(mapPoint,5);
		   mMapView.centerAt(mapPoint, true);   
			   
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageBox("Error en Coordenadas Ingresadas","No fue posible generar el punto");
		}
		   
		   }
		 });
		 
		 alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	   public void onClick(DialogInterface dialog, int whichButton) {
		     // Canceled.
		   }
		 });
		 alert.show();
    	
    }

  
  public  void rutaPoste(String Postex, String Postey)
  {
	  String uri = "geo:0,0?q=" + Postey + "," + Postex ; 
      startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
  }
    
    public void buscarPoste()
    {
   	 	AlertDialog.Builder alert = new AlertDialog.Builder(this);

		 alert.setTitle("Buscar Poste");
		 alert.setMessage("Ingrese Numero a Buscar");

		 // Set an EditText view to get user input 
		 final EditText input = new EditText(this);
		 alert.setView(input);

		 
		 
		 alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		 public void onClick(DialogInterface dialog, int whichButton) {
		   Editable value = input.getText();
		   
		   //MessageBox(value.toString(),"valor poste");
		   try {
			//busca_poste(value.toString());
			   
			   PosteLabel = value.toString();	   
			   
			  if (boolQuery) {
	               String[] queryParams = { activos + "/0", " POSTE =  " + PosteLabel  };
	               AsyncQueryTask ayncQuery = new AsyncQueryTask();
	               ayncQuery.execute(queryParams);
          } else {
              graphicsLayer.removeAll();
              boolQuery = true;
              //queryButton.setText("Buscar");

          }
      		   
			   
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   
		   }
		 });

			 alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int whichButton) {
		     // Canceled.
		   }
		 });

		 alert.show();
		 // see http://androidsnippets.com/prompt-user-input-with-an-alertdialog
	
    }

    

	@Override
	protected void onDestroy() {
		super.onDestroy();
 }
	
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
		milocManager.removeUpdates(milocListener);
		mWakeLock.release();  
		
	 SharedPreferences settings = getSharedPreferences("mapPreference", 0);
     SharedPreferences.Editor ed = settings.edit();
     ed.putString("mapstate", mMapView.retainState());
     ed.commit();
     
     milocManager.removeUpdates(milocListener);
     //mSensorManager.unregisterListener((SensorListener) this);
 }
	
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.unpause();
		milocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, milocListener);
		mWakeLock.acquire();
		

		milocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		milocListener = new MiLocationListener();
		milocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, milocListener);
		
		
	}
	
	
	public void SalirGis(View view){
		//this.finish();
		YesNoBox("Salir de Aplicacion?", "Confirmar Operacion");
	}
	
	public void tracking()
	{
		
		
		if (rastrea == 1)
		{

			try{
					   String cadena = "(" + Longitud + "," + Latitud + ")";
					   TextView texto = (TextView) findViewById(R.id.textXY);
					   texto.setText(cadena);

				}catch (Exception ex)
				{
					TextView texto = (TextView) findViewById(R.id.textXY);
					texto.setText("(X,Y)");
				}


		  //Point mapPoint = (Point) GeometryEngine.project(Double.parseDouble(Longitud) , Double.parseDouble(Latitud) , SpatialReference.create(4326));
			Point mapPoint = (Point) GeometryEngine.project(Double.parseDouble(Longitud) , Double.parseDouble(Latitud) , SpatialReference.create(mMapView.getSpatialReference().getID()));

		  try{
		  //borramos todos los puntos anteriores
			  int[] arr = measures.getGraphicIDs();
			  for (int i = 0; i < arr.length; i++)
					  measures.removeGraphic(arr[i]);
			  
		  }catch (Exception ex)
		  {
			  //MessageBox(ex.getMessage(),"err");
		  }
		  
		  try{
			  measures.addGraphic(new Graphic(mapPoint,new SimpleMarkerSymbol(Color.BLUE,15,STYLE.DIAMOND)));
				if (zoomer == 0)
				{
					mMapView.zoomTo(mapPoint, 5);
					mMapView.zoomToScale(mapPoint,5);
					zoomer++;
				}
				mMapView.centerAt(mapPoint, true);
		  }catch (Exception ex)
		  {
			  //MessageBox(ex.getMessage(),"err");
		  }

		}
	}
	
	
	private class AsyncQueryTask extends AsyncTask<String, Void, FeatureSet> {

        protected void onPreExecute() {
            progress = ProgressDialog.show(EEGSAActivity.this, "",
                    "Un momento.... Ejecutando Busqueda");

        }

        /**
         * First member in parameter array is the query URL; second member is
         * the where clause.
         */
        protected FeatureSet doInBackground(String... queryParams) {
            if (queryParams == null || queryParams.length <= 1)
                return null;
            FeatureSet featureSet = null;
            try {
	            String url = queryParams[0];
	            Query query = new Query();
	            query.setOutFields(new String[] { "*" });
	            String whereClause = queryParams[1];
	            SpatialReference sr = SpatialReference.create(mMapView.getSpatialReference().getID());
	            //query.setGeometry(new Envelope(-92.29624215880666,14.063619062195825,  -89.47420140589195, 15.703067695212889));
	            query.setOutSpatialReference(sr);
	            query.setReturnGeometry(true);
	            query.setWhere(whereClause);
	
	            QueryTask qTask = new QueryTask(url);
                featureSet = qTask.execute(query);
                
            } catch (Exception e) {
                e.printStackTrace();
                return featureSet;
            }
            return featureSet;

        }

        protected void onPostExecute(FeatureSet result) {

            String message = "No hay resultados";
            String X = null;
            String Y = null;
            Point p = null;
            if (result != null) {
            	
            	//desactivo gps para hacer busqueda
            	rastrea = 0;
            	Button Boton = (Button) findViewById(R.id.button2);
            	zoomer = 0;
    			Boton.setBackgroundColor(Color.RED);
            	
            	//marco el punto
                Graphic[] grs = result.getGraphics();

                
                if (grs.length > 0) {
                    graphicsLayer.addGraphics(grs);
                    //message = (grs.length == 1 ? "1 resultado " : Integer.toString(grs.length) );
                    SimpleMarkerSymbol sms = new SimpleMarkerSymbol(Color.RED, 15, SimpleMarkerSymbol.STYLE.CIRCLE);
                    for (Graphic gr : grs)
                    {
                        Graphic g = new Graphic(gr.getGeometry(), sms);
                        
                        graphicsLayer.addGraphic(g);
                        p = (Point) gr.getGeometry();
                        
                        mMapView.zoomTo(p, 5);
        				mMapView.zoomToScale(p,5);
        				
                    	X = (String) gr.getAttributeValue("X").toString(); 
                        Y = (String) gr.getAttributeValue("Y").toString();
                        message = PosteLabel + " (" + X + "," + Y + ")";
                    }
                    
                    //hide android keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    
                }
            }
            progress.dismiss();

            Toast toast = Toast.makeText(EEGSAActivity.this,message, Toast.LENGTH_LONG);
            toast.show();
            
			if ((PosteRuta == 1)&& (p != null ))// esto invoca servicios de rastreo
					rutaPoste(X,Y);
           
        }
    }


 
		
	public void cambia_Track()
	{
		Button Boton = (Button) findViewById(R.id.button2);
		if (rastrea == 0)
		{
			rastrea = 1;
			//MessageBox("Proceso de Rastreo Iniciado","Tracking");
			Toast.makeText( getApplicationContext(),"Tracking Activado",Toast.LENGTH_LONG).show();
        	//String x = texto.getText().toString();
            Boton.setBackgroundColor(Color.GREEN);
			
		}
		else
		{
			rastrea = 0;
			//MessageBox("Proceso de Rastreo Suspendido","Tracking");
			Toast.makeText( getApplicationContext(),"Tracking Desactivado",Toast.LENGTH_LONG ).show();
			zoomer = 0;
			Boton.setBackgroundColor(Color.RED);
		}
	}
	

	
	public void GetMyXY(View view)
	{
		cambia_Track();
	}


	 public void MessageBox(String Msg, String Title) {
         //myActivity.this.mEdit.getText().toString()
         AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
         dlgAlert.setMessage(Msg);
         dlgAlert.setTitle(Title);
         dlgAlert.setPositiveButton("Aceptar", null);
         dlgAlert.setCancelable(true);
         dlgAlert.create().show();
     }
	
	
	
}	
	
	
	
	
	
	
	
	
	
	
	
	
	
