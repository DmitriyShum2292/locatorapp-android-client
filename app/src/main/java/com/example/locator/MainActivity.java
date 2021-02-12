package com.example.locator;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button button;
    TextView textView;
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListenerOnButton();
        textView = (TextView)findViewById(R.id.textView);

        context = getApplicationContext();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void addListenerOnButton(){
        editTextEmail = (EditText)findViewById(R.id.editTextTextEmailAddress);
        editTextPassword = (EditText)findViewById(R.id.editTextTextPassword);
        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            String email = editTextEmail.getText().toString();
                            String password = editTextPassword.getText().toString();


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (context
                                    .checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {

                                startService(new Intent(MainActivity.this, MainService.class)
                                        .putExtra("email", email)
                                        .putExtra("password", password));

                            }
                        }
                    }
                }
        );
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}