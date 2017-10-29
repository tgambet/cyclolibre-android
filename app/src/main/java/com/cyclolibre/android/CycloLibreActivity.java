package com.cyclolibre.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.*;

public class CycloLibreActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_FINE_LOCATION = 1;
    private String mGeolocationOrigin;
    private GeolocationPermissions.Callback mGeolocationCallback;

    public class GeoWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            // Geolocation permissions coming from this app's Manifest will only be valid for devices with
            // API_VERSION < 23. On API 23 and above, we must check for permissions, and possibly ask for them.
            System.out.println("test");
            String perm = Manifest.permission.ACCESS_FINE_LOCATION;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    ContextCompat.checkSelfPermission(CycloLibreActivity.this, perm) == PackageManager.PERMISSION_GRANTED) {
                // we're on SDK < 23 OR user has already granted permission
                callback.invoke(origin, true, false);
            } else {
                // ask the user for permission
                ActivityCompat.requestPermissions(CycloLibreActivity.this, new String[] {perm}, REQUEST_FINE_LOCATION);

                // we will use these when user responds
                mGeolocationOrigin = origin;
                mGeolocationCallback = callback;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_FINE_LOCATION:
                boolean allow = false;
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // user has allowed this permission
                    allow = true;
                }
                if (mGeolocationCallback != null) {
                    // call back to web chrome client
                    mGeolocationCallback.invoke(mGeolocationOrigin, allow, false);
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webview = new WebView(this);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webview.setWebChromeClient(new GeoWebChromeClient());

        webview.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    WebView webView = (WebView) v;
                    switch(keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if(webView.canGoBack()) {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }
                return false;
            }
        });

        webview.loadUrl("https://cyclolibre.herokuapp.com/");
        setContentView(webview);
    }
}
