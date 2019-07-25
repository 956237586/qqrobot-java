package cn.hylstudio.robot.repo;

import cn.hylstudio.robot.entity.GroupCardCheckRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupCardCheckRepo extends JpaRepository<GroupCardCheckRecord, String> {
}
