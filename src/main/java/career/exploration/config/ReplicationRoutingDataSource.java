package career.exploration.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static List<String> readDataSources;

    public static void setReadDataSources(List<String> readers) {
        readDataSources = readers;
    }

    public static void setWriter() {
        contextHolder.set("writer");
    }

    public static void setReader() {
        if (readDataSources == null || readDataSources.isEmpty()) {
            throw new IllegalStateException("Reader DataSource가 구성되지 않음");
        }
        int index = Math.abs(counter.getAndIncrement() % readDataSources.size());
        contextHolder.set(readDataSources.get(index));
    }

    public static void clear() {
        contextHolder.remove();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return contextHolder.get();
    }
}



