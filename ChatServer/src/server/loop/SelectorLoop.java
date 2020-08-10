package server.loop;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import server.Server;
import server.Log;

public class SelectorLoop extends Thread {
    private int maxClientNumber;
    private Server mServer;
    private Selector mSelector;
    private ServerSocketChannel mChannel;
    private IReceivingListener mReceivingListener;

    /**
     * 信息处理器构造函数
     * @param receivingListener 收到信息后对回调监听器
     *
     * @param server            服务器对象
     *
     * @param port              要绑定对端口
     * */
    public SelectorLoop(IReceivingListener receivingListener, Server server, int port, int maxClientNumber) throws IOException {
        mSelector = Selector.open();

        mChannel = ServerSocketChannel.open();
        mChannel.bind(new InetSocketAddress(port));
        mChannel.configureBlocking(false);
        mChannel.register(mSelector, SelectionKey.OP_ACCEPT);

        mReceivingListener = receivingListener;
        mServer = server;

        this.maxClientNumber = maxClientNumber;
    }


    @Override
    public void run() {

        try {
            while (mServer.status != Server.Status.SERVER_STATUS_STOP) {
                while (mServer.status == Server.Status.SERVER_STATUS_SUSPENDED) sleep(1000);//挂起状态

                if(mSelector.select() <= 0)
                {
                    if (mServer.status == Server.Status.SERVER_STATUS_STOP) break;
                    if (mServer.status == Server.Status.SERVER_STATUS_RUNNING)
                    {
                        Log.warn("mSelector.select() <= 0");
                        sleep(1000);
                    }
                    continue;
                }

                Set<SelectionKey> selectionKeys = mSelector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    try {
                        if (key.isAcceptable()) {
                            //客户端连接请求
                            if (mServer.status == Server.Status.SERVER_STATUS_RUNNING && mServer.clientManager.clientNumber() < maxClientNumber) {
                                SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                                Log.info("客户端[" + channel.getRemoteAddress() + "]已连接到服务器");
                                mServer.clientManager.addClient(mServer, channel);

                                channel.configureBlocking(false);
                                channel.register(mSelector, SelectionKey.OP_READ);

                            } else {
                                //如果服务器不处于运行状态或客户端数量超限，则拒绝连接
                                ((ServerSocketChannel) key.channel()).accept().close();
                                Log.warn("服务器已连接客户端超限，已拒绝新到客户端连接");
                            }


                        } else if (key.isReadable()) {
                            //客户端数据来源
                            //当服务器处于运行状态时，读取通道中的数据
                            if (mServer.status == Server.Status.SERVER_STATUS_RUNNING) {
                                SocketChannel channel = (SocketChannel) key.channel();
                                mReceivingListener.onReceiveFromChannel(channel);
                            }
                        }

                    } catch (IOException e) {
                        Log.warn("SelectorLoop线程出现异常1：" + e.toString());
                    }
                    iterator.remove();
                }

            }


        } catch (IOException | InterruptedException e) {
            Log.error("SelectorLoop线程出现异常2：" + e.toString());
            mServer.status = Server.Status.SERVER_STATUS_STOP;
        }

        mServer.clientManager.closeAllClient();//断开所有连接
        Log.info("接收线程已关闭");
    }

    public void close() throws IOException
    {
        mSelector.close();
        mChannel.close();
    }

    public interface IReceivingListener
    {
        void onReceiveFromChannel(SocketChannel channel);
    }

}
