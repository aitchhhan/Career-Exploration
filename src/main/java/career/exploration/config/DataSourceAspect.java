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
            ReplicationRoutingDataSource.setReader(); // Reader DB 선택 (Round-Robin)
        } else {
            ReplicationRoutingDataSource.setWriter(); // Writer DB 선택
        }
    }

    @After("@annotation(transactional)")
    public void clearDataSource(Transactional transactional) {
        ReplicationRoutingDataSource.clear();
    }
}

