package cn.itcast.netty_param_better;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import itcast.protocol.Serializer;

public class TestGson {
    public static void main(String[] args) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Serializer.ClassCodec()).create();
        System.out.println(gson.toJson(String.class));
    }
}
