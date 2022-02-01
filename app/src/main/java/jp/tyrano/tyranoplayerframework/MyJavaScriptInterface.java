package jp.tyrano.tyranoplayerframework;

import android.webkit.JavascriptInterface;

/**
 * Created by nagatani on 2016/06/27.
 */
public class MyJavaScriptInterface {
    private MainActivity activity;

    public MyJavaScriptInterface(MainActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void setStorage(String key, String val) {
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
}
