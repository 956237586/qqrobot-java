package cn.hylstudio.robot.entity.msg;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@Entity
@Table(name = "event")
public class MsgRecord {
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "`type`")
    private Integer type;
    @Column(name = "`tag`")
    private String tag;
    @Column(name = "`group`")
    private String group;
    @Column(name = "`account`")
    private String account;
    @Column(name = "`content`")
    private String content;
    @Column(name = "`operator`")
    private String operator;
    @Column(name = "`time`")
    private Long time;
    @Column(name = "`mark`")
    private String mark;
//    content BLOB,
//    extra BLOB,
//    proto_extra BLOB,
}
