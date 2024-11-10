package com.example.myapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapp.activity.user.MainPageActivity;
import com.example.myapp.activity.user.RegisterActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String LOGIN_URL = "http://3.142.76.164/auth/login";
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String EMAIL_KEY = "user_email"; // 新增，保存用户的电子邮件
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button register_button = findViewById(R.id.create_account_button);
        register_button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        EditText accountEditText = findViewById(R.id.login_account);
        EditText passwordEditText = findViewById(R.id.login_password);
        Button login_button = findViewById(R.id.login_button);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = accountEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUser(email, password);
            }
        });
    }

    private void loginUser(String email, String password) {
        try {
            // 创建 JSON 请求体
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);
            jsonObject.put("password", password);

            RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(LOGIN_URL)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    Log.e("Login Error", e.getMessage(), e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJson = new JSONObject(responseBody);
                            String token = responseJson.getString("token");

                            // 保存 token 和 email 到 SharedPreferences
                            saveUserInfo(token, email);

                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Log In Successful!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, MainPageActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } catch (Exception e) {
                            Log.e("JSON Parse Error", e.getMessage(), e);
                        }
                    } else {
                        // 输出错误状态码和响应体
                        Log.e("Login Response Error", "Code: " + response.code() + ", Body: " + responseBody);
                        runOnUiThread(() -> {
                            String errorMessage = response.code() == 401 ? "Invalid email or password" : "Server Error";
                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create request", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserInfo(String token, String email) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.putString(EMAIL_KEY, email); // 保存电子邮件到 SharedPreferences
        editor.apply();
        Log.d("Token Saved", "JWT token and email saved to SharedPreferences");
    }
}
