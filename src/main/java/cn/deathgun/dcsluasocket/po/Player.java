package cn.deathgun.dcsluasocket.po;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;

@Entity
@Data
@Table(name = "player", schema = "dcs")
public class Player {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "ucid")
    private String ucid;

    @Column(name = "name")
    private String name;

    @Column(name = "pts")
    private Integer pts;

    @Column(name="splashAA")
    private Integer splashAA;

    @Column(name = "splashAG")
    private Integer splashAG;

    @Column(name = "splashSEAD")
    private Integer splashSEAD;

    @Column(name = "dead")
    private Integer dead;

    @Column(name = "landing")
    private Integer landing;
}
