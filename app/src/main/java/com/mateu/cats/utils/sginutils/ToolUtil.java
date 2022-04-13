package com.mateu.cats.utils.sginutils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ToolUtil {
    /**
     * 获取当前app 语言
     *
     * @return
     */
    private static String getLanguageLocal() {
        Log.e("TAG", "getLanguageLocal");
        String language = "zh_cn";
        Log.e("TAG", "language-->" + language);

        return language;
    }

    /**
     * 获取当前系统的版本号
     *
     * @param mContext
     * @return
     * @throws Exception
     */
    public static String getVersionName(Context mContext) throws Exception {
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
        String version = packInfo.versionName;
        return version;
    }

    /**
     * 获取当前系统的uuid
     *
     * @param mContext
     * @return
     * @throws Exception
     */
    public static String getUuid(Context mContext) {
        return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 传递当前请求参数   返回携带加密相关参数
     *
     * @param mContext
     * @param params
     * @return
     * @throws Exception
     */
    public static Map<String, Object> createLinkString(Context mContext, Map<String, Object> params) throws Exception {
        String client = "android";
        Map<String, Object> mapresult = new ArrayMap<>();
        long timeStampSec = System.currentTimeMillis() / 1000;
        String timestamp = String.format("%010d", timeStampSec);
        long timestamplong = Long.parseLong(timestamp);
        String version = getVersionName(mContext);
        String language = getLanguageLocal();
        String uuid = getUuid(mContext);
        params.put("timestamp", timestamp);
        params.put("uuid", uuid);
        params.put("client", client);
        params.put("version", version);
        params.put("language", language);
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        if (timestamplong % 2 == 0) {
            Collections.reverse(keys);
        }
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i).toString();
            Object obj = params.get(key);
            String value = "";
            if (obj != null) {
                value = obj.toString();
            }

            if (value == null || value.equals("") || key.equalsIgnoreCase("sign"))
                continue;
            if (i == keys.size() - 1) {
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        if (prestr.endsWith("&")) {
            prestr = prestr.substring(0, prestr.lastIndexOf("&"));
        }
        mapresult.put("timestamp", timestamp);
        mapresult.put("version", version);
        mapresult.put("language", language);
        mapresult.put("uuid", uuid);
        mapresult.put("client", client);
        mapresult.put("sign", MD5.md5Decode32(prestr + "&salt=OelvRaICnoE7Q2fQ7aFE9XJVg4UPmTVj"));
        return mapresult;
    }
}
