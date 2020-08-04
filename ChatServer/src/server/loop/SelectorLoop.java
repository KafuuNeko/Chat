package server.loop;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import server.global.ClientManager;
import server.global.Global;
import server.log.*;
import server.util.Pack;

public class SelectorLoop extends Thread implements Pack.IPackProcessor {
    public Selector selector;
    public ServerSocketChannel ssChannel;
    private static ByteBuffer buffer = ByteBuffer.allocate(1024);
    private Pack mPack;

    public SelectorLoop() throws IOException {
        selector = Selector.open();

        ssChannel = ServerSocketChannel.open();
        ssChannel.bind(new InetSocketAddress(Global.SERVER_PORT));
        ssChannel.configureBlocking(false);
        ssChannel.register(selector, SelectionKey.OP_ACCEPT);

        mPack = new Pack(this);
    }


    @Override
    public void run() {

        try {
            do {
                while (Global.ServerStatus == Global.SERVER_STATUS_SUSPENDED) super.sleep(1000);//挂起状态

                selector.select();

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    try {
                        if (key.isAcceptable()) {
                            //客户端连接请求
                            if (Global.ServerStatus == Global.SERVER_STATUS_RUNNING) {
                                SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                                ServerLog.info("客户端[" + channel.getRemoteAddress() + "]已连接到服务器");
                                ClientManager.addClient(channel);

                                channel.configureBlocking(false);
                                channel.register(selector, SelectionKey.OP_READ);

                            } else {
                                //如果服务器不处于运行状态，则拒绝所有连接
                                ((ServerSocketChannel) key.channel()).accept().close();
                            }


                        } else if (key.isReadable()) {
                            //客户端数据来源
                            //当服务器处于运行状态时，读取通道中的数据
                            if (Global.ServerStatus == Global.SERVER_STATUS_RUNNING) {
                                SocketChannel channel = (SocketChannel) key.channel();
                                readFromChannel(channel);
                            }
                        }

                    } catch (IOException e) {
                        ServerLog.warn("SelectorLoop线程出现异常1：" + e.toString());
                    }
                    iterator.remove();
                }

            } while (Global.ServerStatus != Global.SERVER_STATUS_ERROR);


        } catch (IOException e) {
            ServerLog.warn("SelectorLoop线程出现异常2：" + e.toString());
        } catch (InterruptedException e) {
            ServerLog.warn("SelectorLoop线程出现异常3：" + e.toString());
        }

        ClientManager.clear();//断开所有连接
        ServerLog.info("接收线程已关闭");

    }

    /*
     * 获取客户端数据*/
    private void readFromChannel(SocketChannel channel) {
        int count;
        buffer.clear();
        try {
            while ((count = channel.read(buffer)) > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);

                mPack.disposeBytes(channel, bytes);
            }
            if (count < 0) {
                ClientManager.closeClient(channel, true);
            }
        } catch (IOException e) {
            ServerLog.warn("SelectorLoop.readFromChannel出现异常：" + e.toString());
        }
    }

    @Override
    public void onPackUnpack(SocketChannel socketChannel, Pack.PackHead head, byte[] data) {
        if (head.operation == 1)
        {
            try {

                ServerLog.debug("[" + socketChannel.getRemoteAddress().toString() + "]lastHeartBeat:" + ClientManager.OnlineClient.get(socketChannel).lastHeartBeat);
            }
            catch (Exception e)
            {

            }
        }
    }
}
