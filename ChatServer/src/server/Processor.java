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
        HeartBeat,
        UserLogin
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
     *
     * 客户端需要提供的信息有：设备名称(device_name), 心跳包验证信息(heart_beat_verify)
     *
     * 接触服务器后将向客户端发送通讯密钥，接下来所有的数据将全部使用此密钥加密
     */
    public static void firstContact(Server server, SocketChannel socketChannel, Pack.PackHead head, byte[] data) {
        if (data == null) return;

        Client client = server.clientManager.getClient(socketChannel);
        if (client != null && client.sessionKey == null) {
            //解析客户端发送来的信息
            JsonObject jsonObject = null;

            try {
                jsonObject = new Gson().fromJson(new String(data, StandardCharsets.UTF_8), JsonObject.class);
            } catch (Exception ignore) { }

            if (jsonObject != null && !jsonObject.isJsonNull()) {
                JsonElement element;

                client.sessionKey = Tools.RandomlyGeneratedKey(16);

                element = jsonObject.get("device_name");
                if (!element.isJsonNull()) client.deviceName = element.getAsString();
                element = jsonObject.get("heart_beat_verify");
                if (!element.isJsonNull()) client.heartBeatVerify = element.getAsString();

                Log.debug(client.deviceName + "设备第一次接触了服务器，心跳包验证口令是：" + client.heartBeatVerify);
                Tools.sendData(socketChannel, head.operation, head.seq, client.sessionKey);
            }
        }
    }

    /**
     * 处理从客户端接收到的心跳包信息
     *
     * 心跳包信息格式为：[包头][验证信息], 其中[验证信息]使用通讯密钥加密
     *
     * 如果处理成功服务器将返回处理成功信息，使用通讯密钥加密
     */
    public static void heartBeat(Server server, SocketChannel socketChannel, Pack.PackHead head, byte[] data) {
        if (data == null) return;

        Client client = server.clientManager.getClient(socketChannel);
        JsonObject result = new JsonObject();

        if (client != null && client.sessionKey != null) {
            //心跳包验证信息是被加密的，需要使用密钥进行解密
            String decryptResult = Tea.decryptByTea(data, client.sessionKey);
            if (decryptResult != null)
            {
                if (client.heartBeatVerify.equals(decryptResult)) {
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


    /**
     * 用户登录请求
     *
     * 在用户进行登录操作后将向服务器发送登录验证，数据格式为[数据包包头][加密的登录信息json]
     *
     * 登录信息json有user字段，记录用户的id或用户名，以及password字段，记录的是用户密码的32位md5值
     *
     * 当服务器接收到登录验证请求后，将在数据库内匹配，匹配规则：(id = json.user OR username = json.user) AND password = json.password
     *
     * 若匹配成功取出用户id，查询 ID-客户端通道 映射图中是否已有信息，若有则通知映射图中的客户端下线，并删除此 ID-客户端通道 映射图中的指定信息以及将 客户端通道-客户端信息 映射图中对应的用户id和login_token置空
     *
     * 接着为用户随机分配一个32位长的login_token，用户接下来的所有操作都将使用到此login_token进行验证，同一时间同一用户仅允许有一个login_token
     *
     * 服务器将在 用户ID-客户端通道 映射图中加入对应的信息，在 客户端通道-客户端信息 映射图中赋值用户id和login_token
     *
     * 返回格式为：[数据包头][加密的json信息]
     *
     * 加密的json信息：若登录成功则包含3个节点（code，login_token，user_id），其中code的值为0，若登录失败则包含2个节点(code，message)，其中code为错误代码，message为错误描述文本
     * */
    public static void userLogin(Server server, SocketChannel socketChannel, Pack.PackHead head, byte[] data) {
        if (data == null) return;

        Client client = server.clientManager.getClient(socketChannel);
        if (client != null && client.sessionKey != null) {
            JsonObject validation = new Gson().fromJson(Tea.decryptByTea(data, client.sessionKey), JsonObject.class);
            if (validation == null || validation.isJsonNull()) return;
            JsonObject result = new JsonObject();

            long uid = server.dataBase.matchUser(validation.get("user").toString(), validation.get("password").toString());
            if (uid < 0) {
                result.addProperty("code", uid);
                result.addProperty("message", "Validation fails");
            } else {
                client.uid = uid;
                client.login_token = Tools.RandomlyToken(16);
                server.clientManager.updateUserToChannel(uid, socketChannel);

                result.addProperty("code", 0);
                result.addProperty("user_id", uid);
                result.addProperty("login_token", client.login_token);
            }

            Tools.sendData(socketChannel, head.operation, head.seq, Tea.encryptByTea(result.getAsString(), client.sessionKey));
        }
    }

}
