package cn.hylstudio.robot.service.group;

public interface IGroupMemberService {

    String getGroupCard(Long groupId, Long senderId);

    String getGroupCard(Long groupId, Long senderId, boolean useCache);

    boolean checkGroupMemberCard(Long senderId, Long groupId);
}
