package cn.hylstudio.robot.listener;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cn.hylstudio.robot.Constant;
import cn.hylstudio.robot.service.group.IGroupMemberService;
import cn.hylstudio.robot.service.group.IRevokeMsgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class GroupMsgListener extends AbstractListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupMsgListener.class);
    @Value("${robot.qq}")
    private Long robotQQ;
    @Value("${managed.group.id}")
    private Long configuredGroup;
    @Value("${admin.group.id}")
    private Long adminGroup;
    private static String AT_MYSELF;
    @Autowired
    private IGroupMemberService groupMemberInfoService;
    @Autowired
    private IRevokeMsgService revokeMsgService;

    @PostConstruct
    public void init() {
        AT_MYSELF = String.format(Constant.AT, robotQQ);
    }

    @EventHandler
    //	群聊消息
    public void onGroupMsg(EventGroupMessage msg) {
        Long groupId = msg.getGroupId();
        if (!groupId.equals(configuredGroup) && !groupId.equals(adminGroup)) {
            return;
        }
        Long senderId = msg.getSenderId();
        if (senderId.equals(1000000L)) {
            return;//系统消息忽略
        }
        String content = msg.getMessage();
        if (content.contains(AT_MYSELF)) {
            LOGGER.info("at myself groupId=[{}] senderId=[{}] msg=[{}]", groupId, senderId, content);
        }
        if (groupId.equals(configuredGroup)) {
            handleGroupMsg(msg);
            return;
        }
        if (groupId.equals(adminGroup)) {
            handleAdminGroupMsg(msg);
            return;
        }
    }

    //TODO 重构下比较好
    private void handleAdminGroupMsg(EventGroupMessage msg) {
        Long groupId = msg.getGroupId();
        Long senderId = msg.getSenderId();
        String message = msg.getMessage();
        revokeMsgService.checkRevokeCmd(senderId, groupId, msg);
        String[] keywords = {"回复群", "hfq", "回复", "hf",};
        for (String keyword : keywords) {
            if (message.startsWith(keyword)) {
                message = message.substring(keyword.length());
                int position = message.indexOf(" ");
                String replyMsg = message.substring(position + 1);
                if (keyword.equalsIgnoreCase(keywords[0]) || keyword.equalsIgnoreCase(keywords[1])) {
                    msg.getHttpApi().sendGroupMsg(configuredGroup, replyMsg);
                } else if (keyword.equalsIgnoreCase(keywords[2]) || keyword.equalsIgnoreCase(keywords[3])) {
                    String replyQQ = message.substring(0, position);
                    try {
                        Long replyQQId = Long.valueOf(replyQQ);
                        msg.getHttpApi().sendPrivateMsg(replyQQId, replyMsg);
                    } catch (Exception e) {
                        LOGGER.error("parse msg error [{}]", e.getMessage(), e);
                    }
                }
                break;
            }
        }
    }

    private void handleGroupMsg(EventGroupMessage msg) {
        Long senderId = msg.getSenderId();
        Long groupId = msg.getGroupId();
        checkCard(msg, senderId, groupId);
    }

    private void checkCard(EventGroupMessage msg, Long senderId, Long groupId) {
        boolean right = groupMemberInfoService.checkGroupMemberCard(senderId, groupId);
        if (!right) {
            IcqHttpApi httpApi = msg.getHttpApi();
            String result = new MessageBuilder()
                    .add(new ComponentAt(senderId))
                    .add(String.format("(%s)", senderId))
                    .add("名片格式不正确！")
                    .toString();
            httpApi.sendGroupMsg(groupId, result);
        }
    }

}
