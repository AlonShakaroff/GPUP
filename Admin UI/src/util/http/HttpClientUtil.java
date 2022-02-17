package util.http;

import okhttp3.*;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpClientUtil {
    private final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .followRedirects(false)
                    .build();

    public static void runAsync(String finalUrl, String method, RequestBody body, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl).method(method, body)
                .build();

        HttpClientUtil.HTTP_CLIENT.newCall(request).enqueue(callback);
    }

    public static void runAsyncWithRequest(Request request, Callback callback) {
        HttpClientUtil.HTTP_CLIENT.newCall(request).enqueue(callback);
    }

    public static Response runSyncWithRequest(Request request) throws IOException {
        return  HttpClientUtil.HTTP_CLIENT.newCall(request).execute();
    }


    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}
