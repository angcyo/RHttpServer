package com.angcyo.httpserver;

import android.util.Log;
import android.util.SparseArray;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

public class RServer implements HttpServerRequestCallback {

    private static final String TAG = "RServer";

    private static RServer mInstance;

    public static int PORT_LISTEN_DEFAULT = 5005;


    private SparseArray<AsyncHttpServer> serverMap = new SparseArray<>();
    //AsyncHttpServer server = new AsyncHttpServer();

    public static RServer getInstance() {
        if (mInstance == null) {
            // 增加类锁,保证只初始化一次
            synchronized (RServer.class) {
                if (mInstance == null) {
                    mInstance = new RServer();
                }
            }
        }
        return mInstance;
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

    public void startServer() {
        startServer(PORT_LISTEN_DEFAULT);
    }

    /**
     * 开启本地服务
     */
    public synchronized void startServer(int port) {
        AsyncHttpServer server = serverMap.get(port);
        if (server == null) {
            server = new AsyncHttpServer();

            //如果有其他的请求方式，例如下面一行代码的写法
            server.addAction("OPTIONS", "[\\d\\D]*", this);
            server.get("[\\d\\D]*", this);
            server.post("[\\d\\D]*", this);

            server.setErrorCallback(new CompletedCallback() {
                @Override
                public void onCompleted(Exception e) {
                    e.printStackTrace();
                }
            });
            serverMap.put(port, server);
            server.listen(port);
        }
    }

    public synchronized void stopServer(int port) {
        AsyncHttpServer server = serverMap.get(port);
        if (server != null) {
            server.stop();

            serverMap.put(port, null);
        }
    }

    @Override
    public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
        //AsyncServer 线程
        log("进来了，哈哈" + request.getHeaders().get("Host"));
        response.send(response.code() + " :" + request.getHeaders().get("Host") + " :" + request.getPath() + " :" + request.getBody());
        //response.redirect("http://www.baidu.com");
        response.end();

//        String uri = request.getPath();
//        //这个是获取header参数的地方，一定要谨记哦
//        Multimap headers = request.getHeaders().getMultiMap();
//
//        //注意：这个地方是获取post请求的参数的地方，一定要谨记哦
//        Multimap parms = ((AsyncHttpRequestBody<Multimap>) request.getBody()).get();
//
//        response.send("send1:" + headers + " " + parms);
//        response.send("send2:" + request + " " + response);
    }

    private void log(String log) {
        Log.d(TAG + "_" + Thread.currentThread().getName(), log);
    }

    public interface RServerListner {
        void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response);
    }
}