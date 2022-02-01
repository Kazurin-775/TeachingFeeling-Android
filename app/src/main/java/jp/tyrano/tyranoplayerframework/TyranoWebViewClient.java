package jp.tyrano.tyranoplayerframework;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TyranoWebViewClient extends WebViewClient {
    public static final String URL_PREFIX = "https://app.tyrano.net/";
    private static final String LOG_TAG = "TeachingFeeling";
    private static final String DEFAULT_MIME_TYPE = "text/plain";

    private Context context;
    private ArrayList<ZipFile> packages = new ArrayList<>();

    public TyranoWebViewClient(Context context) throws IOException {
        this.context = context;
        File assetsDir = context.getExternalFilesDir(null);
        tryAddAssetPackage(new File(assetsDir, "patch.zip"));
        tryAddAssetPackage(new File(assetsDir, "app.zip"));
    }

    private void tryAddAssetPackage(File file) throws IOException {
        if (file.isFile()) {
            Log.d(LOG_TAG, "Found " + file.getName());
            packages.add(new ZipFile(file));
        }
    }

    // https://chromium.googlesource.com/chromium/src/+/HEAD/android_webview/docs/cors-and-webview-api.md
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (url.startsWith(URL_PREFIX)) {
            try {
                return processRequest(url);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error processing request", e);
                return new WebResourceResponse(null, null, null);
            }
        }
        Log.w(LOG_TAG, "Passing-through unknown URL " + url);
        return super.shouldInterceptRequest(view, url);
    }

    private WebResourceResponse processRequest(String url) throws IOException, URISyntaxException {
        String path = new URI(url).normalize().getPath();
        if (path.startsWith("/"))
            path = path.substring(1);

        for (ZipFile pack : packages) {
            ZipEntry entry = pack.getEntry(path);
            if (entry != null) {
                Log.d(LOG_TAG, "Load " + path + ": OK");
                return new WebResourceResponse(guessMimeType(path), null, pack.getInputStream(entry));
            }
        }

        // 404 Not Found
        Log.w(LOG_TAG, "Load " + path + ": not found");
        WebResourceResponse response = url.endsWith(".html") ?
                new WebResourceResponse("text/html", "UTF-8",
                        context.getResources().openRawResource(R.raw.not_found)) :
                new WebResourceResponse(null, null, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            response.setStatusCodeAndReasonPhrase(404, "Not Found");
        }
        return response;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        Log.e(LOG_TAG, "Failed to load URL " + failingUrl + ": " + description);
    }

    // https://github.com/androidx/androidx/blob/androidx-main/webkit/webkit/src/main/java/androidx/webkit/internal/AssetHelper.java
    private static String guessMimeType(String filePath) {
        String mimeType = URLConnection.guessContentTypeFromName(filePath);
        return mimeType == null ? DEFAULT_MIME_TYPE : mimeType;
    }
}
