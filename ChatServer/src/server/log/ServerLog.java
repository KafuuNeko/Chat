package server.log;

/*
 * 服务器日志类*/

import org.fusesource.jansi.Ansi;
import server.global.Global;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ServerLog {
    private static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    public static void debug(String msg) {
        if (!Global.IS_DEBUG) return;
        System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(getDateTime()));
        System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" [DEBUG] "));
        System.out.println(Ansi.ansi().fg(Ansi.Color.CYAN).a(msg));
    }

    public static void info(String msg) {
        System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(getDateTime()));
        System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" [INFO] "));
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLACK).a(msg));
    }

    public static void warn(String msg) {
        System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(getDateTime()));
        System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" [WARN] "));
        System.out.println(Ansi.ansi().fg(Ansi.Color.YELLOW).a(msg));
    }

    public static void error(String msg) {
        System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(getDateTime()));
        System.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA).a(" [ERROR] "));
        System.out.println(Ansi.ansi().fg(Ansi.Color.RED).a(msg));
    }

}
