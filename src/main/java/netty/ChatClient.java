package netty;

import Util.ChatRoom;
import Util.EncryptionUtil;
import Util.MessageUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import netty.initializer.ChatClientInitializer;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient implements Runnable{
    public static ChannelHandlerContext channelHandlerContext;
    private final String host;
    private final int port;

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChatClientInitializer());

            ChannelFuture f = b.connect().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }


    public static void main(String[] args) throws Exception {
        if(args.length!=3){
            System.out.println("It requires 3 parameters,IPAddress,port,roomId");
        }
        System.out.println("Start connecting to server...");
        ChatRoom.INSTANCE.roomId=Integer.valueOf(args[2]);
        ChatRoom.INSTANCE.keyMap = EncryptionUtil.generateRSAKeyMap();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(new ChatClient(args[0], Integer.valueOf(args[1])));
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            String message = scanner.next();
            if(!ChatRoom.INSTANCE.established){
                System.out.println("Server not connected");
                continue;
            }
            if(message.getBytes().length>116){
                System.out.println("Send failed,Message is too long");
                continue;
            }
            new MessageUtil().sendEncryptedStringMessage(ChatRoom.INSTANCE.publicKeyList, (PublicKey) ChatRoom.INSTANCE.keyMap.get("publicKey"),
                    ChatRoom.INSTANCE.localChannel,message);
        }
    }
}