package jp.tyrano.tyranoplayerframework;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

/**
 * Created by nagatani on 2016/06/27.
 */
public class MyJavaScriptInterface {
    private static final String LOG_TAG = "TeachingFeeling";

    private MainActivity activity;

    public MyJavaScriptInterface(MainActivity activity) {
        this.activity = activity;
    }

    // 音楽関係の命令取得
    @JavascriptInterface
    public void audio(String json_str) {
        Log.e(LOG_TAG, "Audio: " + json_str);

        try {
            JSONObject obj = new JSONObject(json_str);
            // context.audio(obj);
        } catch (Exception e) {
            Log.e(LOG_TAG, "JSON ERROR!!!", e);
        }
    }

    @JavascriptInterface
    public void setStorage(String key, String val) {
        // セーブデータを保存する。
        activity.setStorage(key, val);
    }

    @JavascriptInterface
    public String getStorage(String key) {
        return activity.getStorage(key);
    }

    @JavascriptInterface
    public void finishGame() {
        activity.finishGame();
    }

    @JavascriptInterface
    public void openUrl(String url) {
        activity.openUrl(url);
    }

    @JavascriptInterface
    public void stopMovie() {
    }
}
