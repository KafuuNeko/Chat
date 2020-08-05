package server.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Pack {

    public final static int head_len = 16;
    public final static byte start_flag = 0xc;
    public final static int version = 0x01;

    public static byte[] makeHead(int op, int seq, int data_len)
    {
        return makeHead(version, op, seq, data_len);
    }

    public static byte[] makeHead(int version, int op, int seq, int data_len) {
        int i = 0;

        byte[] result = new byte[head_len];
        int pack_len = head_len + data_len;

        result[i++] = start_flag;

        //封包总大小
        for (int j = 0; j < 4; ++j) result[i++] = (byte) (pack_len >> ((3 - j) * 8) & 0xFF);

        //数据校验位
        byte verify = (byte) (((data_len % head_len) ^ (op ^ seq)) % 127);
        result[i++] = verify;

        //协议版本
        result[i++] = (byte) (version >> 8);
        result[i++] = (byte) (version);

        //操作码
        for (int j = 0; j < 4; ++j) result[i++] = (byte) (op >> ((3 - j) * 8) & 0xFF);

        //SeqMake
        for (int j = 0; j < 4; ++j) result[i++] = (byte) (seq >> ((3 - j) * 8) & 0xFF);

        return result;
    }

    public static PackHead unpackHead(byte[] head) {
        //第一位是起始标志位
        if (head[0] != start_flag) return null;

        //总长度
        int len = analysis(head, 1, 4);

        //数据校验
        int verify = analysis(head, 5, 1);

        //获取协议版本
        int versions = analysis(head, 6, 2);

        //获取操作码
        int operation = analysis(head, 8, 4);

        //获取操作码
        int seq = analysis(head, 12, 4);

        return new PackHead(versions, operation, len, verify, seq);
    }

    private static int analysis(byte[] data, int offset, int len)
    {
        int result = 0;
        for (int j = 0; j < len; ++j) {
            int temp = data[offset++] & 0xFF;
            result <<= 8;
            result |= temp;
        }
        return result;
    }


    private IPackProcessor mPackProcessor;
    private PackHead mHeadInfo = null;
    private byte[] mHeadBuff = new byte[head_len];
    private int mHeadGetSize = 0;
    private ByteArrayOutputStream mDataBuff = null;
    private int mDataGetSize = 0;

    public Pack(IPackProcessor packProcessor) {
        mPackProcessor = packProcessor;
    }

    synchronized public void disposeBytes(SocketChannel address, byte[] data) throws IOException {
        for (byte bt : data) {
            if (mHeadInfo == null) {
                if (mHeadGetSize == 0 && bt != start_flag) continue;
                //包头信息对象为空，则不断向缓冲区写入字节
                mHeadBuff[mHeadGetSize++] = bt;

                if (mHeadGetSize == head_len) {
                    //如果当前包头缓冲区已被填满，则对包头缓冲区数据执行解包操作
                    mHeadInfo = unpackHead(mHeadBuff);
                    if (mHeadInfo != null) {
                        //验证解包头信息后包头信息是否合法
                        byte verify = (byte) (((mHeadInfo.length % head_len) ^ (mHeadInfo.operation ^ mHeadInfo.seq)) % 127);
                        if (mHeadInfo.length <= head_len || mHeadInfo.verify != verify) {
                            mHeadInfo = null;
                        } else {
                            //包头信息合法 构造包头对象
                            mDataBuff = new ByteArrayOutputStream();
                            mDataGetSize = 0;
                        }
                    }
                    mHeadGetSize = 0;
                }

            } else {
                //包头对象已构造成功
                assert mDataBuff != null;
                mDataBuff.write(bt);

                if (++mDataGetSize == mHeadInfo.length - head_len) {
                    mDataBuff.flush();

                    mPackProcessor.onPackUnpack(address, mHeadInfo, mDataBuff.toByteArray());

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
        public int length;
        public int verify;
        public int seq;

        public PackHead(int versions, int operation, int length, int verify, int seq)
        {
            this.versions = versions;
            this.operation = operation;
            this.length = length;
            this.verify = verify;
            this.seq = seq;
        }

        public String toString()
        {
            return "Version:"+ this.versions+",operation:"+this.operation+",length:"+this.length+",verify:"+this.verify+",seq:"+this.seq;
        }

    }

    public interface IPackProcessor {
        void onPackUnpack(SocketChannel socketChannel, PackHead head, byte[] data);
    }

    public enum Operation {
        FirstContact,
        HeartBeat
    }

    public static Operation getOperation(int index)
    {
        Operation[] ops = Operation.values();
        if (ops.length <= index) return null;
        return ops[index];
    }
}
