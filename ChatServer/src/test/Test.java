package test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import util.Tea;
import util.Tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class Test {
    public static void main(String[] args) throws IOException {
        ClientLoop loop = new ClientLoop("127.0.0.1", 8080);
        loop.start();
        loop.sendData(0, 1, "{'device_name':'Windows', 'heart_beat_verify':'Hello heart beat'}".getBytes(StandardCharsets.UTF_8));
    }

}
