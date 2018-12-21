import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by IntelliJ IDEA.
 * User: Thuyen Phan
 * Date: 06/09/2018
 * Time: 15:38
 */
public class NettyClient {
    private static final int CLIENT_PORT = 8088;
    private static final String host = "localhost";

    public void Run() throws InterruptedException {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap clientBoostrap = new Bootstrap();

        clientBoostrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ClientInitializer())
                .option(ChannelOption.SO_KEEPALIVE, true);
        try {
            ChannelFuture channelFuture = clientBoostrap.connect(host,CLIENT_PORT);
            if (channelFuture.await().isSuccess()) {
                ClientHandler.channelFuture = channelFuture;

                while (true) {
                    if (!ClientHandler.eventlooping) {
                        channelFuture.channel().closeFuture().sync();
                    }
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully().sync();
        }
    }
}
