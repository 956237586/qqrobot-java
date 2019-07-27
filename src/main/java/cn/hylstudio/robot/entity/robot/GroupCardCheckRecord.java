package cn.hylstudio.robot.entity.robot;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@Entity
@Table(name = "group_card_check")
public class GroupCardCheckRecord {
    public static final String CREATE_SQL = "create table if not exists group_card_check(\n" +
            "  id varchar(50) not null constraint group_card_check_pk primary key,\n" +
            "  `count` int(11) not null default 0\n" +
            ");";
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "`count`")
    private Integer count;

    public void decreaseCount() {
        if (count > 0) {
            this.count--;
        }
    }
}
