package cn.hylstudio.robot.repo.msg;

import cn.hylstudio.robot.entity.msg.MsgRecord;
import cn.hylstudio.robot.entity.robot.GroupCardCheckRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MsgRecordRepo extends JpaRepository<MsgRecord, Integer> {
    @Query(value = "select * from event where type = 2 and `group`=?1 and account=?2 order by id desc limit ?3", nativeQuery = true)
    List<MsgRecord> findRecentMsg(String groupId, String uid, Integer num);
}
