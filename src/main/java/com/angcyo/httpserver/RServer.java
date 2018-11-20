package com.angcyo.httpserver;

import android.util.Log;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.NameValuePair;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;

public class RServer implements HttpServerRequestCallback {

    private static final String TAG = "RServer";

//    private static RServer mInstance;

    public static int PORT_LISTEN_DEFAULT = 5005;

    //    private SparseArray<AsyncHttpServer> serverMap = new SparseArray<>();
    AsyncHttpServer server;

    RServerListener serverListener;

    private RServer() {
    }

    public static RServer getInstance() {
//        if (mInstance == null) {
//            // 增加类锁,保证只初始化一次
//            synchronized (RServer.class) {
//                if (mInstance == null) {
//                    mInstance = new RServer();
//                }
//            }
//        }
//        return mInstance;
        return new RServer();
    }

//    public static enum Status {
//        REQUEST_OK(200, "请求成功"),
//        REQUEST_ERROR(500, "请求失败"),
//        REQUEST_ERROR_API(501, "无效的请求接口"),
//        REQUEST_ERROR_CMD(502, "无效命令"),
//        REQUEST_ERROR_DEVICEID(503, "不匹配的设备ID"),
//        REQUEST_ERROR_ENV(504, "不匹配的服务环境");
//
//        private final int requestStatus;
//        private final String description;
//
//        Status(int requestStatus, String description) {
//            this.requestStatus = requestStatus;
//            this.description = description;
//        }
//
//        public String getDescription() {
//            return description;
//        }
//
//        public int getRequestStatus() {
//            return requestStatus;
//        }
//    }

    public RServer setServerListener(RServerListener serverListener) {
        this.serverListener = serverListener;
        return this;
    }

    public void startServer() {
        startServer(PORT_LISTEN_DEFAULT);
    }

    /**
     * 开启本地服务
     */
    public synchronized RServer startServer(int port) {
        if (server == null) {
            server = new AsyncHttpServer();

            //如果有其他的请求方式，例如下面一行代码的写法
            server.addAction("OPTIONS", "[\\d\\D]*", this);
            server.get("[\\d\\D]*", this);
            server.post("[\\d\\D]*", this);

            server.setErrorCallback(new CompletedCallback() {
                @Override
                public void onCompleted(Exception e) {
                    if (serverListener != null) {
                        serverListener.onErrorCallback(e);
                    } else {
                        e.printStackTrace();
                    }
                }
            });
            server.listen(port);
        }
        return this;
    }

    public synchronized void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        StringBuilder logBuilder = new StringBuilder();
        try {
            logBuilder.append("请求:")
                    .append(request.getMethod())
                    .append(" ")
                    .append(URLDecoder.decode(request.getPath(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Multimap query = request.getQuery();
        if (query != null && !query.isEmpty()) {
            logBuilder.append("\n请求参数:");
            log(logBuilder, query);
        }

        if (request.getHeaders() != null) {
            logBuilder.append("\n请求头:");
            log(logBuilder, request.getHeaders().getMultiMap());
        }

        AsyncHttpRequestBody body = request.getBody();
        if (body != null) {
            logBuilder.append("\n请求体:");
            logBuilder.append("\nContent-Type:");
            logBuilder.append(body.getContentType());
            logBuilder.append("\nlength:");
            try {
                logBuilder.append(body.length());
            } catch (Exception e) {
                //e.printStackTrace();
                logBuilder.append("null");
            }
            logBuilder.append("\n");

            /*
             * UrlEncodedFormBody
             * content-type:application/x-www-form-urlencoded; charset=UTF-8
             * post 请求体 必须是 angcyo=1&bb=2 格式, 否则解析会失败
             * */

            /*
             * JSONObjectBody/JSONArrayBody
             * content-type: application/json
             *
             * FileBody/StreamBody
             * application/binary
             *
             * DocumentBody
             * application/xml
             *
             * MultipartFormDataBody
             * multipart/form-data
             *
             * StringBody
             * text/plain
             * */

            Object obj = body.get();
            if (obj instanceof Multimap) {
                log(logBuilder, (Multimap) obj);
            } else if (obj != null) {
                logBuilder.append(obj.getClass().getSimpleName());
                logBuilder.append("->");
                shortString(logBuilder, obj.toString());
            }
        }

        L.i(logBuilder);

        if (serverListener != null) {
            serverListener.onRequest(request, response);
            return;
        }

        //AsyncServer 线程
        log("进来了，哈哈" + request.getHeaders().get("Host"));
        StringBuilder builder = new StringBuilder();

        String ua = request.getHeaders().get("user-agent");
        //        response.send(response.code() + " :" + request.getHeaders().get("Host") + " :" + request.getPath() + " :" + request.getBody());
        //response.redirect("http://www.baidu.com");
//        response.end();

//        String uri = request.getPath();
//        //这个是获取header参数的地方，一定要谨记哦
        Multimap headers = request.getHeaders().getMultiMap();
//
//        //注意：这个地方是获取post请求的参数的地方，一定要谨记哦
        Multimap parms = ((AsyncHttpRequestBody<Multimap>) body).get();
//

//        response.send("a</br>b");
//        response.end();

        builder.append(headers);
        builder.append("</br>");
        builder.append(parms);
        builder.append("</br>");

        if (ua.contains("MQQBrowser")) {
            //腾讯tbs x5内核浏览器,腾讯体系
            if (ua.contains("QQ/")) {
                builder.append("QQ客户端");

                response.redirect("http://pay.hotapp.cn/108659959");
            } else if (ua.contains("MicroMessenger")) {
                builder.append("微信客户端");

                response.redirect("wxp://f2f0nuJx2qnA2ibH8hKgVQ4c2dWHsyy3-tej");
            }
        } else if (ua.contains("UCBrowser")) {
            //UC 浏览器内核,支付宝体系
            if (ua.contains("AlipayClient")) {
                builder.append("支付宝客户端");

                response.redirect("http://pay.hotapp.cn/108659959");
            }
        }

        //没次请求只能send 一次, 第二次send 无效.
        response.send(builder.toString());
        response.end();
    }

    public static void log(StringBuilder builder, Multimap multimap) {
        if (multimap == null || builder == null) {
            return;
        }
        if (multimap.isEmpty()) {
            return;
        }
        for (Iterator<NameValuePair> it = multimap.iterator(); it.hasNext(); ) {
            NameValuePair nameValuePair = it.next();
            builder.append("\n");
            builder.append(nameValuePair.getName());
            builder.append(":");
            shortString(builder, nameValuePair.getValue());
        }
    }

    private static void shortString(StringBuilder builder, String value) {
        int max_length = 1024;
        int length = -1;
        if (value == null) {
        } else {
            length = value.length();
        }
        if (length > max_length) {
            builder.append(value.substring(0, max_length));
            builder.append("数据过长, 剩余:");
            builder.append(length - max_length);
        } else {
            builder.append(value);
        }
    }

    private void log(String log) {
        Log.d(TAG + "_" + Thread.currentThread().getName(), log);
    }

    public interface RServerListener {
        void onRequest(@NotNull AsyncHttpServerRequest request, @NotNull AsyncHttpServerResponse response);

        void onErrorCallback(@NotNull Exception e);
    }
}