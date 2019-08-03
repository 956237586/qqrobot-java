package cn.hylstudio.robot.service.group.impl;

import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.MessageComponent;
import cc.moecraft.icq.sender.returndata.ReturnData;
import cc.moecraft.icq.sender.returndata.ReturnStatus;
import cc.moecraft.icq.sender.returndata.returnpojo.send.RMessageReturnData;
import cn.hylstudio.robot.entity.msg.MsgRecord;
import cn.hylstudio.robot.repo.msg.MsgRecordRepo;
import cn.hylstudio.robot.service.group.IRevokeMsgService;
import org.apache.commons.lang3.StringUtils;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RevokeMsgServiceImpl implements IRevokeMsgService {
    private Logger LOGGER = LoggerFactory.getLogger(GroupCardCheckServiceImpl.class);

    private static final String REVOKE_REGEX = "cch\\[CQ:at,qq=(\\d+)] (\\d+) (\\d+)";
    private static final String CQ_FILE_REGEX = ".*\\[CQ:image,file=(.+?)].*";
    private static final String REVOKE_REGEX1 = "cch (\\d+) (\\d+) (\\d+) ?(\\d+)?";
    private static final Pattern REVOKE_PATTERN = Pattern.compile(REVOKE_REGEX);
    private static final Pattern REVOKE_PATTERN1 = Pattern.compile(REVOKE_REGEX1);
    private static final Pattern CQ_FILE_PATTERN = Pattern.compile(CQ_FILE_REGEX);
    @Autowired
    private IcqHttpApi httpApi;

    @Autowired
    private MsgRecordRepo msgRecordRepo;
    @Value("${managed.group.id}")
    private Long configuredGroup;
    @Value("${admin.group.id}")
    private Long adminGroup;

    @Override
    public void checkRevokeCmd(Long senderId, Long groupId, EventGroupMessage msg) {
        String message = msg.getMessage();
        if (StringUtils.isEmpty(message)) {
            return;
        }

        Matcher matcher = REVOKE_PATTERN.matcher(message);
        if (!matcher.matches()) {
            matcher = REVOKE_PATTERN1.matcher(message);
            if (!matcher.matches()) {
                LOGGER.info("msg not match revoke, msg = [{}]", message);
                return;
            }
        }
        LOGGER.info("msg matched revoke, msg = [{}]", message);
        boolean disableCQCode = true;
        String atQQStr = matcher.group(1);
        String numStr = matcher.group(2);
        String limitStr = matcher.group(3);
        String showCqCode = matcher.group(4);
        Long atQQ = 0L;
        Integer num = 0;
        Integer limit = 0;
        try {
            atQQ = Long.valueOf(atQQStr);
            num = Integer.valueOf(numStr);
            limit = Integer.valueOf(limitStr);
        } catch (NumberFormatException e) {
            LOGGER.error("parse cmd error, e = [{}]", e.getMessage(), e);
        }
        if (!StringUtils.isEmpty(showCqCode)) {
            disableCQCode = false;
        }
        if (atQQ == 0L) {
            LOGGER.error("parse cmd error, atQQ = 0");
            return;
        }
        if (num == 0) {
            LOGGER.error("parse cmd error, num = 0");
            return;
        }
        if (limit == 0) {
            LOGGER.error("parse cmd error, limit = 0");
            return;
        }
        if (limit > 5) {
            LOGGER.warn("parse cmd limit > 10, ignore it");
            return;
        }
        if (limit > num) {
            LOGGER.warn("parse cmd limit > num, limit = [{}] num = [{}]", limit, num);
            limit = num;
        }
        //cch@123 3
        LOGGER.info("get revoke qq = [{}], num = [{}], limit = [{}]", atQQ, num, limit);
        String groupIdStr = String.format("qq/group/%s", configuredGroup);
        String uidStr = String.format("qq/user/%s", atQQ);
        List<MsgRecord> recentMsgs = msgRecordRepo.findRecentMsg(groupIdStr, uidStr, num);
        if (CollectionUtils.isEmpty(recentMsgs)) {
            LOGGER.warn("recent msgs is empty");
            httpApi.sendGroupMsg(adminGroup, "查不到最近的消息");
            return;
        }
        int n = recentMsgs.size();
        LOGGER.warn("recent msgs size = [{}]", n);
        if (limit > n) {
            LOGGER.warn("limit > n, limit = [{}], n = [{}]", limit, n);
            limit = n;
        }
        LOGGER.info("disableCQCode = [{}]", disableCQCode);
        for (int i = 0; i < limit; i++) {
            MsgRecord msgRecord = recentMsgs.get(n - 1 - i);
            String content = msgRecord.getContent();
            if (disableCQCode) {
                content = content.replaceAll("\\[", "");
                content = content.replaceAll("]", "");
            } else {
                Matcher fileMatcher = CQ_FILE_PATTERN.matcher(content);
                if (fileMatcher.matches()) {
                    String imgId = fileMatcher.group(1);
                    content = getImageUrl(imgId);
                    sendMsg(getImageRedirectUrl(imgId));
                }
            }
            LOGGER.info("content = [{}]", content);
            if (StringUtils.isEmpty(content)) {
                sendMsg("empty");
            } else {
                sendMsg(content);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private String getImageRedirectUrl(String imgId) {
        return String.format("http://robot.hylstudio.cn/img/%s", imgId);
    }

    @Override
    public String getImageUrl(String imgId) {
        String fileIniName = String.format("D:/coolQPro/data/image/%s.cqimg", imgId);
        Ini ini = null;
        try {
            ini = new Ini(new File(fileIniName));
            return ini.get("image", "url");
        } catch (Exception e) {
            return "http://www.baidu.com";
        }
    }

    private void sendMsg(String msg) {
        ReturnData<RMessageReturnData> result = httpApi.sendGroupMsg(adminGroup, msg);
        if (result == null) {
            LOGGER.warn("result is null");
            return;
        }
        ReturnStatus status = result.getStatus();
        if (status == null) {
            LOGGER.warn("result status is null");
            return;
        }
        LOGGER.info("send status = [{}]", status);
    }

}
