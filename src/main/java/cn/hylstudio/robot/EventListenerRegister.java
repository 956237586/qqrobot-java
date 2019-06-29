package cn.hylstudio.robot;

import cc.moecraft.icq.PicqBotX;
import cc.moecraft.icq.event.EventManager;
import cc.moecraft.icq.exceptions.VersionIncorrectException;
import cc.moecraft.icq.exceptions.VersionRecommendException;
import cn.hylstudio.robot.listener.*;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
public class EventListenerRegister {
    @Autowired
    private PicqBotX robot;
    @Autowired
    private EventManager eventManager;
    @Autowired
    private PrivateMsgListener privateMsgListener;
    @Autowired
    private FriendRequestListener friendRequestListener;
    @Autowired
    private GroupMemberListener groupMemberListener;
    @Autowired
    private GroupRequestListener groupRequestListener;
    @Autowired
    private GroupMsgListener groupMsgListener;
    private final Logger LOGGER = LoggerFactory.getLogger(EventListenerRegister.class);

    public void reg() {
        eventManager.registerListener(privateMsgListener);
        eventManager.registerListener(groupRequestListener);
        eventManager.registerListener(friendRequestListener);
        eventManager.registerListener(friendRequestListener);
        eventManager.registerListener(groupMsgListener);
        eventManager.registerListener(groupMemberListener);
    }


    public void init() {
        reg();
        LOGGER.error("robot starting");
        // 启动机器人, 这个因为会占用线程, 所以必须放到最后
        robot.startBot();
    }
}
