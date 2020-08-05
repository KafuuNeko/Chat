package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/*
C++版本包头
struct PackHead {
    uint32_t size;      //数据长度，此包头后面跟着的数据的长度
    uint16_t verify;    //数据校验位
    uint16_t versions;  //协议版本
    uint32_t operation; //操作序号
    uint32_t seq;       //操作序列号，一般用于客户端区分相同操作的不同包
};

如果计算机字节序是则可直接:
PackHead* ptr_head = reinterpret_cast<PackHead*>(head_bytes);
*/

public class Pack {

    //包头尺寸，固定为16
    public final static int head_size = 16;
    //协议版本号
    public final static int version = 0x01;

    public static byte[] makeHead(int operation, int seq, int size) {
        return makeHead(version, operation, seq, size);
    }

    /**
     * 构造包头，包头数据字节序为大端
     *
     * @param version   协议版本
     * @param operation 操作序号，通过Operation.ordinal()可获得
     * @param seq       此操作序列号，一般用于客户端区分相同操作的不同包
     * @param size      数据长度，此包头后面跟着的数据的长度
     * @return 包头数据
     */
    public static byte[] makeHead(int version, int operation, int seq, int size) {
        int i = 0;

        byte[] result = new byte[head_size];
        int pack_len = head_size + size;

        //封包总大小
        for (int j = 0; j < 4; ++j) result[i++] = (byte) (pack_len >> ((3 - j) * 8) & 0xFF);

        //数据校验位
        short verify = (short) (((size % head_size) ^ (operation ^ seq)) % 32767);
        result[i++] = (byte) ((verify >> 8) & 0xFF);
        result[i++] = (byte) ((verify) & 0xFF);

        //协议版本
        result[i++] = (byte) ((version >> 8) & 0xFF);
        result[i++] = (byte) ((version) & 0xFF);

        //操作码
        for (int j = 0; j < 4; ++j) result[i++] = (byte) (operation >> ((3 - j) * 8) & 0xFF);

        //操作序列码
        for (int j = 0; j < 4; ++j) result[i++] = (byte) (seq >> ((3 - j) * 8) & 0xFF);

        return result;
    }

    /**
     * 解包头，包头数据字节序为大端
     *
     * @param head 包头数据
     * @return 返回一个包含此包头数据的对象，可通过数据校验判断是否解析成功
     */
    public static PackHead unpackHead(byte[] head) {

        //总长度
        int size = bytesToInt(head, 0, 4);

        //数据校验
        int verify = bytesToInt(head, 4, 2);

        //获取协议版本
        int versions = bytesToInt(head, 6, 2);

        //获取操作码
        int operation = bytesToInt(head, 8, 4);

        //获取操作码
        int seq = bytesToInt(head, 12, 4);

        return new PackHead(versions, operation, size, verify, seq);
    }

    /**
     * 将字节转换为int型数据，使用大端序
     *
     * @param data   大端序字节数组
     * @param offset 欲转换为Int型数据的字节数组起始位置
     * @param size   参与转换的字节数量
     */
    private static int bytesToInt(byte[] data, int offset, int size) {
        int result = 0;
        for (int j = 0; j < size; ++j) {
            int temp = data[offset++] & 0xFF;
            result <<= 8;
            result |= temp;
        }
        return result;
    }


    //解析成功后使用的回调接口
    private IPackProcessor mPackProcessor;
    //当前解析的包头对象
    private PackHead mHeadInfo = null;
    //包头数据缓冲区
    private byte[] mHeadBuff = new byte[head_size];
    //已填入包头数据缓冲区的字节数量
    private int mHeadGetSize = 0;
    //数据缓冲区
    private ByteArrayOutputStream mDataBuff = null;
    //已加入数据缓冲区的数据数量
    private int mDataGetSize = 0;

    public Pack(IPackProcessor packProcessor) {
        mPackProcessor = packProcessor;
    }

    /**
     * 解析接收到的数据，若数据已构成一个完整的数据包则回调
     *
     * @param socketChannel 数据来源客户端
     * @param data          待处理的数据
     *                      此数据可能并不是一个完整的数据包
     */
    synchronized public void disposeBytes(SocketChannel socketChannel, byte[] data) throws IOException {
        for (byte bt : data) {
            if (mHeadInfo == null) {
                //包头信息对象为空，则不断向缓冲区写入字节
                mHeadBuff[mHeadGetSize++] = bt;

                if (mHeadGetSize == head_size) {
                    //如果当前包头缓冲区已被填满，则对包头缓冲区数据执行解包操作
                    mHeadInfo = unpackHead(mHeadBuff);
                    //验证解包头信息后包头信息是否合法
                    short verify = (short) (((mHeadInfo.size % head_size) ^ (mHeadInfo.operation ^ mHeadInfo.seq)) % 32767);
                    if (mHeadInfo.size < head_size || mHeadInfo.verify != verify) {
                        //包头非法
                        mHeadInfo = null;
                    } else if (mHeadInfo.size == head_size) {
                        //仅含包头，无任何数据，直接调用回调
                        mPackProcessor.onPackUnpack(socketChannel, mHeadInfo, null);
                        mHeadInfo = null;
                    } else {
                        //包头信息合法 构造包头对象
                        mDataBuff = new ByteArrayOutputStream();
                        mDataGetSize = 0;
                    }
                    mHeadGetSize = 0;
                }

            } else {
                //包头对象已构造成功
                assert mDataBuff != null;
                mDataBuff.write(bt);

                if (++mDataGetSize == mHeadInfo.size - head_size) {
                    //数据缓冲区内的数据已经读取完毕
                    mDataBuff.flush();

                    //回调
                    mPackProcessor.onPackUnpack(socketChannel, mHeadInfo, mDataBuff.toByteArray());

                    mHeadInfo = null;
                    mDataBuff.close();
                    mDataBuff = null;
                }

            }
        }
    }


    public static class PackHead {
        public int versions;
        public int operation;
        public int size;
        public int verify;
        public int seq;

        public PackHead(int versions, int operation, int size, int verify, int seq) {
            this.versions = versions;
            this.operation = operation;
            this.size = size;
            this.verify = verify;
            this.seq = seq;
        }

        public String toString() {
            return "Version:" + this.versions + ",operation:" + this.operation + ",size:" + this.size + ",verify:" + this.verify + ",seq:" + this.seq;
        }

    }

    public interface IPackProcessor {
        void onPackUnpack(SocketChannel socketChannel, PackHead head, byte[] data);
    }

    public enum Operation {
        FirstContact,
        HeartBeat
    }

    public static Operation getOperation(int index) {
        Operation[] ops = Operation.values();
        if (ops.length <= index) return null;
        return ops[index];
    }
}
