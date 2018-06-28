package cn.hylstudio.robot.listener;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.events.message.EventPrivateMessage;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cn.hylstudio.robot.service.group.IGroupMemberInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PrivateMsgListener extends AbstractListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateMsgListener.class);


    @Value("${managed.group.id}")
    private Long configuredGroup;
    @Value("${admin.group.id}")
    private Long adminGroup;
    @Value("${master.qq}")
    private Long masterQQ;
    @Autowired
    private IGroupMemberInfoService groupMemberInfoService;

    @EventHandler
    public void debugListener(EventPrivateMessage msg) {
        IcqHttpApi httpApi = msg.getBot().getHttpApi();
        Long senderId = msg.getSenderId();
//        if (senderId.equals(masterQQ)) {
//            ReturnData<RMessageReturnData> response = msg.respond("hi master");
//            return;
//        }
        String message = msg.getMessage();
        String groupCard = groupMemberInfoService.getGroupMemberCard(configuredGroup, senderId);
        String prompt;
        if (groupCard == null) {
            prompt = String.format("来自(%s): ", senderId);
        } else {
            prompt = String.format("来自%s(%s): ", groupCard, senderId);
        }
        String result = new MessageBuilder()
                .add(prompt)
                .add(message)
                .toString();
        httpApi.sendGroupMsg(adminGroup, result);
    }
}
