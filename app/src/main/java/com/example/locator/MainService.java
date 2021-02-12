package com.example.locator;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.Gson;

public class MainService extends Service implements LocationListener {

    private OkHttpClient client = new OkHttpClient();
    static Location imHere;
    private static final int interval = 3600000;
    private String email;
    private String password;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground();
            SetUpLocationListener(getApplicationContext());
        }
        else {
            startForeground(1, new Notification());
            SetUpLocationListener(getApplicationContext());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");

        Toast toast = Toast.makeText(getApplicationContext(),
                "Service started", Toast.LENGTH_SHORT);
        toast.show();

        run.start();

        return START_STICKY;
    }
    @Override
    public void onDestroy() {

    }

    Thread run = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    try {
                        if (imHere!=null) {
                            double lat = imHere.getLatitude();
                            double lon = imHere.getLongitude();
                            postCoordinates(email, password, lat, lon);
                        }
                        else {
                            double lat = 0;
                            double lon = 0;
                            postCoordinates(email, password, lat, lon);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Thread.sleep(interval);
                } catch (InterruptedException ex) {
                }
            }
        }
    });

    public void postCoordinates(String email,String password,double lat,double lon) throws IOException {
        Coordinate coordinate = new Coordinate();
        coordinate.setLat(lat);
        coordinate.setLon(lon);
        Gson gson = new Gson();
        String json = gson.toJson(coordinate);
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);

        Request request = new Request.Builder()
                .url("http://185.139.70.180:8081/add")
                .addHeader("Authorization", Credentials.basic(email, password))
                .post(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

    }

    public static void SetUpLocationListener(Context context)
    {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new MainService();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(context.getApplicationContext()
                    .checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1800000,
                        100,
                        locationListener);

                imHere = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        imHere = loc;
    }
    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
}
