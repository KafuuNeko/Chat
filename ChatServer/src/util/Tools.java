package util;

import server.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Base64;
import java.util.Random;

public class Tools {
    /**
     * 随机获取指定长度的密钥
     * @param length 密钥长度
     * @return 返回随机获取的密钥
     * */
    public static byte[] RandomlyGeneratedKey(int length) {
        byte[] key = new byte[length];
        new Random().nextBytes(key);
        return key;
    }

    /**
     * 随机生成令牌
     * */
    public static String RandomlyToken(int length)
    {
        byte[] token_byte = new byte[length];
        new Random().nextBytes(token_byte);
        return Base64.getEncoder().encodeToString(token_byte);
    }

    /**
     * 向指定的SocketChannel发送信息
     * 将信息打包并直接发送给客户端
     *
     * @param   socketChannel   发送信息的通道
     *
     * @param   operation       操作类型序号
     *
     * @param   seq             操作序列号
     *
     * @param   data            将要发送的数据
     * */
    public static void sendData(SocketChannel socketChannel, int operation, int seq, byte[] data) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length + Pack.head_size);
            byteBuffer.put(Pack.makeHead(operation, seq, data.length));
            byteBuffer.put(data);

            byteBuffer.flip();

            socketChannel.write(byteBuffer);
        } catch (Exception e) {
            Log.warn(e.toString());
        }
    }
}
