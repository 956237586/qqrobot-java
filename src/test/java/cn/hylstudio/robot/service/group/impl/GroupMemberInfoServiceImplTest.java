package cn.hylstudio.robot.service.group.impl;

import cc.moecraft.icq.sender.message.MessageBuilder;
import cn.hylstudio.robot.BaseTest;
import cn.hylstudio.robot.entity.msg.MsgRecord;
import cn.hylstudio.robot.entity.robot.GroupCardCheckRecord;
import cn.hylstudio.robot.repo.msg.MsgRecordRepo;
import cn.hylstudio.robot.repo.robot.GroupCardCheckRepo;
import cn.hylstudio.robot.service.group.IGroupCardCheckService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class GroupMemberInfoServiceImplTest extends BaseTest {
    @Autowired
    private GroupCardCheckRepo groupCardCheckRepo;
    @Autowired
    private IGroupCardCheckService groupCardCheckService;

    @Test
    public void test() {
        groupCardCheckService.resetCount(123L, 456L);
        LOGGER.info("debug alreadyChecked = [{}]", groupCardCheckService.alreadyChecked(123L, 456L));
        groupCardCheckService.decreaseCount(123L, 456L);
        Optional<GroupCardCheckRecord> optional = groupCardCheckRepo.findById(String.format("%s_%s", 123L, 456L));
        GroupCardCheckRecord groupCardCheckRecord = optional.orElseGet(null);
        LOGGER.info("debug alreadyChecked = [{}]", groupCardCheckRecord);
        groupCardCheckRepo.delete(groupCardCheckRecord);
    }
}