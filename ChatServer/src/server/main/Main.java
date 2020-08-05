package server.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import server.global.ClientManager;
import server.global.Global;
import server.log.*;

public class Main {

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

    }

}
