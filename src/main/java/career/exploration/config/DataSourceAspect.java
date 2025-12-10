package career.exploration.config;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
public class DataSourceAspect {

    @Before("@annotation(transactional)")
    public void switchDataSource(Transactional transactional) {
        if (transactional.readOnly()) {
            ReplicationRoutingDataSource.setReader();
        } else {
            ReplicationRoutingDataSource.setWriter();
        }
    }

    @After("@annotation(transactional)")
    public void clearDataSource(Transactional transactional) {
        ReplicationRoutingDataSource.clear();
    }
}

