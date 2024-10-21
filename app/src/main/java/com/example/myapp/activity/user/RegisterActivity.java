package com.example.myapp.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerPassword, makeSurePassword, email;
    private Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 绑定 Toolbar
        Toolbar toolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 绑定 EditText 控件
        registerPassword = findViewById(R.id.register_password);
        makeSurePassword = findViewById(R.id.make_sure_password);
        email = findViewById(R.id.email);

        // 绑定 Button 控件
        createAccountButton = findViewById(R.id.register_create_button);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户输入的邮箱和密码
                String password = registerPassword.getText().toString();
                String confirmPassword = makeSurePassword.getText().toString();
                String userEmail = email.getText().toString();

                // 检查是否有未填信息
                if (TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 检查两个输入是否一致
                if (!password.equals(confirmPassword)) {
                    // 如果密码不一致
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 进行注册请求
                performRegistration(userEmail, password);
            }
        });
    }

    private void performRegistration(String email, String password) {
        // 创建 JSON 对象
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建 OkHttpClient 实例
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url("http://ec2-18-222-203-103.us-east-2.compute.amazonaws.com/users/") // 注册接口 URL
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        // 在后台线程中执行网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, "Registration Successful!!!!!!!!", Toast.LENGTH_SHORT).show();
                            // 在这里，可以存储用户 ID 或令牌，并跳转到填写详细信息的页面
                            finish();
                        });
                    } else {
                        // 打印 Bad Request 详细内容
                        Log.e("BadRequest", "Code: " + response.code() + ", Error Body: " + responseBody);
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration Failed: " + response.code() + " - " + responseBody,
                                    Toast.LENGTH_LONG).show();
                        });

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Network Error", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }
}
