package server.loop;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import server.global.ClientManager;
import server.log.ServerLog;
import server.util.Pack;
import server.util.Tools;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Processor {
    public synchronized static void firstContact(SocketChannel socketChannel, Pack.PackHead head, byte[] data) {
        ClientManager.ClientInfo clientInfo = ClientManager.getClientInfo(socketChannel);
        if (clientInfo != null && clientInfo.sessionKey == null) {
            clientInfo.sessionKey = Tools.RandomlyGeneratedKey(16);
            //JsonObject jsonObject = new Gson().fromJson(new String(data), JsonObject.class);
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(Pack.makeHead(Pack.Operation.FirstContact.ordinal(), head.seq, clientInfo.sessionKey.length));
                byteArrayOutputStream.write(clientInfo.sessionKey);
                byteArrayOutputStream.flush();
                socketChannel.write(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()));
            } catch (Exception e) {
                ServerLog.warn(e.toString());
            }
        }
    }

    public synchronized static void heartBeat(SocketChannel socketChannel, Pack.PackHead head, byte[] data) {

    }

}
