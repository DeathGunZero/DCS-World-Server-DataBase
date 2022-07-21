package cn.deathgun.dcsluasocket.utils;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Slf4j
@ServerEndpoint("/websocket")
public class WebSocket {
    private Session session;

    private static CopyOnWriteArraySet<WebSocket> webSockets = new CopyOnWriteArraySet<>();
    private static Map<String, Session> sessionPool = new HashMap<String, Session>();
    private String userId;

    // 连接开启
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") String userId) {
        System.out.println("userid:" + userId);
        try {
            this.session = session;
            webSockets.add(this);
            sessionPool.put(userId, session);
            log.info("[WebSocket消息]有新的连接, 总数为: " + webSockets.size());
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    // 关闭调用
    @OnClose
    public void onClose() {
        try {
            webSockets.remove(this);
            log.info("[WebSocket消息]连接断开， 总数为: " + webSockets.size());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // 收到消息
    @OnMessage
    public void onMessage(String message) {
        log.info("[WebSocket消息]收到客户端消息: " + message);
        if (Objects.equals(message, "test")) {
            sendAllMessage("你发送了测试消息");
        }
    }

    // 错误
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误: " + this.userId + "原因: " + error.getMessage());
        error.printStackTrace();
    }

    // 广播消息
    public void sendAllMessage(String message) {
        log.info("[WebSocket消息]广播消息: "  + message);
        for (WebSocket webSocket : webSockets) {
            try {
                if (webSocket.session.isOpen()){
                    webSocket.session.getAsyncRemote().sendText(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
