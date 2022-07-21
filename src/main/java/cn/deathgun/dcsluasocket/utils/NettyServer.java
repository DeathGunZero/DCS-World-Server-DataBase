package cn.deathgun.dcsluasocket.utils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringEncoder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NettyServer {
    @Autowired
    private NettyServerHandler serverHandler;

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workGroup = new NioEventLoopGroup();

    /**
     * 启动netty服务
     * @throws InterruptedException
     */
    @PostConstruct
    public void start() throws InterruptedException {
        ServerBootstrap b=new ServerBootstrap();
        b.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,128)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(serverHandler)
                                .addLast(new StringEncoder())//添加字符串编码器
                                .addLast(new ByteArrayEncoder());//添加字节数组的编码器
                    }
                });
        ChannelFuture future = b.bind(23333).sync();
        if (future.isSuccess()) {
            System.out.println("启动 Netty 成功");
        }
    }
    /**
     * 销毁
     */
    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workGroup.shutdownGracefully().syncUninterruptibly();
        System.out.println("关闭 Netty 成功");
    }
}