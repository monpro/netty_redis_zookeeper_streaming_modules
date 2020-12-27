package common;

import com.alibaba.fastjson.JSONObject;

import java.util.concurrent.CompletableFuture;

public class RestHelper {
    public static JSONObject genResponse(int code, String msg, JSONObject data) {
        JSONObject res = new JSONObject();
        res.put("code", code);
        if(msg != null) {
            res.put("msg", msg);
        }
        if(data != null) {
            res.put("data", data);
        }
        return res;
    }

    public static JSONObject genResponse(int code, String msg) {
        return genResponse(code, msg, null);
    }

    public static String genResponseString(int code, String msg) {
        return genResponse(code, msg, null).toString();
    }

    public static CompletableFuture<String> genResponseFuture(int code, String msg) {
        return CompletableFuture.completedFuture(genResponseString(code, msg));
    }

}
