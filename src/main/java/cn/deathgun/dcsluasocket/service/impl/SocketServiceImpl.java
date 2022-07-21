package cn.deathgun.dcsluasocket.service.impl;

import cn.deathgun.dcsluasocket.utils.NettyServerHandler;
import cn.deathgun.dcsluasocket.service.SocketService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service("socketService")
public class SocketServiceImpl implements SocketService {

    @Override
    public void sendMessageToOne(String ipAndPort, String msg) {
        NettyServerHandler.channelGroup.forEach(ch -> {
            if(ipAndPort.equals(ch.remoteAddress().toString())){
                ch.writeAndFlush(msg);
                System.err.println("Succ");
            }
        });
    }
}
