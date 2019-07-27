package cn.hylstudio.robot.service.group;

public interface IGroupCardCheckService {

    boolean checkGroupMemberCard(Long senderId, Long groupId, String card);

    boolean alreadyChecked(Long groupId, Long senderId);

    void resetCount(Long groupId, Long senderId);

    void decreaseCount(Long groupId, Long senderId);
}
