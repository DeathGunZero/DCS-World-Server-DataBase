package cn.deathgun.dcsluasocket.service;

import org.springframework.stereotype.Service;

public interface SocketService {

    // 发送给指定ip的用户
    void sendMessageToOne(String ipAndPort, String msg);
}
