import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ThreadPoolExecutor;

@SpringBootTest
@RunWith(SpringRunner.class)
public class StarterTest {
    
    @Autowired
    private ThreadPoolExecutor MyThreadPoolExecutor;
    
    @Test
    public void test() {
        System.out.println(MyThreadPoolExecutor.getCorePoolSize());
    }
}
