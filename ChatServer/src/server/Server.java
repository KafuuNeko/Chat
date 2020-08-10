package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import server.client.Client;
import server.client.ClientManager;
import server.loop.HeartBeatLoop;
import server.loop.SelectorLoop;

public class Server implements SelectorLoop.IReceivingListener{
    private SelectorLoop mSelectorLoop;
    public ClientManager clientManager;

    /**
     * 服务器状态
     * 默认为挂起状态，当初始化完成后加入运行状态
     * 若运行过程中出现致命错误或用户主动停止则立即进入停止状态
     * 进入停止状态当操作不可逆
     * */
    public enum Status
    {
        SERVER_STATUS_STOP, SERVER_STATUS_RUNNING, SERVER_STATUS_SUSPENDED
    }

    public Status status;

    public Server(int port, int maxClientNumber) throws IOException
    {
        Log.info("正在初始化服务器....");

        clientManager = new ClientManager();

        Log.info("Init SelectorLoop...");
        mSelectorLoop = new SelectorLoop(this, this, port, maxClientNumber);
        mSelectorLoop.start();

        Log.info("Init HeartBeatLoop...");
        new HeartBeatLoop(this).start();

        Log.info("服务器启动成功");

        status = Status.SERVER_STATUS_RUNNING;//置服务器为运行状态
    }

    /**
     * 关闭服务器
     * */
    public void stop() throws IOException {
        status = Status.SERVER_STATUS_STOP;//置服务器为关闭状态
        clientManager.closeAllClient();
        mSelectorLoop.close();
        Log.info("服务器已关闭");
    }

    /**
     * 接收到从客户端传来的信息
     * 将交给指定的客户端数据处理器进行解包处理
     * */
    @Override
    public void onReceiveFromChannel(SocketChannel channel) {
        int count;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            while ((count = channel.read(buffer)) > 0) {
                buffer.flip();//翻转缓冲区

                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                Log.debug(Arrays.toString(bytes));
                buffer.clear();

                //获取指定的客户端数据处理
                Client client = clientManager.getClient(channel);
                if (client != null) client.disposeBytes(channel, bytes);
            }

            //如果是-1则客户端已和服务器多开连接
            if (count < 0) clientManager.closeClient(channel, true);
        } catch (IOException e) {
            Log.warn("SelectorLoop.readFromChannel出现异常：" + e.toString());
        }
    }
}
