package site.handglove.labserver.security.custom;

public class LoginUserHelper {
    private static ThreadLocal<String> username = new ThreadLocal<String>();
    private static ThreadLocal<Integer> permissionCode = new ThreadLocal<Integer>();

    public static void setUsername(String _username) {
        username.set(_username);
    }
    public static String getUsername() {
        return username.get();
    }
    public static void removeUsername() {
        username.remove();
    }

    public static void setPermission(Integer permission) {
        permissionCode.set(permission);
    }
    public static Integer getPermission() {
        return permissionCode.get();
    }
    public static void removePermission() {
        permissionCode.remove();
    }
}
