import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import letchat.proto.LetChatProtos;
import letchat.proto.LetChatProtos.Response;
import letchat.proto.LetChatProtos.Request;

import java.util.List;
import java.util.Scanner;

/**
 * Created by IntelliJ IDEA.
 * User: Thuyen Phan
 * Date: 06/09/2018
 * Time: 15:39
 */
public class ClientHandler extends SimpleChannelInboundHandler<Response> {
    public static Scanner scanner = new Scanner(System.in);
    public static ChannelFuture channelFuture;
    public static boolean isLogin = false;
    public static boolean eventlooping = true;
    public static String username;
    public static ChatGUI chatGUI;
    public static String token = "";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
        System.out.println("Server response: " + response.getType());
        System.out.println("Message: " + response.getMessage().getMessage());
        switch (response.getType()) {
            case SUCCESS:
                token = response.getMessage().getMessage();
                isLogin = true;
                chatGUI = new ChatGUI(username);
                chatGUI.chatFrame.setVisible(true);
                Login.Loginframe.setVisible(false);
                break;
            case FAILURE:
                System.out.println(response.getMessage().getMessage());
                break;
            case RECEIVE_MESSAGE:
                handleReceiveMessage(response);
                break;
            case CREATECHANNEL_SUCCESS:

                break;
            case GETCHANNEL_SUCCESS:
                handleGetChannels(response);
                break;
            case GETUSERS_SUCCESS:
                handleGetUsers(response);
                break;
            case JOINCHANNEL_SUCCESS:
                break;
            case SIGNUP_FAIL:
                System.out.println(response.getMessage().getMessage());
                break;
            case SIGNUP_SUCCESS:
                System.out.println(response.getMessage().getMessage());
                break;
            case GETMESSAGE_COMPLETE:
                handleGetMessage(response);
                break;
            case GETCHANNEL_MESSAGE_COMPLETE:
                handleGetMessageChannel(response);
                break;
            case AUTH_FAIL:
                LogOut();
                break;
        }
    }

    private void handleGetMessageChannel(Response response) {
        chatGUI.clearMessageArea();
        chatGUI.addAllMessageToArea(response.getMessage().getMessage());
    }

    private void handleGetMessage(Response response) {
        chatGUI.clearMessageArea();
        chatGUI.addAllMessageToArea(response.getMessage().getMessage());
    }

    private void handleGetUsers(Response response) {
        List<String> usersList = response.getUsersList();
        chatGUI.addUsersList(usersList);
    }

    private void handleGetChannels(Response response) {
        List<String> channelsList = response.getChannelsList();
        System.out.println(channelsList.size());
        chatGUI.addChannelsList(channelsList);
    }

    private void handleReceiveMessage(Response response) {

        chatGUI.addMessage(response.getMessage().getMessage(), response.getMessage().getFrom(), response.getMessage().getChannelName());
    }

    public static void LogOut() {
        isLogin = false;
        token = "";
        Login.Loginframe.setVisible(true);
        ChatGUI.chatFrame.setVisible(false);
    }


    public static void getUserMessage(String channel) throws InterruptedException {
        Request request = Request.newBuilder()
                .setType(LetChatProtos.RequestType.GETMESSAGES)
                .setToken(token)
                .setChannel(LetChatProtos.Channel.newBuilder()
                        .setName(channel)
                        .build())
                .build();

        channelFuture.channel().writeAndFlush(request).await();
    }

    public static void getChannelMessage(String channel) throws InterruptedException {
        Request request = Request.newBuilder()
                .setType(LetChatProtos.RequestType.GETCHANNEL_MESSAGE)
                .setToken(token)
                .setChannel(LetChatProtos.Channel.newBuilder()
                        .setName(channel)
                        .build())
                .build();

        channelFuture.channel().writeAndFlush(request).await();
    }


    public static void getListUsers() {
        Request request = Request.newBuilder()
                .setType(LetChatProtos.RequestType.GETUSERS)
                .setToken(token)
                .build();
        channelFuture.channel().writeAndFlush(request);
    }


    public static void createChannel(String channelName) {
        Request request = Request.newBuilder()
                .setType(LetChatProtos.RequestType.CREATECHANNEL)
                .setToken(token)
                .setChannel(LetChatProtos.Channel.newBuilder()
                        .setName(channelName)
                        .build())
                .build();

        channelFuture.channel().writeAndFlush(request);
    }

    public static void joinChannel(String channelName) {
        Request request = Request.newBuilder()
                .setType(LetChatProtos.RequestType.JOINCHANNEL)
                .setToken(token)
                .setChannel(LetChatProtos.Channel.newBuilder()
                        .setName(channelName)
                        .build())
                .build();

        channelFuture.channel().writeAndFlush(request);
    }

    public static void getListChannels() {
        Request request = Request.newBuilder()
                .setType(LetChatProtos.RequestType.GETCHANNELS)
                .setToken(token)
                .build();
        channelFuture.channel().writeAndFlush(request);
    }

    public static void sendMessage(String message, String receiver) {

        Request request = Request.newBuilder()
                .setType(LetChatProtos.RequestType.SENDMESSAGE)
                .setToken(token)
                .setMessage(LetChatProtos.MessageRequest.newBuilder()
                        .setReceiver(receiver)
                        .setMessage(message)
                        .build())
                .build();

        channelFuture.channel().writeAndFlush(request);
    }

    public static void processLogin(String name, String password) throws InterruptedException {
        username = name;
        if (isLogin) {
            System.out.println("You already logged in.");
            return;
        }

        Request request= Request.newBuilder()
                .setType(LetChatProtos.RequestType.LOGIN)
                .setUser(LetChatProtos.User.newBuilder()
                        .setUsername(username)
                        .setPassword(password)
                        .build())
                .build();

        channelFuture.channel().writeAndFlush(request).await();
    }


    public static void processSignUp(String username, String password) throws InterruptedException {
        Request request= Request.newBuilder()
                .setType(LetChatProtos.RequestType.SIGNUP)
                .setUser(LetChatProtos.User.newBuilder()
                        .setUsername(username)
                        .setPassword(password)
                        .build())
                .build();

        channelFuture.channel().writeAndFlush(request).await();
    }
}
