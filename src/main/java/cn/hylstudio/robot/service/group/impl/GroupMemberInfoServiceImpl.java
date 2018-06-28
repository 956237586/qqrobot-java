package cn.hylstudio.robot.service.group.impl;

import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.returndata.ReturnData;
import cc.moecraft.icq.sender.returndata.ReturnStatus;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import cn.hylstudio.robot.service.group.IGroupMemberInfoService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupMemberInfoServiceImpl implements IGroupMemberInfoService {
    @Autowired
    private IcqHttpApi httpApi;
    private Logger LOGGER = LoggerFactory.getLogger(GroupMemberInfoServiceImpl.class);

    @Override
    public String getGroupMemberCard(Long groupId, Long senderId) {
        ReturnData<RGroupMemberInfo> groupMemberInfo = httpApi.getGroupMemberInfo(groupId, senderId);
        if (ReturnStatus.ok.equals(groupMemberInfo.getStatus())) {
            RGroupMemberInfo data = groupMemberInfo.getData();
            String groupCard = data.getCard();
            if (!StringUtils.isEmpty(groupCard)) {
                return groupCard;
            }
        } else {
            LOGGER.error("get group member info error [{}]", groupMemberInfo);
        }
        return null;
    }
}
