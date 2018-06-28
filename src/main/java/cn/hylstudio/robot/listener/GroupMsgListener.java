package cn.hylstudio.robot.listener;

import cc.moecraft.icq.PicqBotX;
import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.event.events.request.EventGroupAddRequest;
import cc.moecraft.icq.event.events.request.EventGroupInviteRequest;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cc.moecraft.icq.sender.returndata.ReturnData;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class GroupMsgListener extends AbstractListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupMsgListener.class);
    @Autowired
    private IcqHttpApi httpApi;
    @Value("${robot.qq}")
    private Long robotQQ;
    @Value("${managed.group.id}")
    private Long configuredGroup;
    @Value("${admin.group.id}")
    private Long adminGroup;
    public static String AT_MYSELF;
    private LoadingCache<Long, Boolean> cardCheckCache = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.DAYS)
            .build(new CacheLoader<Long, Boolean>() {
                @Override
                public Boolean load(Long senderId) throws Exception {
                    return groupCardCheck(senderId);
                }
            });


    @PostConstruct
    public void init() {
        AT_MYSELF = String.format("[CQ:at,qq=%s]", robotQQ);
    }

    @EventHandler
    //	群聊消息
    public void groupRequestHandler(EventGroupMessage msg) {
        Long groupId = msg.getGroupId();
        if (!groupId.equals(configuredGroup) && !groupId.equals(adminGroup)) {
            return;
        }
        Long senderId = msg.getSenderId();
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

    private void handleAdminGroupMsg(EventGroupMessage msg) {
        String message = msg.getMessage();
        String keyword1 = "回复";
        String keyword2 = "hf";
        if (message.startsWith(keyword1) || message.startsWith(keyword2)) {
            message = message.substring(keyword1.length());
            int position = message.indexOf(" ");
            String replyQQ = message.substring(0, position);
            String replyMsg = message.substring(position + 1);
            Long replyQQId = Long.valueOf(replyQQ);
            msg.getHttpApi().sendPrivateMsg(replyQQId, replyMsg);
        }
    }

    private void handleGroupMsg(EventGroupMessage msg) {
        //check card
        Long senderId = msg.getSenderId();
        Long groupId = msg.getGroupId();
        boolean right = true;
        try {
            right = cardCheckCache.get(senderId);
        } catch (ExecutionException e) {
            LOGGER.error("load cardCheckCache ExecutionException [{}]", e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("load cardCheckCache unknown Exception [{}]", e.getMessage(), e);
        }
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


    private Boolean groupCardCheck(Long senderId) {
        return true;
    }
}
