package cn.hylstudio.robot;

import cc.moecraft.icq.PicqBotX;
import cc.moecraft.icq.exceptions.HttpServerStartFailedException;
import cc.moecraft.icq.exceptions.VersionIncorrectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class RobotApplication {

    @Autowired
    private EventListenerRegister register;
    private final Logger LOGGER = LoggerFactory.getLogger(RobotApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RobotApplication.class, args);
    }

    @PostConstruct
    public void initRobot() {
        register.init();
        LOGGER.info("robot started");
    }
}
