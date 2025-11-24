package career.exploration.config;

public class DbContextHolder {

    private static final ThreadLocal<String> context = new ThreadLocal<>();

    public static void setDbType(String dbType) {
        context.set(dbType);
    }

    public static String getDbType() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}

