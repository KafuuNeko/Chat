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

    //Channel - Client 映射表
    public Map<SocketChannel, Client> ChannelToClient = new HashMap<>();
    //UID - Channel 映射表
    public Map<Long, SocketChannel> UserToChannel = new HashMap<>();

    /**
     * 获取当前在线的客户端数量
     * */
    public int clientNumber() {
        return ChannelToClient.size();
    }

    /**
     * 添加新的在线客户端
     * */
    public void addClient(Server server, SocketChannel cl) {
        ChannelToClient.put(cl, new Client(server, System.currentTimeMillis()));
    }

    /**
     * 关闭一个客户端
     * @param cl 要关闭的客户端
     * @param remove_map 关闭此客户端后是否立即从Map中移除
     * */
    public void closeClient(SocketChannel cl, boolean remove_map) {
        Client client = ChannelToClient.get(cl);
        try {
            if (client == null) {
                Log.warn("未找到符合的客户端：" + cl.getRemoteAddress());
            } else {
                if (cl.isOpen()) {
                    Log.info("关闭客户端：" + cl.getRemoteAddress());
                    cl.close();
                    if (client.uid != -1) UserToChannel.remove(client.uid);
                }
                if (remove_map) ChannelToClient.remove(cl);
            }
        } catch (IOException e) {
            Log.warn("关闭客户端时发生异常：" + e.toString());
        }
    }

    /**
     * 主动关闭所有在线的客户端
     * */
    public void closeAllClient() {
        for (SocketChannel sc : ChannelToClient.keySet()) closeClient(sc, false);
        ChannelToClient.clear();
    }

    /**
     * 验证所有在线的客户端的心跳包
     * 如果最近的心跳包在60秒之前，则主动断开此客户端的连接
     * */
    public void checkAllHeartBeat() {
        Set<SocketChannel> closed = new HashSet<>();

        long now = System.currentTimeMillis();
        for (SocketChannel sc : ChannelToClient.keySet()) {
            Client client = ChannelToClient.get(sc);
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

        for (SocketChannel sc : closed) ChannelToClient.remove(sc);
    }

    /**
     * 通过SocketChannel获取客户端
     * */
    public Client getClient(SocketChannel socketChannel) {
        return ChannelToClient.get(socketChannel);
    }

    public void updateUserToChannel(long uid, SocketChannel channel)
    {
        UserToChannel.remove(uid);
        UserToChannel.put(uid, channel);
    }
}
