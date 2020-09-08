package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static final boolean IS_DEBUG = true;
    private static int mMaxClientNumber = 1000;

    private static Server mServer = null;

    public static void main(String[] args) {
        init();

        try {
            loop();
        } catch (Exception e) {
            Log.error(e.toString());
        }
    }

    /**
     * 初始化服务器
     * 1.等待输入服务器端口号
     * 2.检测端口号是否可用
     * 3.如果端口可用，启动服务器
     * */
    private static void init() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Server port:");
                mServer = new Server(Integer.parseInt(br.readLine()), mMaxClientNumber);
                break;
            } catch (NumberFormatException ignored) {

            } catch (IOException e) {
                Log.error(e.toString());
            }
        }
    }

    /**
     * 检测控制台输入内容，并执行指定操作
     * */
    private static void loop() throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (mServer.status != Server.Status.SERVER_STATUS_STOP) {
            String inData = br.readLine();

            switch (inData) {
                case "stop":
                    mServer.stop();
                    break;
                case "number":
                    Log.info("当前服务器客户端数：" + mServer.clientManager.clientNumber());
                    break;
            }

        }
    }


}
