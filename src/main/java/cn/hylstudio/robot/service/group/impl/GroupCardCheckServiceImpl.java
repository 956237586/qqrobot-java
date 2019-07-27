package cn.hylstudio.robot.service.group.impl;

import cn.hylstudio.robot.entity.GroupCardCheckRecord;
import cn.hylstudio.robot.repo.GroupCardCheckRepo;
import cn.hylstudio.robot.service.group.IGroupCardCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GroupCardCheckServiceImpl implements IGroupCardCheckService {
    private Logger LOGGER = LoggerFactory.getLogger(GroupCardCheckServiceImpl.class);

    private static final String CARD_REGEX = "^[01][0-9][^ ].{1,7} [^ ].{1,30}";
    private static final Pattern CARD_PATTERN = Pattern.compile(CARD_REGEX);
    private static final String KEY_FORMAT = "%s_%s";//groupId_senderId
    private static final Integer DEFAULT_COUNT = 1000;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    private void initDb() {
        jdbcTemplate.update(GroupCardCheckRecord.CREATE_SQL);
        LOGGER.info("db init succ.");
    }

    @Autowired
    private GroupCardCheckRepo groupCardCheckRepo;

    @Override
    public boolean checkGroupMemberCard(Long senderId, Long groupId, String card) {
        Matcher matcher = CARD_PATTERN.matcher(card);
        boolean right = matcher.matches();
        if (right) {
            if ("19专业 真实姓名".equals(card) ||
                    card.endsWith("真实姓名")) {
                right = false;
            }
        }
        return right;
    }

    @Override
    public boolean alreadyChecked(Long groupId, Long senderId) {
        Optional<GroupCardCheckRecord> optional = groupCardCheckRepo.findById(getKey(groupId, senderId));;
        GroupCardCheckRecord groupCardCheckRecord = optional.orElseGet(null);
        if (groupCardCheckRecord == null) {
            return false;
        }
        return groupCardCheckRecord.getCount() > 0;
    }

    @Override
    public void resetCount(Long groupId, Long senderId) {
        GroupCardCheckRecord groupCardCheckRecord = new GroupCardCheckRecord();
        groupCardCheckRecord.setId(getKey(groupId, senderId));
        groupCardCheckRecord.setCount(DEFAULT_COUNT);
        groupCardCheckRepo.save(groupCardCheckRecord);
    }

    @Override
    public void decreaseCount(Long groupId, Long senderId) {
        Optional<GroupCardCheckRecord> optional = groupCardCheckRepo.findById(getKey(groupId, senderId));
        GroupCardCheckRecord groupCardCheckRecord = optional.orElseGet(null);
        if (groupCardCheckRecord == null) {
            return;
        }
        groupCardCheckRecord.decreaseCount();
        groupCardCheckRepo.save(groupCardCheckRecord);
    }

    private String getKey(Long groupId, Long senderId) {
        return String.format(KEY_FORMAT, groupId, senderId);
    }
}
