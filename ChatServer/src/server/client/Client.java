package server.client;

import server.Server;
import server.Processor;
import util.Pack;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Client implements Pack.IPackProcessor {
    //服务器
    public Server server;
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

    public Client(Server server, long lastHeartBeat) {
        this.lastHeartBeat = lastHeartBeat;
        this.server = server;
    }

    /**
     * 解析接收到的数据，若数据已构成一个完整的数据包则回调
     *
     * @param socketChannel 数据来源客户端
     * @param data          待处理的数据
     *                      此数据可能并不是一个完整的数据包
     */
    public void disposeBytes(SocketChannel socketChannel, byte[] data) throws IOException
    {
        mPack.disposeBytes(socketChannel, data);
    }

    /**
     * 解包完成后的回调
     * @param socketChannel 来源客户端
     *
     * @param head          包头信息
     *
     * @param data          包的数据
     * */
    @Override
    public void onPackUnpack(SocketChannel socketChannel, Pack.PackHead head, byte[] data) {
        Processor.Operation operation = Processor.getOperation(head.operation);
        if (operation == null) return;
        //通过操作类型选择包的数据处理器对数据进行处理
        switch (operation) {
            case FirstContact:  //第一次与服务器接触
                Processor.firstContact(server, socketChannel, head, data);
                break;

            case HeartBeat:     //心跳包信息
                Processor.heartBeat(server, socketChannel, head, data);
                break;
        }
    }
}
