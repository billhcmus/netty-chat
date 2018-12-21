//import io.netty.channel.*;
//import io.netty.channel.embedded.EmbeddedChannel;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.handler.codec.protobuf.ProtobufDecoder;
//import io.netty.handler.codec.protobuf.ProtobufEncoder;
//import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
//import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
//import junit.framework.TestCase;
//import letchat.proto.LetChatProtos;
//import org.junit.Test;
//import server.ServerHandler;
//
//import java.net.Socket;
//
///**
// * Created by IntelliJ IDEA.
// * User: Thuyen Phan
// * Date: 13/09/2018
// * Time: 22:05
// */
//public class ServerInitializerTest extends TestCase {
//    @Test
//    public void testChannelInitializer() {
//        ServerHandler serverHandler = new ServerHandler();
//
//        ChannelHandler handler = new ChannelInboundHandlerAdapter() {
//            @Override
//            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                ctx.fireChannelRead(1);
//                ctx.fireChannelRead(2);
//            }
//        };
//
//        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInitializer<Channel>() {
//            @Override
//            protected void initChannel(Channel ch) throws Exception {
//                ch.pipeline().addLast(serverHandler);
//            }
//        });
//
//        ChannelPipeline pipeline = channel.pipeline();
//
//        assertSame(serverHandler, pipeline.firstContext().handler());
//    }
//}
