package com.mateu.cats.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by Administrator on 2016/10/10.
 * 封装的GSON解析工具类，提供泛型参数
 */
public class GsonUtil {

    private static GsonUtil instance;
    private static Gson gson;

    private GsonUtil() {}

    public static GsonUtil getInstance() {
        if (instance == null) {
            gson = new GsonBuilder().create();
            instance = new GsonUtil();
        }
        return instance;
    }

    public Gson getGson() {
        return gson;
    }

    /** 将Json数据解析成相应的映射对象 */
    public <T> T fromJson(String jsonData, Class<T> type) {
        T result = gson.fromJson(jsonData, type);
        return result;
    }

    // 在创建完gson之后, 使用时注意自己注册的type类型 , 这时转换出的int 不会变成double
    public ArrayList<TreeMap<String, Object>> fromJsonListMap(String json) {
        ArrayList<TreeMap<String, Object>> mList = new ArrayList<TreeMap<String, Object>>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for(final JsonElement elem : array){
            mList.add((TreeMap<String, Object>) gson.fromJson(elem, new TypeToken<TreeMap<String, Object>>(){}.getType()));
        }
        return mList;
    }

    /** 将Json数据解析成相应的映射对象列表 */
    public <T> ArrayList<T> fromJsonList(String json, Class<T> cls) {
        ArrayList<T> mList = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        if (array != null && array.size() > 0) {
            for (final JsonElement elem : array) {
                mList.add(gson.fromJson(elem, cls));
            }
        }
        return mList;
    }

}
