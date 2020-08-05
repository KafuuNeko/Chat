package server.loop;

import server.client.ClientManager;
import server.Definition;
import server.Log;

/**
 * 心跳包验证循环
 * 每10秒对所有在线客户端进行一次心跳包验证
 * */
public class HeartBeatLoop extends Thread {
    @Override
    public void run() {

        try {
            while (Definition.ServerStatus != Definition.SERVER_STATUS_ERROR) {
                while (Definition.ServerStatus == Definition.SERVER_STATUS_SUSPENDED) sleep(1000);//挂起等待

                //10秒验证一次心跳包
                sleep(10000);
                Log.debug("CheckHeartBeatLoop...");
                ClientManager.CheckAllHeartBeat();
            }

        } catch (InterruptedException e) {
            Log.error("心跳包Loop异常：" + e.toString());
        }
    }
}
