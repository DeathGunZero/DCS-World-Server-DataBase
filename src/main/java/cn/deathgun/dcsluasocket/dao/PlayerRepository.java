package cn.deathgun.dcsluasocket.dao;

import cn.deathgun.dcsluasocket.po.Player;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
@Transactional
public interface PlayerRepository extends JpaRepository<Player, Integer> {
    @Modifying
    @Query(value = "INSERT INTO player(ucid, name) values(:ucid, :name)", nativeQuery = true)
    void insertNewPlayer(@Param("ucid") String ucid, @Param("name") String name);

    @Query(value = "SELECT * FROM player WHERE  ucid = ?", nativeQuery = true)
    Player selectPlayerByUcId(String ucid);

    @Modifying
    @Query(value = "UPDATE player set name=:name WHERE ucid=:ucid", nativeQuery = true)
    void updatePlayerNameByUcId(@Param("ucid") String ucid, @Param("name") String name);

    @Modifying
    @Query(value = "UPDATE player set pts=pts+:ptsAdd WHERE ucid=:ucid", nativeQuery = true)
    void updatePlayerPtsByUcId(@Param("ucid") String ucid, @Param("ptsAdd") Integer ptsAdd);

    @Modifying
    @Query(value = "UPDATE player SET splashAA=splashAA+1 WHERE ucid=:ucid", nativeQuery = true)
    void plusPlayerAATracker(@Param("ucid") String ucid);

    @Modifying
    @Query(value = "UPDATE player SET splashAG=splashAG+1 WHERE ucid=:ucid", nativeQuery = true)
    void plusPlayerAGTracker(@Param("ucid") String ucid);

    @Modifying
    @Query(value = "UPDATE player SET dead=dead+1 WHERE ucid=:ucid", nativeQuery = true)
    void plusPlayerDeadTracker(@Param("ucid")String ucid);

    @Modifying
    @Query(value = "UPDATE player SET landing=landing+1 WHERE ucid=:ucid", nativeQuery = true)
    void plusPlayerLandingTracker(@Param("ucid") String ucid);

    @Modifying
    @Query(value = "UPDATE player SET splashSEAD=splashSEAD+1 WHERE ucid=:ucid", nativeQuery = true)
    void plusPlayerSEADTracker(@Param("ucid") String ucid);

}
