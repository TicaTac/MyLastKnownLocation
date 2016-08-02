package changed.location.com.mylastknownlocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    TextView currentWaetherTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        currentWaetherTV= (TextView) findViewById(R.id.TempTV) ;

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = "gps";
       // String provider = "network";

        // try to get the last known location with the given provider
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(provider);


        //ALWAYS CHECK IF NOT NULL- LAST KNOWN LOCATION CAN BE NULL
if(lastKnownLocation!= null) {
    String urlTodownload = "http://api.openweathermap.org/data/2.5/weather?lat=" + lastKnownLocation.getLatitude() + "&lon=" + lastKnownLocation.getLongitude() + "&appid=a69961c7031783d91d299c95e15920da";
    Log.d("URL", urlTodownload);
    DownloadWebsite downloadWebsite = new DownloadWebsite();
    downloadWebsite.execute(urlTodownload);
}



    }




    public  class DownloadWebsite extends AsyncTask<String, Integer, String >
    {

        @Override
        protected String doInBackground(String... params) {

            //start download....
            int lineConut=0;

            BufferedReader input = null;
            HttpURLConnection connection = null;
            StringBuilder response = new StringBuilder();
            try {
                //create a url:
                URL url = new URL(params[0]);
                //create a connection and open it:
                connection = (HttpURLConnection) url.openConnection();

                //status check:
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                    //connection not good - return.
                }

                //get a buffer reader to read the data stream as characters(letters)
                //in a buffered way.
                input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                //go over the input, line by line
                String line="";
                while ((line=input.readLine())!=null){
                    //append it to a StringBuilder to hold the
                    //resulting string
                    response.append(line+"\n");
                    lineConut++;

                    try {
                        //current thread - simulating long task
                        Thread.sleep(200);

                        publishProgress(lineConut);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                if (input!=null){
                    try {
                        //must close the reader
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(connection!=null){
                    //must disconnect the connection
                    connection.disconnect();
                }
            }




            return response.toString();
        }


        @Override
        protected void onPostExecute(String resutFromWebsite) {

            String currentWeather="current weather: ";

            try {

                //the main JSON object - initialize with string
                JSONObject mainObject= new JSONObject(resutFromWebsite);

                //extract data with getString, getInt getJsonObject - for inner objects or JSONArray- for inner arrays

                JSONArray myArray= mainObject.getJSONArray("weather");


                for(int i=0; i<myArray.length(); i++)
                {
                    //inner objects inside the array
                    JSONObject innerObj= myArray.getJSONObject(i);
                    String description= innerObj.getString("description");
                    Log.d("json", description);
                    currentWeather=currentWeather+ description;
                }

                JSONObject tempObject=   mainObject.getJSONObject("main");
                double  tmeper=   tempObject.getDouble("temp");

                currentWeather=currentWeather+ " Temp: "+tmeper;
                Log.d("json", ""+tmeper);

            } catch (JSONException e) {
                e.printStackTrace();

            }

            currentWaetherTV.setText(currentWeather);
        }






    }









}
