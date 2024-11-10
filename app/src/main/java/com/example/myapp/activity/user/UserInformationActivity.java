package com.example.myapp.activity.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapp.MainActivity;
import com.example.myapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserInformationActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://3.142.76.164/user/";
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "jwt_token";
    private String jwtToken;
    private OkHttpClient client = new OkHttpClient();

    private EditText nameEditText, ageEditText, weightEditText, annotationEditText;
    private RadioGroup genderRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinformation);

        // Load JWT token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        jwtToken = sharedPreferences.getString(TOKEN_KEY, null);

        // Check if jwtToken exists; if not, prompt re-login
        if (jwtToken == null) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(UserInformationActivity.this, MainActivity.class));
            finish();
            return;
        }

        // Initialize UI elements
        nameEditText = findViewById(R.id.Name);
        ageEditText = findViewById(R.id.Age);
        weightEditText = findViewById(R.id.Weight);
        annotationEditText = findViewById(R.id.Annotation);
        genderRadioGroup = findViewById(R.id.rg_gender);

        Button saveButton = findViewById(R.id.user_profile_save);
        saveButton.setOnClickListener(v -> updateUserInformation());

        Button cancelButton = findViewById(R.id.user_profile_cancle);
        cancelButton.setOnClickListener(v -> {
            startActivity(new Intent(UserInformationActivity.this, MainPageActivity.class));
            finish();
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_user_profile);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_health_manage) {
                    startActivity(new Intent(UserInformationActivity.this, MainPageActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_disease_consult) {
                    startActivity(new Intent(UserInformationActivity.this, DiseaseActivity.class));
                    finish();
                    return true;
                }

                return false;
            }
        });

        // Load user information on activity start
        loadUserInformation();
    }

    private void loadUserInformation() {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + jwtToken) // Use loaded jwtToken
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(UserInformationActivity.this, "Failed to load user info", Toast.LENGTH_SHORT).show());
                Log.e("Load User Error", e.getMessage(), e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject userData = new JSONObject(response.body().string());
                        runOnUiThread(() -> populateUserFields(userData));
                    } catch (JSONException e) {
                        Log.e("Parse Error", e.getMessage(), e);
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(UserInformationActivity.this, "Error loading user info", Toast.LENGTH_SHORT).show());
                    Log.e("Load User Response", "Code: " + response.code() + ", Body: " + response.body().string());
                }
            }
        });
    }

    private void populateUserFields(JSONObject userData) {
        try {
            nameEditText.setText(userData.getString("username"));
            ageEditText.setText(String.valueOf(userData.getInt("age")));
            weightEditText.setText(String.valueOf(userData.getInt("weight")));
            annotationEditText.setText(userData.optString("annotation", ""));

            String gender = userData.getString("gender");
            if (gender.equalsIgnoreCase("male")) {
                genderRadioGroup.check(R.id.rb_male);
            } else if (gender.equalsIgnoreCase("female")) {
                genderRadioGroup.check(R.id.rb_female);
            } else {
                genderRadioGroup.check(R.id.rb_other);
            }
        } catch (JSONException e) {
            Log.e("Populate Error", e.getMessage(), e);
        }
    }

    private void updateUserInformation() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", nameEditText.getText().toString());
            jsonObject.put("age", Integer.parseInt(ageEditText.getText().toString()));
            jsonObject.put("weight", Integer.parseInt(weightEditText.getText().toString()));

            int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
            String gender = selectedGenderId == R.id.rb_male ? "male" : selectedGenderId == R.id.rb_female ? "female" : "other";
            jsonObject.put("gender", gender);

            RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .addHeader("Authorization", "Bearer " + jwtToken) // Use loaded jwtToken
                    .put(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(UserInformationActivity.this, "Failed to update user info", Toast.LENGTH_SHORT).show());
                    Log.e("Update Error", e.getMessage(), e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(UserInformationActivity.this, "User info updated", Toast.LENGTH_SHORT).show());
                        startActivity(new Intent(UserInformationActivity.this, MainPageActivity.class));
                        finish();

                    } else {
                        Log.e("Update Error", "Code: " + response.code() + ", Body: " + responseBody);
                        runOnUiThread(() -> {
                            Toast.makeText(UserInformationActivity.this, "Error updating user info: " + response.code(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (JSONException e) {
            Log.e("JSON Error", e.getMessage(), e);
        }
    }
}
