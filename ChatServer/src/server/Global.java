package server;

public class Global {
    //常量定义处
    public static final boolean IS_DEBUG = true;

    public static final int SERVER_STATUS_STOP = 0;//停止
    public static final int SERVER_STATUS_RUNNING = 1;//运行中
    public static final int SERVER_STATUS_ERROR = 2;//运行错误
    public static final int SERVER_STATUS_SUSPENDED = 3;//挂起，停止后的一种状态，不响应任何操作

    //变量定义处
    public static int SERVER_PORT = 0;
    public static int ServerStatus = SERVER_STATUS_STOP;
}

