package site.handglove.labserver.security.custom;

public class LoginUserHelper {
    private static ThreadLocal<String> username = new ThreadLocal<String>();

    public static void setUsername(String _username) {
        username.set(_username);
    }
    public static String getUsername() {
        return username.get();
    }
    public static void removeUsername() {
        username.remove();
    }
}
