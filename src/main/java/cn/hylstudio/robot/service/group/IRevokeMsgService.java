package cn.hylstudio.robot.service.group;

import cc.moecraft.icq.event.events.message.EventGroupMessage;

public interface IRevokeMsgService {
    void checkRevokeCmd(Long senderId, Long groupId, EventGroupMessage msg);

    String getImageUrl(String imgId);
}
