package server;

/*
 * 服务器日志类*/

import org.fusesource.jansi.Ansi;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log {

    /**
     * 获取格式化后的当前服务器时间
     * */
    private static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    /**
     * 输出一个调试日志信息到控制台
     * @param msg 日志信息
     * */
    public static void debug(String msg) {
        if (!Definition.IS_DEBUG) return;
        System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(getDateTime()));
        System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" [DEBUG] "));
        System.out.println(Ansi.ansi().fg(Ansi.Color.CYAN).a(msg));
    }

    /**
     * 输出一个标准日志信息到控制台
     * @param msg 日志信息
     * */
    public static void info(String msg) {
        System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(getDateTime()));
        System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" [INFO] "));
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLACK).a(msg));
    }

    /**
     * 输出一个警告日志信息到控制台
     * @param msg 日志信息
     * */
    public static void warn(String msg) {
        System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(getDateTime()));
        System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" [WARN] "));
        System.out.println(Ansi.ansi().fg(Ansi.Color.YELLOW).a(msg));
    }

    /**
     * 输出一个错误日志信息到控制台
     * @param msg 日志信息
     * */
    public static void error(String msg) {
        System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(getDateTime()));
        System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" [ERROR] "));
        System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a(msg));
    }

}
