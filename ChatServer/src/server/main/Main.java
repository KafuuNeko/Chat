package server.main;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import server.global.ClientManager;
import server.global.Global;
import server.log.*;
import server.util.Pack;
import server.util.Tea;

public class Main implements Pack.IPackProcessor {

    public static void main(String[] args) throws IOException {
        debug();
        init();

        Server.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        //不断循环判断
        while (true) {
            String inData = br.readLine();

            if (Global.ServerStatus == Global.SERVER_STATUS_RUNNING) {
                //当服务器状态为 运行中 时执行的代码
                switch (inData) {
                    case "stop":
                        Server.stop();
                        break;
                    case "exit":
                        Server.stop();
                        Server.selectorloop.ssChannel.close();
                        System.exit(0);
                    case "clientnumber":
                        ServerLog.info("当前服务器客户端数：" + ClientManager.clientNumber());
                        break;
                }
            } else if (Global.ServerStatus == Global.SERVER_STATUS_STOP) {
                //当服务器状态为 停止 时执行的代码
                if (inData.equals("start")) {
                    Server.start();
                } else if (inData.equals("exit")) {
                    Server.selectorloop.ssChannel.close();
                    System.exit(0);
                }
            }

        }

    }

    private static void init() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Server port:");
                Global.SERVER_PORT = Integer.parseInt(br.readLine());
                Server.init();
                break;
            } catch (NumberFormatException ignored) {

            } catch (IOException e) {
                System.out.println("Error:" + e.toString());

            }
        }
    }

    private static void debug() {
        byte[] key = {12, 43, 35, 14, 42, 101, 45, 24, 17, 53, 95, 23, 61, 36, 13, 76};
        byte[] en_result = Tea.encryptByTea("Hello World", key);
        String de_result = Tea.decryptByTea(en_result, key);

        System.out.println("en:" + Arrays.toString(en_result));
        System.out.println("de:" + de_result);


        byte[] con = {-128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128};
        byte[] con_en_res = Tea.encrypt(con, 0, key, 32);
        byte[] con_de_res = Tea.decrypt(con_en_res, 0, key, 32);

        System.out.println("en:" + Arrays.toString(con_en_res));
        System.out.println("de:" + Arrays.toString(con_de_res));

        byte[] headTD1 = "Hello Client".getBytes();

        byte[] head = Pack.makeHead(1001, 1, 1, headTD1.length);
        System.out.println("head:" + Arrays.toString(head));
        Pack.PackHead packHead = Pack.unpackHead(head);
        assert packHead != null;
        System.out.println("head:" + packHead.toString());

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(head);
            byteArrayOutputStream.write(headTD1);
            byteArrayOutputStream.flush();
            System.out.println("data:" + Arrays.toString(byteArrayOutputStream.toByteArray()));
            new Pack(new Main()).disposeBytes(null, byteArrayOutputStream.toByteArray());
        } catch (IOException ignored) {

        }

    }

    @Override
    public void onPackUnpack(SocketChannel socketChannel, Pack.PackHead head, byte[] data) {
        System.out.println(new String(data));
    }
}
