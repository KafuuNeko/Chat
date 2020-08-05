package server.client;

import server.loop.Processor;
import util.Pack;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ClientInfo implements Pack.IPackProcessor {
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

    @Override
    public void onPackUnpack(SocketChannel socketChannel, Pack.PackHead head, byte[] data) {
        Pack.Operation operation = Pack.getOperation(head.operation);
        if (operation == null) return;
        switch (operation) {
            case FirstContact:  //第一次与服务器接触
                Processor.firstContact(socketChannel, head, data);
                break;

            case HeartBeat:     //心跳包信息
                Processor.heartBeat(socketChannel, head, data);
                break;
        }
    }
}
