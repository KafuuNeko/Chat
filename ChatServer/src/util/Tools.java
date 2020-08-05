package util;

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
}
