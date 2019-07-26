package cn.hylstudio.robot.service.group.impl;

import cn.hylstudio.robot.BaseTest;
import cn.hylstudio.robot.entity.GroupCardCheckRecord;
import cn.hylstudio.robot.repo.GroupCardCheckRepo;
import cn.hylstudio.robot.service.group.IGroupCardCheckService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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