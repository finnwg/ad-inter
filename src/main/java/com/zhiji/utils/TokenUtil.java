package com.zhiji.utils;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author finnwg
 * @version 1.0
 * @desc
 * @date 2022/2/28 17:33
 */
public class TokenUtil {
    /**
     * @description post请求方法，刷新token
     * @author      finnwg
     * @updateTime 2022/2/28 17:00
     * @param
     * @return
     */

    public static String getToken (String getTokenUrl,String jsonStr) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(50, TimeUnit.SECONDS).build();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonStr);
        Request request = new Request.Builder()
                .url(getTokenUrl)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    /**
     * @description get请求方法，获取结果数据
     * @author      finnwg
     * @updateTime 2022/2/28 17:01
     * @param
     * @return
     */
    public static String get(String url, String accessToken) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(50, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Access-Token", accessToken)
                .build();
        Response response = client.newCall(request).execute();
        //System.out.println(response.body().string());
        return response.body().string();
    }


    public static String qqGet(String url) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(50, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }


}
