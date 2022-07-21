package cn.deathgun.dcsluasocket.service.impl;

import cn.deathgun.dcsluasocket.dao.PlayerRepository;
import cn.deathgun.dcsluasocket.po.Player;
import cn.deathgun.dcsluasocket.service.PlayerService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service("playerService")
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    private PlayerRepository playerDao;

    @Override
    public void registeNewPlayer(String ucid, String name) {
        playerDao.insertNewPlayer(ucid, name);

    }

    @Override
    public Player checkPlayInfoByUcid(String ucid) {
        return playerDao.selectPlayerByUcId(ucid);
    }

    @Override
    public void updatePlayerName(String ucid, String name) {
        playerDao.updatePlayerNameByUcId(ucid, name);
    }

    @Override
    public void addPlayerPts(String ucid, Integer ptsAdd) {
        playerDao.updatePlayerPtsByUcId(ucid, ptsAdd);
    }

    // 更新玩家计数器
    @Override
    public void updatePlayerTracker(String ucid, String tracker) {
        switch (tracker) {
            case "AA" -> {
                playerDao.plusPlayerAATracker(ucid);
            }
            case "AG" -> {
                playerDao.plusPlayerAGTracker(ucid);
            }
            case "dead" -> {
                playerDao.plusPlayerDeadTracker(ucid);
            }
            case "landing" -> {
                playerDao.plusPlayerLandingTracker(ucid);
            }
            case "SEA" -> {
                playerDao.plusPlayerSEADTracker(ucid);
            }
        }
    }
}
