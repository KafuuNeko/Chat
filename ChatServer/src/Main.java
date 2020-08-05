import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import server.ClientManager;
import server.Global;
import server.Server;
import util.Pack;
import server.Log;

public class Main {

    public static void main(String[] args) throws IOException {
        if (Global.IS_DEBUG) debug();
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
                    case "number":
                        Log.info("当前服务器客户端数：" + ClientManager.clientNumber());
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
        byte[] data = "{'device_name':'Windows', 'heart_beat_verify':'Hello heart beat'}".getBytes(StandardCharsets.UTF_8);

        byte[] head_bytes = Pack.makeHead(Pack.Operation.FirstContact.ordinal(), 10, data.length);
        Pack.PackHead head_object = Pack.unpackHead(head_bytes);
        Log.debug("head_bytes:" + Arrays.toString(head_bytes));
        Log.debug("head_object:" + head_object.toString());

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(head_bytes);
            byteArrayOutputStream.write(data);
            byteArrayOutputStream.flush();
            Log.debug("pack:" + Arrays.toString(byteArrayOutputStream.toByteArray()));
        } catch (Exception ignored) {

        }

    }

}
