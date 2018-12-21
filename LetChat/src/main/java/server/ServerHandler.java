package server;

import Storage.*;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import letchat.proto.LetChatProtos;
import letchat.proto.LetChatProtos.Response;
import letchat.proto.LetChatProtos.Request;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA.
 * User: Thuyen Phan
 * Date: 05/09/2018
 * Time: 22:31
 */
public class ServerHandler extends SimpleChannelInboundHandler<Request> {

    final static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    static ConcurrentMap<ChannelId, String> IdUser = new ConcurrentHashMap<>();
    static ConcurrentMap<String, ChannelId> UserId = new ConcurrentHashMap<>();
    static ConcurrentMap<String, Set<String>> listChannel = new ConcurrentHashMap<>();
    static RocksDBStorage storage = new RocksDBStorage("/home/lap11852/testRockDB");
    static Authentication auth = new Authentication();
    private String username;


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        Channel incomingChannel = ctx.channel();
        channels.add(incomingChannel);
        System.out.println("[Server] - " + incomingChannel.remoteAddress() + " has joined.");
        System.out.println("ID: " + ctx.channel().id());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        System.out.println("Receive from: " + ctx.channel().remoteAddress());
        System.out.println("Server received: " + request.getType());

        LetChatProtos.RequestType reqType = request.getType();
        if (reqType != LetChatProtos.RequestType.LOGIN
                && reqType != LetChatProtos.RequestType.SIGNUP
                && reqType != LetChatProtos.RequestType.LOGOUT) {
            if (!checkAuth(request)) {
                Response response = Response.newBuilder()
                        .setType(LetChatProtos.ResponseType.AUTH_FAIL)
                        .setMessage(LetChatProtos.MessageResponse.newBuilder()
                                .setMessage("Please login again.")
                                .build())
                        .build();
                ctx.writeAndFlush(response);
                return;
            }
        }
        switch (request.getType()) {
            case LOGIN:
                processLogin(request, ctx);
                break;
            case SIGNUP:
                processSignUp(request, ctx);
                break;
            case SENDMESSAGE:
                processSendMessage(request, ctx);
                break;
            case CREATECHANNEL:
                processCreateChannel(request, ctx);
                break;
            case JOINCHANNEL:
                processJoinChannel(request, ctx);
                break;
            case GETCHANNELS:
                processGetChannels(request, ctx);
                break;
            case GETUSERS:
                processGetUsers(ctx);
                break;
            case GETMESSAGES:
                processGetMessages(request, ctx);
                break;
            case GETCHANNEL_MESSAGE:
                processGetChannelMessage(request, ctx);
                break;
        }
    }

    private boolean checkAuth(Request request) {
        String token = request.getToken();
        return auth.verifyToken(token) && this.username.equals(Authentication.username);
    }

    private void processGetChannelMessage(Request request, ChannelHandlerContext ctx) throws InterruptedException {
        String channelInfo = request.getChannel().getName();
        String numMessageId = String.format("%s.messagecount", channelInfo);
        int numMessage = 0;
        if (storage.contains(numMessageId)) {
            numMessage = Integer.parseInt(storage.get(numMessageId));
        }

        String messageResponse = "";
        for (int i = 1; i <= numMessage; ++i) {
            String messageId = String.format("%s.message%d", channelInfo, i);
            if (storage.contains(messageId)) {
                String content = storage.get(messageId);

                String [] contentPart = content.split("\\.");
                String from = contentPart[0].split(":")[1];
                String message = contentPart[1].split(":")[1];

                messageResponse = messageResponse + from + ": " + message + "\n";
            }
        }
        Response response = Response.newBuilder()
                .setType(LetChatProtos.ResponseType.GETCHANNEL_MESSAGE_COMPLETE)
                .setMessage(LetChatProtos.MessageResponse.newBuilder()
                        .setMessage(messageResponse)
                        .build())
                .build();

        ctx.writeAndFlush(response).await();
    }

    private void processGetMessages(Request request, ChannelHandlerContext ctx) throws InterruptedException {
        // from user-user
        String receiver = request.getChannel().getName();
        String from = IdUser.get(ctx.channel().id());

        String id = String.format("%s:%s", from, receiver);
        String alt_id = String.format("%s:%s", receiver, from);
        String total_msg_id = "";

        if (storage.contains(id)) {
            total_msg_id = id;
        } else if (storage.contains(alt_id)) {
            total_msg_id = alt_id;
        }


        int total_msg = 0;
        if (storage.contains(total_msg_id)) {
            total_msg = Integer.parseInt(storage.get(total_msg_id));
        }

        String messageResponse = "";
        for (int i = 1; i <= total_msg; ++i) {
            String msg_id = total_msg_id + ":" + i;
            if (storage.contains(msg_id)) {
                String content = storage.get(msg_id);
                String [] contentPart = content.split("\\.", 2);
                String fr = contentPart[0].split(":", 2)[1];
                String msg = contentPart[1].split(":", 2)[1];
                messageResponse += fr + ": " + msg + "\n";
            }
        }

        Response response = Response.newBuilder()
                .setType(LetChatProtos.ResponseType.GETMESSAGE_COMPLETE)
                .setMessage(LetChatProtos.MessageResponse.newBuilder()
                        .setMessage(messageResponse)
                        .build())
                .build();

        ctx.writeAndFlush(response).await();
    }


    private void processGetUsers(ChannelHandlerContext ctx) throws InterruptedException {
        Response.Builder responseBuilder = Response.newBuilder();
        responseBuilder.setType(LetChatProtos.ResponseType.GETUSERS_SUCCESS);
        for (ChannelId id : IdUser.keySet()) {
            String user = IdUser.get(id);
            if (!user.equalsIgnoreCase(username)) {
                responseBuilder.addUsers(user);
            }
        }
        Response response = responseBuilder.build();
        ctx.writeAndFlush(response).await();
    }

    private void processGetChannels(Request request, ChannelHandlerContext ctx) throws InterruptedException {
        loadUserChannelsFromDB(username);

        Response.Builder responseBuilder = Response.newBuilder();
        responseBuilder.setType(LetChatProtos.ResponseType.GETCHANNEL_SUCCESS);
        for (Map.Entry<String, Set<String>> entry : listChannel.entrySet()) {
            if (entry.getValue().contains(username)) {
                responseBuilder.addChannels(entry.getKey());
            }
        }
        Response response = responseBuilder.build();
        ctx.writeAndFlush(response).await();
    }

    private void processJoinChannel(Request request, ChannelHandlerContext ctx) throws InterruptedException {
        Response.Builder responseBuilder = Response.newBuilder();
        String channelName = request.getChannel().getName();
        String allChannelsId = String.format("all.channels");

        if (storage.contains(allChannelsId)) {
            String AllChannels = storage.get(allChannelsId);
            String [] tmpChannels = AllChannels.split(":");
            String channelInfo = "";
            String channelOwner = "";
            for (String info : tmpChannels) {
                if (info.contains(channelName)) {
                    channelOwner = info.split("\\.")[1];
                    channelInfo = info;
                    break;
                }
            }

            Set<String> members = listChannel.get(channelInfo);


            String myChannelsId = String.format("%s.my.channels", username);
            String myListChannels = "";
            if (storage.contains(myChannelsId)) {
                myListChannels = storage.get(myChannelsId);
            }

            if (!myListChannels.contains(channelName)) {
                // add to current channel
                storage.remove(myChannelsId);
                myListChannels = myListChannels + ":" + channelInfo;
                storage.put(myChannelsId, myListChannels);

                // add current user to member channel
                String channelMembersId = String.format("%s.%s.members", channelName, channelOwner);
                String mem = storage.get(channelMembersId);
                mem = mem + ":" + username;

                for (String m : mem.split(":")) {
                    String key = String.format("%s.%s.members", channelName, m);
                    storage.remove(key);
                    storage.put(key, mem);
                }

                members.add(username);

                Response response = responseBuilder
                        .setType(LetChatProtos.ResponseType.JOINCHANNEL_SUCCESS)
                        .setMessage(LetChatProtos.MessageResponse.newBuilder()
                                .setMessage("Join channel successfully.")
                        )
                        .build();
                ctx.writeAndFlush(response).await();
            } else {
                Response response = responseBuilder
                        .setType(LetChatProtos.ResponseType.JOINCHANNEL_FAIL)
                        .setMessage(LetChatProtos.MessageResponse.newBuilder()
                                .setMessage("Channel is already joined.")
                        )
                        .build();
                ctx.writeAndFlush(response).await();
            }
        } else {

        }
    }

    private void processCreateChannel(Request request, ChannelHandlerContext ctx) throws InterruptedException {
        String channelName = request.getChannel().getName();
        String myChannelsId = String.format("%s.my.channels", username);

        // add channel to my channel
        if (storage.contains(myChannelsId)) {
            String listChannels = storage.get(myChannelsId);
            if (listChannels.contains(channelName)) {
                Response response = Response.newBuilder()
                        .setType(LetChatProtos.ResponseType.CREATECHANNEL_FAIL)
                        .setMessage(LetChatProtos.MessageResponse.newBuilder()
                                .setMessage("Create channel fail.")
                                .build())
                        .build();

                ctx.writeAndFlush(response).await();
                return;
            } else {
                String value = String.format("%s.%s", channelName, username);
                listChannels = listChannels + ":" + value;
                storage.put(myChannelsId, listChannels);
            }
        } else {
            String value = String.format("%s.%s", channelName, username);
            storage.put(myChannelsId, value);
        }

        // add member to my channel
        String channelMemberId = String.format("%s.%s.members", channelName, username);
        storage.put(channelMemberId, username);


        // add channel to list of all channels
        String allChannels = String.format("all.channels");
        if (storage.contains(allChannels)) {
            String listChannels = storage.get(allChannels);
            listChannels = listChannels + ":" + String.format("%s.%s", channelName, username);
            storage.remove(allChannels);
            storage.put(allChannels, listChannels);
        } else {
            storage.put(allChannels, String.format("%s.%s", channelName, username));
        }

        // add channel message
        String numMessageId = String.format("%s.%s.messagecount", channelName, username);
        storage.put(numMessageId, String.valueOf(0));

        // cache
        String keyListChannelCached = String.format("%s.%s", channelName, username);
        Set<String> members = listChannel.get(keyListChannelCached);
        if (members == null) {
            members = ConcurrentHashMap.newKeySet();
            listChannel.put(keyListChannelCached, members);
        }
        members.add(username);

        Response.Builder responseBuilder = Response.newBuilder();
        Response response = responseBuilder.setType(LetChatProtos.ResponseType.CREATECHANNEL_SUCCESS)
                .setMessage(LetChatProtos.MessageResponse.newBuilder()
                        .setMessage("Create channel completed.")
                )
                .build();

        ctx.writeAndFlush(response).await();
    }

    private void processSendMessage(Request request, ChannelHandlerContext ctx) throws InterruptedException {
        String receiver = request.getMessage().getReceiver();
        String message = request.getMessage().getMessage();
        String from = IdUser.get(ctx.channel().id());
        // neu la group
        if (listChannel.get(receiver) != null) {

            String numMessageId = String.format("%s.messagecount", receiver);

            int count = 1;
            if (storage.contains(numMessageId)) {
                count = Integer.parseInt(storage.get(numMessageId)) + 1;
                storage.remove(numMessageId);
            }
            storage.put(numMessageId, String.valueOf(count));

            String messageId = String.format("%s.message%d", receiver, count);
            String messageContent = String.format("from:%s.message:%s", from, message);
            storage.put(messageId, messageContent);

            Set<String> members = listChannel.get(receiver);
            for (String user : members) {
                if (!user.equalsIgnoreCase(username)) {
                    ChannelId memberId = UserId.get(user);
                    if (memberId != null) { // user online
                        Channel receiverChannel = channels.find(UserId.get(user));
                        Response response = Response.newBuilder()
                                .setType(LetChatProtos.ResponseType.RECEIVE_MESSAGE)
                                .setMessage(LetChatProtos.MessageResponse.newBuilder()
                                        .setFrom(from)
                                        .setChannelName(receiver)
                                        .setMessage(message).build())
                                .build();
                        if (receiverChannel != null) {
                            receiverChannel.writeAndFlush(response).await();
                        }
                    }
                    else {
                        // do something
                    }
                }
            }

        } else {
            ChannelId userId = UserId.get(receiver);

            if (userId == null) {
                return;
            }

            Channel receiverChannel = channels.find(userId);

            // total message
            /*
            id,
            total_message
            */
            String id = String.format("%s:%s", from, receiver);
            String alt_id = String.format("%s:%s", receiver, from);
            String total_msg_id = "";

            if (!storage.contains(id) && !storage.contains(alt_id)) {
                total_msg_id = id;
            } else if (storage.contains(id)) {
                total_msg_id = id;
            } else if (storage.contains(alt_id)) {
                total_msg_id = alt_id;
            }

            int total_message = 1;
            if (storage.contains(total_msg_id)) {
                total_message = Integer.parseInt(storage.get(total_msg_id)) + 1;
                storage.remove(total_msg_id);
            }
            storage.put(total_msg_id, String.valueOf(total_message));

            // message
            /*
            id,
            message_text,
            from
            */

            String msg_id = total_msg_id + ":" + total_message;
            String content = String.format("from:%s.message:%s", from, message);
            this.storage.put(msg_id, content);

            Response.Builder responseBuilder = Response.newBuilder();
            Response response = responseBuilder.setType(LetChatProtos.ResponseType.RECEIVE_MESSAGE)
                    .setMessage(LetChatProtos.MessageResponse.newBuilder()
                            .setFrom(from)
                            .setChannelName("")
                            .setMessage(message)
                            .build())
                    .build();

            receiverChannel.writeAndFlush(response).await();
        }
    }

    private void processSignUp(Request request, ChannelHandlerContext ctx) throws InterruptedException {
        String userName = request.getUser().getUsername();
        String passWord = request.getUser().getPassword();

        if (this.storage.contains(userName)) {
            Response response = Response.newBuilder()
                    .setType(LetChatProtos.ResponseType.SIGNUP_FAIL)
                    .setMessage(LetChatProtos.MessageResponse.newBuilder()
                            .setMessage("Username was duplicated.")
                            .build())
                    .build();
            ctx.writeAndFlush(response).await();
        } else {
            this.storage.put(userName, passWord);
            Response response = Response.newBuilder()
                    .setType(LetChatProtos.ResponseType.SIGNUP_SUCCESS)
                    .setMessage(LetChatProtos.MessageResponse.newBuilder()
                            .setMessage("Sign Up Successfully.")
                            .build())
                    .build();
            ctx.writeAndFlush(response).await();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        Channel outcomingChannel = ctx.channel();
        System.out.println(ctx.channel().remoteAddress() + " was outed.");
        channels.remove(outcomingChannel);
    }

    public void processLogin(Request request, ChannelHandlerContext ctx) throws InterruptedException {

        String username = request.getUser().getUsername();
        this.username = username;
        String password = request.getUser().getPassword();
        loadUserChannelsFromDB(username);

        if (password != null && password.equals(this.storage.get(username))) {
            if (UserId.containsKey(username)) {
                this.IdUser.remove(this.UserId.get(username));
            }
            this.IdUser.put(ctx.channel().id(), username);
            this.UserId.put(username, ctx.channel().id());
            String token = auth.createToken(username, 86400000);
            Response.Builder responseBuilder = Response.newBuilder();
            responseBuilder.setType(LetChatProtos.ResponseType.SUCCESS);
            responseBuilder.setMessage(LetChatProtos.MessageResponse.newBuilder()
                    .setMessage(token)
                    .build());
            Response response = responseBuilder.build();
            ctx.writeAndFlush(response).await();
        } else {
            Response.Builder responseBuilder = Response.newBuilder();
            responseBuilder.setType(LetChatProtos.ResponseType.FAILURE)
                    .setMessage(LetChatProtos.MessageResponse.newBuilder()
                            .setMessage("Wrong username or password.")
                            .build());
            Response response = responseBuilder.build();
            ctx.writeAndFlush(response).await();
        }
    }

    public void loadUserChannelsFromDB(String username) {
        // load channels's user
        String myChannelsId = String.format("%s.my.channels", username);
        if (storage.contains(myChannelsId)) {
            String listChannels = storage.get(myChannelsId);
            String [] channelInfo = listChannels.split(":");

            for (String info : channelInfo) {
                String channelName = info.split("\\.")[0];
                String channelId = String.format("%s.%s.members", channelName, username);
                if (storage.contains(channelId)) {
                    Set<String> members = ConcurrentHashMap.newKeySet();
                    for (String memberName : storage.get(channelId).split(":")) {
                        members.add(memberName);
                    }

                    listChannel.put(info, members);
                }
            }
        }
    }

}