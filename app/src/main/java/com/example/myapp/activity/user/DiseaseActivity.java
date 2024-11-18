package com.example.myapp.activity.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapp.MainActivity;
import com.example.myapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class DiseaseActivity extends AppCompatActivity {
    private static final String WEB_URL = "http://otu-healthcare-h5.s3-website.us-east-2.amazonaws.com/healthlife?email=";
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String EMAIL_KEY = "user_email";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diseasepage);
        /////////////////////////////
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String email = sharedPreferences.getString(EMAIL_KEY, null);

        if (email != null) {
            String encodedEmail = email.replace("@", "%40");
            String fullUrl = WEB_URL + encodedEmail;

            WebView webView = findViewById(R.id.disease_webview);
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);  // 启用DOM存储
            // 设置WebView的自适应属性
            webSettings.setLoadWithOverviewMode(true); // 缩放内容以适应WebView的宽度
            webSettings.setUseWideViewPort(true);      // 启用宽视口支持

            // 设置自定义的 User-Agent
            //webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");

            // 设置 WebViewClient 和 WebChromeClient
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return false; // 返回 false，允许 WebView 自行处理 URL
                }
            });

            webView.setWebChromeClient(new WebChromeClient()); // 支持 window.open 等操作
            WebView.setWebContentsDebuggingEnabled(true); // 启用调试

            // 启用Cookie支持
            CookieManager.getInstance().setAcceptCookie(true);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

            Log.d("WebView URL", "Loading URL: " + fullUrl);
            webView.loadUrl(fullUrl);
        } else {
            startActivity(new Intent(DiseaseActivity.this, MainActivity.class));
            finish();
            return;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_disease_consult);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_health_manage) {
                    startActivity(new Intent(DiseaseActivity.this, MainPageActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_user_profile) {
                    startActivity(new Intent(DiseaseActivity.this, UserInformationActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }

                return false;
            }
        });



    }
}
