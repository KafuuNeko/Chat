package server.util;

import java.util.Random;

public class Tools {
    public static byte[] RandomlyGeneratedKey(int length)
    {
        byte[] key = new byte[length];
        new Random().nextBytes(key);
        return key;
    }
}
