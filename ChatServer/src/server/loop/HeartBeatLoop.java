package server.loop;

import server.Server;
import server.Log;

/**
 * 心跳包验证循环
 * 每10秒对所有在线客户端进行一次心跳包验证
 * */
public class HeartBeatLoop extends Thread {
    private Server mServer;

    public HeartBeatLoop(Server server)
    {
        mServer = server;
    }

    @Override
    public void run() {

        try {
            loop:
            while (mServer.status != Server.Status.SERVER_STATUS_STOP) {
                while (mServer.status == Server.Status.SERVER_STATUS_SUSPENDED) sleep(1000);//挂起等待

                //10秒验证一次心跳包，等待期间若服务器关闭则关闭心跳验证
                for (int i = 0; i < 10; ++i)
                {
                    if (mServer.status == Server.Status.SERVER_STATUS_STOP)
                    {
                        break loop;
                    }
                    sleep(1000);
                }

                if (mServer.status != Server.Status.SERVER_STATUS_STOP)
                {
                    Log.debug("CheckHeartBeatLoop...");
                    mServer.clientManager.checkAllHeartBeat();
                }
            }

            Log.info("心跳验证循环已关闭");

        } catch (InterruptedException e) {
            Log.error("心跳包Loop异常：" + e.toString());
            mServer.status = Server.Status.SERVER_STATUS_STOP;
        }
    }
}
