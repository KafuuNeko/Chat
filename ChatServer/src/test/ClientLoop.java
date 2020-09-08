package test;

import util.Tools;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class ClientLoop extends Thread{
    private SocketChannel mChannel;
    private Selector mSelector;

    public ClientLoop(String address, int port) throws IOException
    {
        //打开通道
        mChannel = SocketChannel.open();
        //通道设置为非阻塞
        mChannel.configureBlocking(false);
        //连接
        mChannel.connect(new InetSocketAddress(address, port));
        //选择器
        mSelector = Selector.open();
        //把通道注册到选择器，监听读事件
        mChannel.register(mSelector, SelectionKey.OP_READ);
    }

    @Override
    public void run() {
        //定义缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            while (!mChannel.finishConnect()) sleep(1000);

            //轮询
            while (mSelector.select() > 0) {
                for (SelectionKey sk : mSelector.selectedKeys())
                {
                    //读事件
                    if (sk.isReadable()) {
                        //SocketChannel channel = (SocketChannel) sk.channel();
                        int len;
                        while ((len = mChannel.read(buffer)) > 0) {
                            buffer.flip();

                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            buffer.clear();

                            System.out.println(Arrays.toString(bytes));

                        }
                    }
                }

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向服务器发送信息
     * 将信息打包并直接发送给客户端
     *
     * @param   operation       操作类型序号
     *
     * @param   seq             操作序列号
     *
     * @param   data            将要发送的数据
     * */
    public void sendData(int operation, int seq, byte[] data) {
        Tools.sendData(mChannel, operation, seq, data);
    }
}
