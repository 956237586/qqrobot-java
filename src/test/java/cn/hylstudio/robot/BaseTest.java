package cn.hylstudio.robot;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RobotApplication.class)
public class BaseTest {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);

}
