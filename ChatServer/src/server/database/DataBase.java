package server.database;

public abstract class DataBase {

    /**
     * 通过用户ID或用户名+密码匹配用户
     *
     * @param user      用户名或用户id
     *
     * @param password  密码（MD5）
     *
     * @return 若匹配成功，则返回匹配到的用户ID，否者返回-1
     * */
    public abstract long matchUser(String user, String password);

}
