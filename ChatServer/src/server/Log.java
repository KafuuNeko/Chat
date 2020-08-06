package server;

/*
 * 服务器日志类*/

import org.fusesource.jansi.Ansi;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log {
    private static final boolean isWindows = System.getProperty("os.name").contains("Windows");

    /**
     * 获取格式化后的当前服务器时间
     */
    private static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    /**
     * 输出一个调试日志信息到控制台
     *
     * @param msg 日志信息
     */
    public static void debug(String msg) {
        if (!Definition.IS_DEBUG) return;
        if (isWindows) {
            System.out.println(getDateTime() + " [DEBUG] " + msg);
        } else {
            System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(getDateTime()).reset());
            System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" [DEBUG] ").reset());
            System.out.println(Ansi.ansi().fg(Ansi.Color.CYAN).a(msg).reset());
        }
    }

    /**
     * 输出一个标准日志信息到控制台
     *
     * @param msg 日志信息
     */
    public static void info(String msg) {
        if (isWindows) {
            System.out.println(getDateTime() + " [INFO] " + msg);
        } else {
            System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(getDateTime()).reset());
            System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" [INFO] ").reset());
            System.out.println(Ansi.ansi().fg(Ansi.Color.DEFAULT).a(msg).reset());
        }
    }

    /**
     * 输出一个警告日志信息到控制台
     *
     * @param msg 日志信息
     */
    public static void warn(String msg) {
        if (isWindows) {
            System.out.println(getDateTime() + " [WARN] " + msg);
        } else {
            System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(getDateTime()).reset());
            System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" [WARN] ").reset());
            System.out.println(Ansi.ansi().fg(Ansi.Color.YELLOW).a(msg).reset());
        }
    }

    /**
     * 输出一个错误日志信息到控制台
     *
     * @param msg 日志信息
     */
    public static void error(String msg) {
        if (isWindows) {
            System.out.println(getDateTime() + " [ERROR] " + msg);
        } else {
            System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(getDateTime()).reset());
            System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" [ERROR] ").reset());
            System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a(msg).reset());
        }
    }

}
