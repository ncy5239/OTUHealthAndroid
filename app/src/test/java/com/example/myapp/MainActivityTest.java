package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.junit.Assert.*;

public class MainActivityTest {

    private static final String LOGIN_URL = "http://3.142.76.164/auth/login";

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testLoginUser_successfulLogin() throws Exception {
        // 模拟登录的输入数据
        String email = "testuser@example.com";
        String password = "password123";

        // 创建 JSON 请求体
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);
        jsonObject.put("password", password);

        RequestBody requestBody = RequestBody.create(jsonObject.toString(), okhttp3.MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

        // 使用 OkHttpClient 发送请求
        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();

        // 检查是否登录成功
        assertTrue(response.isSuccessful());

        // 检查响应中是否包含 token
        String responseBody = response.body().string();
        JSONObject responseJson = new JSONObject(responseBody);
        assertTrue(responseJson.has("token"));

        // 保存 token 到 SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("jwt_token", responseJson.getString("token"));
        editor.apply();
    }
}
