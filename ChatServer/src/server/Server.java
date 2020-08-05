package server;

import java.io.IOException;

import server.loop.SelectorLoop;

public class Server {
    public static SelectorLoop selectorloop;
    public static ClientManager.HeartBeatLoop heartBeatLoop;

    public static void init() throws IOException {
        Global.ServerStatus = Global.SERVER_STATUS_SUSPENDED;//置服务器状态为挂起状态

        Log.info("正在初始化服务器....");

        Log.info("Init SelectorLoop...");
        selectorloop = new SelectorLoop();
        selectorloop.start();

        Log.info("Init HeartBeatLoop...");

        heartBeatLoop = new ClientManager.HeartBeatLoop();
        heartBeatLoop.start();

        Log.info("服务器初始化完成.");
        Global.ServerStatus = Global.SERVER_STATUS_STOP;//置服务器状态为停止状态
    }

    /*
     * 启动服务器*/
    public static void start() throws IOException {
        Global.ServerStatus = Global.SERVER_STATUS_RUNNING;//置服务器为运行状态
        Log.info("服务器已启动");
    }

    /*
     * 关闭服务器*/
    public static void stop() throws IOException {
        Global.ServerStatus = Global.SERVER_STATUS_STOP;//置服务器为关闭状态
        ClientManager.clear();
        Log.info("服务器已关闭");
    }
}
