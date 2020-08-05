package server.global;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.*;

import server.log.ServerLog;

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
                ServerLog.warn("未找到符合的客户端：" + cl.getRemoteAddress());
            } else {
                if (cl.isOpen()) {
                    ServerLog.info("关闭客户端：" + cl.getRemoteAddress());
                    cl.close();
                }
                if (remove_map) OnlineClient.remove(cl);
            }
        } catch (IOException e) {
            ServerLog.warn("关闭客户端时发生异常：" + e.toString());
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
                        ServerLog.info("客户端心跳超时：" + sc.getRemoteAddress());
                    } catch (IOException ignored) {

                    }
                    closeClient(sc, false);
                    closed.add(sc);
                }
            }
        }

        for (SocketChannel sc : closed) OnlineClient.remove(sc);
    }

    public static class ClientInfo {
        public long lastHeartBeat = 0;
        public byte[] sessionKey = null;
        public ClientInfo(long lastHeartBeat)
        {
            this.lastHeartBeat = lastHeartBeat;
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
                    ServerLog.debug("CheckHeartBeatLoop...");
                    CheckAllHeartBeat();
                }


            } catch (InterruptedException e) {
                ServerLog.debug("心跳包Loop异常：" + e.toString());
            }
        }
    }

    public static ClientInfo getClientInfo(SocketChannel socketChannel)
    {
        return OnlineClient.get(socketChannel);
    }
}
