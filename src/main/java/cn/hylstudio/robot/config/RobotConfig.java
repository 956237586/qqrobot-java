package cn.hylstudio.robot.config;

import cc.moecraft.icq.PicqBotX;
import cc.moecraft.icq.event.EventManager;
import cc.moecraft.icq.sender.IcqHttpApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RobotConfig {
    @Value("${robot.host}")
    private String robotHost;
    @Value("${robot.port}")
    private Integer robotPort;
    @Value("${robot.listen.port}")
    private Integer robotListenPort;
    @Value("${robot.debug}")
    private Boolean debug;

    @Bean
    public PicqBotX getPicqBotX() {
        // 创建机器人对象 ( 信息发送URL, 发送端口, 接收端口, 是否DEBUG )
        PicqBotX bot = new PicqBotX(robotHost, robotPort, robotListenPort, debug);
        return bot;
    }

    @Bean
    public EventManager getEventManager(PicqBotX robot) {
        EventManager eventManager = robot.getEventManager();
        return eventManager;
    }

    @Bean
    public IcqHttpApi getHttpApi(PicqBotX robot) {
        return robot.getHttpApi();
    }
}
