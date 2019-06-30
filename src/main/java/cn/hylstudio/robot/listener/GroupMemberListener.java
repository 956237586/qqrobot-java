package cn.hylstudio.robot.listener;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.events.notice.groupmember.EventNoticeGroupMemberChange;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GroupMemberListener extends AbstractListener {
    private static final String GROUP_MEMBER_DECREASE = "group_decrease";
    private static final String GROUP_MEMBER_INCREASE = "group_increase";
    @Value("${managed.group.id}")
    private Long configuredGroup;

    @EventHandler
    public void groupMemberHandler(EventNoticeGroupMemberChange msg) {
        Long groupId = msg.getGroupId();
        String noticeType = msg.getNoticeType();
        if (!noticeType.equalsIgnoreCase(GROUP_MEMBER_INCREASE)) {
            return;
        }
        if (groupId.equals(configuredGroup)) {
            Long qq = msg.getUserId();
            IcqHttpApi httpApi = msg.getHttpApi();
            httpApi.setGroupCard(groupId, qq, "19地区 真实姓名");
            String welcomeMsg = new MessageBuilder()
                    .add("欢迎")
                    .add(new ComponentAt(qq))
                    .add(String.format("(%s)", qq))
                    .add("加入本群，请阅读群公告，按格式修改名片，谢谢~~")
                    .toString();
            httpApi.sendGroupMsg(groupId, welcomeMsg);
            httpApi.sendPrivateMsg(qq, "欢迎加入BISTU新生群，请阅读群公告，按格式修改名片，谢谢~~");
        }
    }
}
