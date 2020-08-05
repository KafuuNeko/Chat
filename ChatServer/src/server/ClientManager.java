package server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.*;

import server.loop.Processor;
import util.Pack;

public class ClientManager {

    public static Map<SocketChannel, ClientInfo> OnlineClient = new HashMap<>();

    /*
     * 获取连接的客户端数量*/
    public static int clientNumber() {
        return OnlineClient.size();
    }

    /*
     * 添加新的Client*/
    public static void addClient(SocketChannel cl) {
        OnlineClient.put(cl, new ClientInfo(System.currentTimeMillis()));
    }

    /*
     * 关闭一个Client*/
    public static void closeClient(SocketChannel cl, boolean remove_map) {
        ClientInfo info = OnlineClient.get(cl);
        try {
            if (info == null) {
                Log.warn("未找到符合的客户端：" + cl.getRemoteAddress());
            } else {
                if (cl.isOpen()) {
                    Log.info("关闭客户端：" + cl.getRemoteAddress());
                    cl.close();
                }
                if (remove_map) OnlineClient.remove(cl);
            }
        } catch (IOException e) {
            Log.warn("关闭客户端时发生异常：" + e.toString());
        }
    }

    public static void clear() {
        for (SocketChannel sc : OnlineClient.keySet()) closeClient(sc, false);
        OnlineClient.clear();
    }

    public static void CheckAllHeartBeat() {
        Set<SocketChannel> closed = new HashSet<>();

        long now = System.currentTimeMillis();
        for (SocketChannel sc : OnlineClient.keySet()) {
            ClientInfo info = OnlineClient.get(sc);
            if (info != null) {
                if (now - info.lastHeartBeat >= 60 * 1000) {
                    try {
                        Log.info("客户端心跳超时：" + sc.getRemoteAddress());
                    } catch (IOException ignored) {

                    }
                    closeClient(sc, false);
                    closed.add(sc);
                }
            }
        }

        for (SocketChannel sc : closed) OnlineClient.remove(sc);
    }

    public static class ClientInfo implements Pack.IPackProcessor {
        //此客户端最近一次的心跳时间
        public long lastHeartBeat = 0;
        //通讯密钥，此客户端与服务器的通讯均采用此密钥加密
        public byte[] sessionKey = null;
        //客户端设备名称
        public String deviceName = "";
        //客户端心跳包验证口令
        public String heartBeatVerify = "";

        //客户端包处理
        private Pack mPack = new Pack(this);

        public ClientInfo(long lastHeartBeat) {
            this.lastHeartBeat = lastHeartBeat;
        }

        public void disposeBytes(SocketChannel socketChannel, byte[] data) throws IOException
        {
            mPack.disposeBytes(socketChannel, data);
        }

        @Override
        public void onPackUnpack(SocketChannel socketChannel, Pack.PackHead head, byte[] data) {
            Pack.Operation operation = Pack.getOperation(head.operation);
            if (operation == null) return;
            switch (operation) {
                case FirstContact:
                    Processor.firstContact(socketChannel, head, data);
                    break;

                case HeartBeat:
                    Processor.heartBeat(socketChannel, head, data);
                    break;
            }
        }
    }

    public static class HeartBeatLoop extends Thread {
        @Override
        public void run() {

            try {
                while (Global.ServerStatus != Global.SERVER_STATUS_ERROR) {
                    while (Global.ServerStatus == Global.SERVER_STATUS_SUSPENDED) sleep(1000);//挂起等待

                    //10秒验证一次心跳包
                    sleep(10000);
                    Log.debug("CheckHeartBeatLoop...");
                    CheckAllHeartBeat();
                }

            } catch (InterruptedException e) {
                Log.error("心跳包Loop异常：" + e.toString());
            }
        }
    }

    public static ClientInfo getClientInfo(SocketChannel socketChannel) {
        return OnlineClient.get(socketChannel);
    }
}
