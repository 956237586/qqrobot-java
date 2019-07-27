package cn.hylstudio.robot.repo.robot;

import cn.hylstudio.robot.entity.robot.GroupCardCheckRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupCardCheckRepo extends JpaRepository<GroupCardCheckRecord, String> {
}
