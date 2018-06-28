package cn.hylstudio.robot.listener;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.events.message.EventPrivateMessage;
import cc.moecraft.icq.event.events.request.EventFriendRequest;
import cc.moecraft.icq.event.events.request.EventGroupAddRequest;
import cc.moecraft.icq.event.events.request.EventGroupInviteRequest;
import cc.moecraft.icq.sender.returndata.ReturnData;
import cc.moecraft.icq.sender.returndata.returnpojo.send.RMessageReturnData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GroupRequestListener extends AbstractListener {
    @Value("${managed.group.id}")
    private Long configuredGroup;

    @Value("${master.qq}")
    private Long masterQQ;

    @EventHandler
    //拉你入群请求事件
    public void groupRequestHandler(EventGroupInviteRequest req) {
        if (req.getUserId().equals(masterQQ)) {
            req.accept();
        } else {
            req.reject("");
        }
    }

    @EventHandler
    //	加群请求事件
    public void groupRequestHandler(EventGroupAddRequest request) {
        if (request.getGroupId().equals(configuredGroup)) {
            request.accept();
        } else if (request.getGroupId().equals(647283125L)) {
            request.accept();

        }
    }
}
