package cn.hylstudio.robot.listener;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import cc.moecraft.icq.sender.IcqHttpApi;
import cc.moecraft.icq.sender.message.MessageBuilder;
import cc.moecraft.icq.sender.message.components.ComponentAt;
import cc.moecraft.icq.sender.returndata.ReturnData;
import cc.moecraft.icq.sender.returndata.returnpojo.get.RGroupMemberInfo;
import cn.hylstudio.robot.entity.GroupCardCheckRecord;
import cn.hylstudio.robot.repo.GroupCardCheckRepo;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GroupMsgListener extends AbstractListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupMsgListener.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private GroupCardCheckRepo groupCardCheckRepo;
    @Autowired
    private IcqHttpApi httpApi;
    @Value("${robot.qq}")
    private Long robotQQ;
    @Value("${managed.group.id}")
    private Long configuredGroup;
    @Value("${admin.group.id}")
    private Long adminGroup;
    public static String AT_MYSELF;
    private LoadingCache<Pair<Long, Long>, String> cardCache = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.DAYS)
            .build(new CacheLoader<Pair<Long, Long>, String>() {
                @Override
                public String load(Pair<Long, Long> pair) throws Exception {
                    Long groupId = pair.getLeft();
                    Long senderId = pair.getRight();
                    return getGroupCard(groupId, senderId);
                }
            });


    @PostConstruct
    public void init() {
        AT_MYSELF = String.format("[CQ:at,qq=%s]", robotQQ);
        initDb();
//        test();
    }

    private void initDb() {
        jdbcTemplate.update(GroupCardCheckRecord.CREATE_SQL);
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
        String[] keywords = {"回复群", "hfq", "回复", "hf",};
        for (String keyword : keywords) {
            if (message.startsWith(keyword)) {
                message = message.substring(keyword.length());
                int position = message.indexOf(" ");
                String replyMsg = message.substring(position + 1);
                if (keyword.equalsIgnoreCase(keywords[0]) || keyword.equalsIgnoreCase(keywords[1])) {
                    msg.getHttpApi().sendGroupMsg(configuredGroup, replyMsg);
                } else if (keyword.equalsIgnoreCase(keywords[2]) || keyword.equalsIgnoreCase(keywords[3])) {
                    String replyQQ = message.substring(0, position);
                    try {
                        Long replyQQId = Long.valueOf(replyQQ);
                        msg.getHttpApi().sendPrivateMsg(replyQQId, replyMsg);
                    } catch (Exception e) {
                        LOGGER.error("parse msg error [{}]", e.getMessage(), e);
                    }
                }
                break;
            }
        }
    }

    private String regex = "^[01][0-9][^ ].{1,7} [^ ].{1,30}";
    private Pattern pattern = Pattern.compile(regex);

    public void test() {
        resetCount(123L, 456L);
        LOGGER.info("debug alreadyChecked = [{}]", alreadyChecked(123L, 456L));
        decreaseCount(123L, 456L);
        Optional<GroupCardCheckRecord> optional = groupCardCheckRepo.findById(String.format("%s_%s", 123L, 456L));
        GroupCardCheckRecord groupCardCheckRecord = optional.orElseGet(null);
        LOGGER.info("debug alreadyChecked = [{}]", groupCardCheckRecord);
    }

    private void handleGroupMsg(EventGroupMessage msg) {
        //check card
        Long senderId = msg.getSenderId();
        Long groupId = msg.getGroupId();
        if (alreadyChecked(groupId, senderId)) {
            decreaseCount(groupId, senderId);
            return;
        }
        String card = "";
        card = getGroupCard(groupId, senderId);
        Matcher matcher = pattern.matcher(card);
        boolean right = matcher.matches();
        if (right) {
            if ("19专业 真实姓名".equals(card)||
                    card.endsWith("真实姓名")) {
                right = false;
            }
        }
        if (!right) {
            LOGGER.info("not matched card, card = [{}]", card);
            IcqHttpApi httpApi = msg.getHttpApi();
            String result = new MessageBuilder()
                    .add(new ComponentAt(senderId))
                    .add(String.format("(%s)", senderId))
                    .add("名片格式不正确！")
                    .toString();
            httpApi.sendGroupMsg(groupId, result);
        } else {
            LOGGER.info("right card = [{}]", card);
            resetCount(groupId, senderId);
        }
    }


    private boolean alreadyChecked(Long groupId, Long senderId) {
        Optional<GroupCardCheckRecord> optional = groupCardCheckRepo.findById(String.format("%s_%s", groupId, senderId));
        return optional.isPresent();
    }

    private void resetCount(Long groupId, Long senderId) {
        GroupCardCheckRecord groupCardCheckRecord = new GroupCardCheckRecord();
        groupCardCheckRecord.setId(String.format("%s_%s", groupId, senderId));
        groupCardCheckRecord.setCount(1000);
        groupCardCheckRepo.save(groupCardCheckRecord);
    }


    private void decreaseCount(Long groupId, Long senderId) {
        Optional<GroupCardCheckRecord> optional = groupCardCheckRepo.findById(String.format("%s_%s", groupId, senderId));
        GroupCardCheckRecord groupCardCheckRecord = optional.orElseGet(null);
        if (groupCardCheckRecord == null) {
            return;
        }
        groupCardCheckRecord.decreaseCount();
        groupCardCheckRepo.save(groupCardCheckRecord);
    }


    private String getGroupCard(Long groupId, Long senderId) {
        ReturnData<RGroupMemberInfo> groupMemberInfo = httpApi.getGroupMemberInfo(groupId, senderId, true);
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

}
