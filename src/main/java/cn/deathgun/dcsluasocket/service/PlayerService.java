package cn.deathgun.dcsluasocket.service;

import cn.deathgun.dcsluasocket.po.Player;
import org.springframework.stereotype.Service;


public interface PlayerService {
    void registeNewPlayer(String ucid, String name);

    Player checkPlayInfoByUcid(String ucid);

    void updatePlayerName(String ucid, String name);

    void addPlayerPts(String ucid, Integer ptsAdd);

    void updatePlayerTracker(String ucid, String tracker);

}
