package cn.hylstudio.robot.service.group.impl;

import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.returndata.ReturnData;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import cn.hylstudio.robot.service.group.IGroupCardCheckService;
import cn.hylstudio.robot.service.group.IGroupMemberService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;

@Service
public class GroupMemberServiceImpl implements IGroupMemberService {
    private Logger LOGGER = LoggerFactory.getLogger(GroupMemberServiceImpl.class);
    @Autowired
    private IcqHttpApi httpApi;
    @Autowired
    private IGroupCardCheckService groupCardCheckService;

    @Override
    public String getGroupCard(Long groupId, Long senderId) {
        return getGroupCard(groupId, senderId, false);
    }

    @Override
    public String getGroupCard(Long groupId, Long senderId, boolean useCache) {
        ReturnData<RGroupMemberInfo> groupMemberInfo = httpApi.getGroupMemberInfo(groupId, senderId, !useCache);
        if (groupMemberInfo == null) {
            LOGGER.warn("empty groupMemberInfo, gid = [{}], senderId = [{}]", groupId, senderId);
            return "";
        }

        RGroupMemberInfo data = groupMemberInfo.getData();
        if (data == null) {
            LOGGER.warn("empty member data, gid = [{}], senderId = [{}], groupMemberInfo = [{}]", groupId, senderId, groupMemberInfo);
            return "";
        }
        String card = data.getCard();
        if (StringUtils.isEmpty(card)) {
            LOGGER.warn("empty card data, gid = [{}], senderId = [{}], groupMemberInfo = [{}]", groupId, senderId, groupMemberInfo);
            return "";
        }
        return card;
    }

    @Override
    public boolean checkGroupMemberCard(Long senderId, Long groupId) {
        if (groupCardCheckService.alreadyChecked(groupId, senderId)) {
            groupCardCheckService.decreaseCount(groupId, senderId);
            LOGGER.info("already checked groupId = [{}], senderId = [{}]", groupId, senderId);
            return true;
        }
        String card = getGroupCard(groupId, senderId);
        LOGGER.info("get group card, groupId = [{}], senderId = [{}], card = [{}]", groupId, senderId, card);
        boolean right = groupCardCheckService.checkGroupMemberCard(groupId, senderId, card);
        if (!right) {
            LOGGER.info("not matched card, card = [{}]", card);
        } else {
            LOGGER.info("right card = [{}]", card);
            groupCardCheckService.resetCount(groupId, senderId);
        }
        return right;
    }

}
