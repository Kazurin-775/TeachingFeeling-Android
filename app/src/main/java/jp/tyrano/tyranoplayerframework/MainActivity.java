package jp.tyrano.tyranoplayerframework;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {
    private static final String LOG_TAG = "TeachingFeeling";

    private WebView webView;
    private boolean paused = false;

    @Override
    @SuppressLint("SetJavaScriptEnabled")   // we know what we're doing
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ステータスバー削除
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webView = (WebView) findViewById(R.id.webview_playgame);

        // 画面を入れ替えてもリロードさせない
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
            return;
        }

        WebSettings settings = webView.getSettings();
        settings.setUserAgentString(settings.getUserAgentString() + ";tyranoplayer-android-1.0");
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUseWideViewPort(true);
        webView.clearCache(true);

        MyJavaScriptInterface obj = new MyJavaScriptInterface(this);
        webView.addJavascriptInterface(obj, "appJsInterface");

        try {
            webView.setWebViewClient(new TyranoWebViewClient(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(LOG_TAG, consoleMessage.message()
                        + " (file " + consoleMessage.sourceId()
                        + ":" + consoleMessage.lineNumber() + ")");
                return super.onConsoleMessage(consoleMessage);
            }
        });

        // Don't forget to inject tyrano_player.js at the bottom of <head>
        webView.loadUrl(TyranoWebViewClient.URL_PREFIX + "index.html");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (paused) {
            webView.onResume();
            webView.loadUrl("javascript:_tyrano_player.resumeAllAudio();");
            paused = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.loadUrl("javascript:_tyrano_player.pauseAllAudio();");
        webView.onPause();
        paused = true;
    }

    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private String readToString(InputStream input) throws IOException {
        try {
            byte[] data = new byte[input.available()];
            if (input.read(data) != data.length) {
                throw new IOException("short read");
            }
            if (input.read() != -1) {
                throw new IOException("expected EOF");
            }
            return new String(data);
        } finally {
            input.close();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        webView.restoreState(inState);
    }

    public void setStorage(String key, String val) {
        try {
            File file = new File(getExternalFilesDir(null), key + ".sav");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
            writer.write(val);
            writer.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to write storage " + key, e);
            Toast.makeText(this, "Failed to write storage: " + e, Toast.LENGTH_LONG).show();
        }
    }

    public String getStorage(String key) {
        try {
            File file = new File(getExternalFilesDir(null), key + ".sav");
            if (!file.exists()) {
                return "";
            }

            FileInputStream input = new FileInputStream(file);
            return readToString(input);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to read storage " + key, e);
            Toast.makeText(this, "Failed to read storage: " + e, Toast.LENGTH_LONG).show();
            return "";
        }
    }

    public void finishGame() {
        runOnUiThread(() -> {
            //本当に戻って良いですか
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.confirmation)
                    .setMessage(R.string.confirm_return_to_title)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // OK が押された
                        finish();
                        startActivity(getIntent());
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
    }

    public void openUrl(String url) {
        Uri uri = Uri.parse(url);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}
