package cn.deathgun.dcsluasocket.utils;

import cn.deathgun.dcsluasocket.po.Player;
import cn.deathgun.dcsluasocket.service.PlayerService;
import cn.deathgun.dcsluasocket.service.SocketService;
import cn.deathgun.dcsluasocket.service.impl.PlayerServiceImpl;
import cn.deathgun.dcsluasocket.service.impl.SocketServiceImpl;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {
    @Autowired
    private PlayerService playerService;

    @Autowired
    private SocketService socketService;

    //定义一个channle 组，管理所有的channel
    //GlobalEventExecutor.INSTANCE) 是全局的事件执行器，是一个单例
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 有客户端与服务器发生连接时执行此方法
     * 1.打印提示信息
     * 2.将客户端保存到 channelGroup 中
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        System.err.println("A new client is connected: " + channel.remoteAddress());
        channelGroup.add(channel);
    }

    /**
     * 当有客户端与服务器断开连接时执行此方法，此时会自动将此客户端从 channelGroup 中移除
     * 1.打印提示信息
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        System.err.println("Client disconnect. Address: " + channel.remoteAddress());
    }

    /**
     * 表示channel 处于活动状态
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println(ctx.channel().remoteAddress() + " is Alive");
    }

    /**
     * 表示channel 处于不活动状态
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println(ctx.channel().remoteAddress() + " is deactivated");
    }

    /**
     * 读取到客户端发来的数据数据
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception{
        ByteBuf bytemessage = (ByteBuf) message;
        String msg = bytemessage.toString(CharsetUtil.UTF_8);
        //获取到当前channel
        Channel channel = ctx.channel();
        System.out.println(channel.remoteAddress());
        System.err.println("客户端发来了消息: " + channel.remoteAddress() + " 内容:  " + msg);

        // 解析发送的文本

        JSONObject userInput = JSONObject.parseObject(msg);

        // 用户请求类型id
        Integer userRequestId = userInput.getInteger("requestId");

        // 用户名称
        String userName = userInput.getString("name");

        // 唯一标识id
        String ucid = userInput.getString("ucid");

        // 查询数据库是否有该用户
        Player playerInfo = playerService.checkPlayInfoByUcid(ucid);

        switch (userRequestId) {
            // 用户登录 - requestid为0
            case 0 -> {
                // 如果数据库中没有这个玩家就注册
                if (playerInfo == null) {
                    playerService.registeNewPlayer(ucid, userName);
                } else { // 如果数据库有数据
                    // 检验玩家名称是否更改
                    if (!Objects.equals(userName, playerInfo.getName())) {
                        playerService.updatePlayerName(ucid, userName);
                    }
                }
            }

            // 用户查询info - requestid为1
            case 1 -> {
                Player tempPlayer = playerService.checkPlayInfoByUcid(ucid);
                String outputMsg = tempPlayer.getName()
                        + " 你的点数为: " + tempPlayer.getPts()
                        + ". 对空战果: " + tempPlayer.getSplashAA()
                        + ". 对地战果: " + tempPlayer.getSplashAG()
                        + ". 对海战果: " + tempPlayer.getSplashSEAD()
                        + ". 阵亡次数: " + tempPlayer.getDead()
                        + ". 着陆次数: " + tempPlayer.getLanding()
                        + "\n";
                System.out.println("服务器发送了消息: " + outputMsg);
                socketService.sendMessageToOne(String.valueOf(channel.remoteAddress()),
                        outputMsg);
            }

            // 用户分数变动
            case 2 -> {
                Integer ptsAdd = userInput.getInteger("ptsAdd");
                String tracker = userInput.getString("tracker");
                if (tracker != null) {
                    playerService.updatePlayerTracker(ucid, tracker);
                }
                playerService.addPlayerPts(ucid, ptsAdd);

            }
        }

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {

    }


    /**
     * 处理异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error!!!! {}", cause.getMessage());
        //关闭通道
        ctx.close();
    }

}