package util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Tea {
    private static int mDelta = 0x9e3779b9;

    //加密
    private static byte[] encrypt(byte[] content, int offset, byte[] key, int times) {
        int[] tempInt = byteToInt(content, offset);
        int y = tempInt[0], z = tempInt[1], sum = 0, i;
        int[] byte_int = byteToInt(key, 0);

        for (i = 0; i < times; i++) {
            sum += mDelta;
            y += ((z << 4) + byte_int[0]) ^ (z + sum) ^ ((z >> 5) + byte_int[1]);
            z += ((y << 4) + byte_int[2]) ^ (y + sum) ^ ((y >> 5) + byte_int[3]);
        }

        tempInt[0] = y;
        tempInt[1] = z;
        return intToByte(tempInt, 0);
    }

    //解密
    private static byte[] decrypt(byte[] encryptContent, int offset, byte[] key, int times) {
        int[] tempInt = byteToInt(encryptContent, offset);
        int[] byte_int = byteToInt(key, 0);

        int y = tempInt[0], z = tempInt[1], sum, i;

        if (times == 32)
            sum = 0xC6EF3720; /* delta << 5*/
        else if (times == 16)
            sum = 0xE3779B90; /* delta << 4*/
        else
            sum = mDelta * times;

        for (i = 0; i < times; i++) {
            z -= ((y << 4) + byte_int[2]) ^ (y + sum) ^ ((y >> 5) + byte_int[3]);
            y -= ((z << 4) + byte_int[0]) ^ (z + sum) ^ ((z >> 5) + byte_int[1]);
            sum -= mDelta;
        }

        tempInt[0] = y;
        tempInt[1] = z;

        return intToByte(tempInt, 0);
    }

    //byte[]型数据转成int[]型数据
    private static int[] byteToInt(byte[] content, int offset) {

        int[] result = new int[content.length >> 2]; //除以2的n次方 == 右移n位 即 content.length / 4 == content.length >> 2
        for (int i = 0, j = offset; j < content.length; i++, j += 4) {
            result[i] = transform(content[j + 3]) | transform(content[j + 2]) << 8 |
                    transform(content[j + 1]) << 16 | (int) content[j] << 24;
        }
        return result;

    }

    //int[]型数据转成byte[]型数据
    private static byte[] intToByte(int[] content, int offset) {
        byte[] result = new byte[content.length << 2]; //乘以2的n次方 == 左移n位 即 content.length * 4 == content.length << 2
        for (int i = 0, j = offset; j < result.length; i++, j += 4) {
            result[j + 3] = (byte) (content[i] & 0xff);
            result[j + 2] = (byte) ((content[i] >> 8) & 0xff);
            result[j + 1] = (byte) ((content[i] >> 16) & 0xff);
            result[j] = (byte) ((content[i] >> 24) & 0xff);
        }
        return result;
    }

    //若某字节被解释成负的则需将其转成无符号正数
    private static int transform(byte temp) {
        int tempInt = (int) temp;
        if (tempInt < 0) {
            tempInt += 256;
        }
        return tempInt;
    }

    /**
     * TEA加密字符串，默认使用UTF-8编码
     * @param info  待加密的数据
     *
     * @param key   用于加密的16位密钥
     *
     * @return 加密结果
     * */
    public static byte[] encryptByTea(String info, byte[] key) {
        return encryptByTea(info, key, StandardCharsets.UTF_8);
    }

    /**
     * TEA解密数据为字符串，默认使用UTF-8编码
     * @param secretInfo    密文数据
     *
     * @param key           解密密文的16位密钥
     *
     * @return 解密结果，如果解密失败则返回null
     * */
    public static String decryptByTea(byte[] secretInfo, byte[] key) {
        return decryptByTea(secretInfo, key, StandardCharsets.UTF_8);
    }

    /**
     * TEA加密字符串
     * @param info  待加密的数据
     *
     * @param key   用于加密的16位密钥
     *
     * @param charset 字符串编码类型
     *
     * @return 加密结果
     * */
    public static byte[] encryptByTea(String info, byte[] key, Charset charset) {
        byte[] temp = info.getBytes(charset);
        int fill = 8 - temp.length % 8;//若temp的位数不足8的倍数,需要填充的位数
        byte[] encryptStr = new byte[temp.length + fill];
        encryptStr[0] = (byte) fill;
        System.arraycopy(temp, 0, encryptStr, fill, temp.length);
        byte[] result = new byte[encryptStr.length];
        for (int offset = 0; offset < result.length; offset += 8) {
            byte[] tempEncrypt = encrypt(encryptStr, offset, key, 32);
            System.arraycopy(tempEncrypt, 0, result, offset, 8);
        }
        return result;
    }

    /**
     * TEA解密数据为字符串，默认使用UTF-8编码
     * @param secretInfo    密文数据
     *
     * @param key           解密密文的16位密钥
     *
     * @param charset       字符串编码类型
     *
     * @return 解密结果，如果解密失败则返回null
     * */
    public static String decryptByTea(byte[] secretInfo, byte[] key, Charset charset) {
        if (secretInfo.length % 8 != 0) return null;

        byte[] decryptStr = null;
        byte[] tempDecrypt = new byte[secretInfo.length];
        for (int offset = 0; offset < secretInfo.length; offset += 8) {
            decryptStr = decrypt(secretInfo, offset, key, 32);
            System.arraycopy(decryptStr, 0, tempDecrypt, offset, 8);
        }
        int n = tempDecrypt[0];
        return new String(tempDecrypt, n, decryptStr.length - n, charset);
    }
}
