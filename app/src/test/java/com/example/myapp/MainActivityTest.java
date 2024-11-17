package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.*;

public class MainActivityTest {

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String TOKEN_KEY = "jwt_token";

    private Context context;
    private MockWebServer mockWebServer;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void testLoginUser_successfulLogin() throws Exception {
        // 模拟服务端返回的 JSON 响应
        String mockToken = "mocked_jwt_token";
        String mockResponseBody = "{ \"token\": \"" + mockToken + "\" }";

        // 设置 MockWebServer 的响应
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockResponseBody)
                .addHeader("Content-Type", "application/json"));

        // 获取 MockWebServer 的 URL
        String mockLoginUrl = mockWebServer.url("/auth/login").toString();

        // 模拟登录的输入数据
        String email = "testuser@example.com";
        String password = "password123";

        // 创建 JSON 请求体
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", email);
        jsonObject.put("password", password);

        RequestBody requestBody = RequestBody.create(jsonObject.toString(), okhttp3.MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(mockLoginUrl)
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
        assertEquals(mockToken, responseJson.getString("token"));

        // 保存 token 到 SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, responseJson.getString("token"));
        editor.apply();

        // 验证 token 是否已成功保存
        String savedToken = sharedPreferences.getString(TOKEN_KEY, null);
        assertNotNull(savedToken);
        assertEquals(mockToken, savedToken);
    }
}
