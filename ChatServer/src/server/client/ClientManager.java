package server.client;

import server.Log;
import server.Server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * 客户端管理器
 * */
public class ClientManager {

    //在线的客户端以及对应的客户端信息
    public Map<SocketChannel, Client> OnlineClient = new HashMap<>();

    /**
     * 获取当前在线的客户端数量
     * */
    public int clientNumber() {
        return OnlineClient.size();
    }

    /**
     * 添加新的在线客户端
     * */
    public void addClient(Server server, SocketChannel cl) {
        OnlineClient.put(cl, new Client(server, System.currentTimeMillis()));
    }

    /**
     * 关闭一个客户端
     * @param cl 要关闭的客户端
     * @param remove_map 关闭此客户端后是否立即从Map中移除
     * */
    public void closeClient(SocketChannel cl, boolean remove_map) {
        Client client = OnlineClient.get(cl);
        try {
            if (client == null) {
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

    /**
     * 主动关闭所有在线的客户端
     * */
    public void closeAllClient() {
        for (SocketChannel sc : OnlineClient.keySet()) closeClient(sc, false);
        OnlineClient.clear();
    }

    /**
     * 验证所有在线的客户端的心跳包
     * 如果最近的心跳包在60秒之前，则主动断开此客户端的连接
     * */
    public void checkAllHeartBeat() {
        Set<SocketChannel> closed = new HashSet<>();

        long now = System.currentTimeMillis();
        for (SocketChannel sc : OnlineClient.keySet()) {
            Client client = OnlineClient.get(sc);
            if (client != null) {
                if (now - client.lastHeartBeat >= 60 * 1000) {
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

    /**
     * 通过SocketChannel获取客户端
     * */
    public Client getClient(SocketChannel socketChannel) {
        return OnlineClient.get(socketChannel);
    }
}
