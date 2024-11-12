package com.example.myapp.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapp.R;
import com.example.myapp.activity.user.MainPageActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String REGISTER_URL = "http://3.142.76.164/auth/register";
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.register_password);
        EditText confirmPasswordEditText = findViewById(R.id.make_sure_password);
        Button registerButton = findViewById(R.id.register_create_button);
        Button cancelButton = findViewById(R.id.register_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

                // 检查是否有空值
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 检查两次密码是否一致
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                registerUser(email, password);
            }
        });
    }

    private void registerUser(String email, String password) {
        try {
            // 创建 JSON 请求体
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);
            jsonObject.put("username", email); // 使用邮箱作为用户名
            jsonObject.put("password", password);

            RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(REGISTER_URL)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    Log.e("Register Error", e.getMessage(), e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                            // 注册成功后跳转到登录页面
                            //Intent intent = new Intent(RegisterActivity.this, MainPageActivity.class);
                            //startActivity(intent);
                            finish();
                        });
                    } else {
                        String errorMessage = response.code() == 400 ? "Registration failed" : "Server Error";
                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create request", Toast.LENGTH_SHORT).show();
        }
    }
}
