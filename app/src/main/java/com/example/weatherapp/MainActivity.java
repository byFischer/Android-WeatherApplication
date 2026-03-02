package com.example.weatherapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {

    ImageView backgroundImage;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int noktaSayisi = 0;
    private boolean yukleniyor= false;
    private Runnable noktaAnimasyonu;
    TextView textWeather;
    EditText editCity;
    Button btnGetWeather;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        AutoCompleteTextView autoCompleteCity;
        autoCompleteCity = findViewById(R.id.autoCompleteCity);
        Button btnGetWeather = findViewById(R.id.btnGetWeather);
        textWeather = findViewById(R.id.textWeather);
        backgroundImage = findViewById(R.id.backgroundImage);

        String[] sehirler = getResources().getStringArray(R.array.sehirler);

        CaseInsensitiveAdapter adapter= new CaseInsensitiveAdapter(
                this,android.R.layout.simple_dropdown_item_1line, sehirler
        );

        autoCompleteCity.setAdapter(adapter);
        autoCompleteCity.setThreshold(1);
        autoCompleteCity.setOnItemClickListener((parent, view, position, id) -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(autoCompleteCity.getWindowToken(), 0);
        });

        noktaAnimasyonu = new Runnable() {
            @Override
            public void run() {
                if (yukleniyor) {
                    noktaSayisi = (noktaSayisi % 3) + 1;
                    String noktalar = "";
                    for (int i = 0; i< noktaSayisi; i++){
                        noktalar += ".";
                    }
                    textWeather.setText("Yükleniyor" + noktalar);
                    handler.postDelayed(this, 500);
                }
            }
        };

        btnGetWeather.setOnClickListener(v -> {
            String city = autoCompleteCity.getText().toString();
            Log.d("WeatherApp", "Girilen Sehir: " + city);

            yukleniyor = true;
            handler.post(noktaAnimasyonu);
            new Thread(() -> {
                try {
                    String apiKey = BuildConfig.WEATHER_API_KEY;
                    String urlString =
                            "https://api.openweathermap.org/data/2.5/weather?q="
                                    + city +
                                    "&appid=" + apiKey +
                                    "&units=metric";

                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    Log.d("WeatherApp", "Response code: " + responseCode);

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    );

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonObject = new JSONObject(response.toString());

                    JSONObject main = jsonObject.getJSONObject("main");
                    double temp = main.getDouble("temp");

                    JSONArray weatherArray = jsonObject.getJSONArray("weather");
                    JSONObject weather = weatherArray.getJSONObject(0);
                    String description = weather.getString("description");

                    runOnUiThread(() -> {
                        textWeather.setText(

                                city + "\n" +
                                        "Sıcaklık: " + temp + "°C\n" +
                                        "Durum: " + description

                        );
                        if (description.contains("clear") || description.contains("sun")){
                            backgroundImage.setImageResource(R.drawable.weather_sunny);
                        } else if (description.contains("cloud")) {
                            backgroundImage.setImageResource(R.drawable.weather_cloudy);

                        } else if (description.contains("rain") || description.contains("drizzle")) {
                            backgroundImage.setImageResource(R.drawable.weather_rainy);
                        } else if (description.contains("snow")) {
                            backgroundImage.setImageResource(R.drawable.weather_snowy);
                        } else if (description.contains("thunder") || description.contains("Storm")) {
                            backgroundImage.setImageResource(R.drawable.weather_stormy);
                        }
                        else {
                            backgroundImage.setImageResource(R.drawable.weather_cloudy);
                        }

                        yukleniyor = false;
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();


        });




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Log.d("Weather", "Uygulama basariyla basladi");
    }
}