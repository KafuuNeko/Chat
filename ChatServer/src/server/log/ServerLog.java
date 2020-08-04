package server.log;

/*
 * 服务器日志类*/

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ServerLog {
    private static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    public static void debug(String msg) {

        System.out.println(getDateTime() + " [DEBUG] " + msg);
    }

    public static void info(String msg) {
        System.out.println(getDateTime() + " [info] " + msg);
    }

    public static void warn(String msg) {
        System.out.println(getDateTime() + " [warn] " + msg);
    }

    public static void error(String msg) {
        System.out.println(getDateTime() + " [error] " + msg);
    }

}
