package cn.hylstudio.robot.listener;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.events.request.EventFriendRequest;
import cc.moecraft.icq.event.events.request.EventGroupAddRequest;
import cc.moecraft.icq.event.events.request.EventGroupInviteRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FriendRequestListener extends AbstractListener {

    @EventHandler
    //	加好友请求事件
    public void friendRequestHandler(EventFriendRequest request) {
//        request.accept();
        request.reject("");
    }

}
