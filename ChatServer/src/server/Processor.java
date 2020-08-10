package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import server.client.Client;
import util.Pack;
import util.Tea;
import util.Tools;

import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * 处理器
 * */
public class Processor {

    public enum Operation {
        FirstContact,
        HeartBeat
    }

    /**
     * 通过操作索引获得指定操作
     * @param index 操作索引
     * */
    public static Operation getOperation(int index) {
        Operation[] ops = Operation.values();
        if (ops.length <= index) return null;
        return ops[index];
    }

    /**
     * 第一次接触服务器
     * 客户端需要提供的信息有：设备名称(device_name), 心跳包验证信息(heart_beat_verify)
     * 接触服务器后将向客户端发送通讯密钥，接下来所有的数据将全部使用此密钥加密
     */
    public static void firstContact(Server server, SocketChannel socketChannel, Pack.PackHead head, byte[] data) {
        if (data == null) return;

        Client client = server.clientManager.getClient(socketChannel);
        if (client != null && client.sessionKey == null) {
            client.sessionKey = Tools.RandomlyGeneratedKey(16);
            JsonObject jsonObject = new Gson().fromJson(new String(data, StandardCharsets.UTF_8), JsonObject.class);
            if (!jsonObject.isJsonNull()) {
                JsonElement element = null;
                element = jsonObject.get("device_name");
                if (!element.isJsonNull()) client.deviceName = element.getAsString();
                element = jsonObject.get("heart_beat_verify");
                if (!element.isJsonNull()) client.heartBeatVerify = element.getAsString();

                Log.debug(client.deviceName + "设备第一次接触了服务器，心跳包验证口令是：" + client.heartBeatVerify);
            }
            Tools.sendData(socketChannel, head.operation, head.seq, client.sessionKey);
        }
    }

    /**
     * 处理从客户端接收到的心跳包信息
     * 心跳包信息格式为：[包头][验证信息], 其中[验证信息]使用通讯密钥加密
     * 如果处理成功服务器将返回处理成功信息，使用通讯密钥加密
     */
    public static void heartBeat(Server server, SocketChannel socketChannel, Pack.PackHead head, byte[] data) {
        if (data == null) return;

        Client client = server.clientManager.getClient(socketChannel);
        JsonObject result = new JsonObject();

        if (client != null && client.sessionKey != null) {
            if (client.heartBeatVerify.equals(Tea.decryptByTea(data, client.sessionKey))) {
                client.lastHeartBeat = System.currentTimeMillis();
                result.addProperty("code", 0);
            } else {
                result.addProperty("code", -1);
                result.addProperty("message", "Verification failed");
            }

            Tools.sendData(socketChannel, head.operation, head.seq, Tea.encryptByTea(result.getAsString(), client.sessionKey));
        }
    }

}
