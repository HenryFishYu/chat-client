package netty.handler;

import Util.ChatRoom;
import Util.EncryptionUtil;
import Util.Message;
import Util.MessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        ChatRoom.INSTANCE.localChannel = channelHandlerContext.channel();
        int messageType = byteBuf.readByte();
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        switch (messageType){
            case 1: // User joined the chat
                byte[] joinPublicKeyBytes = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(joinPublicKeyBytes);
                PublicKey publicKey = EncryptionUtil.getPublicKeyFromBytes(joinPublicKeyBytes);
                System.out.println(publicKey);
                System.out.println("has joined the chat. " + simpleDateFormat.format(date));
                System.out.println();
                ChatRoom.INSTANCE.publicKeyList.add(publicKey);
                break;
            case 2: // User leave the chat
                byte[] leavePublicKeyBytes = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(leavePublicKeyBytes);
                PublicKey leavePublicKey = EncryptionUtil.getPublicKeyFromBytes(leavePublicKeyBytes);
                System.out.println(leavePublicKey);
                System.out.println("has left the chat." + simpleDateFormat.format(date));
                System.out.println();
                ChatRoom.INSTANCE.publicKeyList.remove(leavePublicKey);
                break;
            case 16: // chatting message
                Message message = new MessageUtil().decryptMessage(byteBuf);
                System.out.println(message.getPublicKey());
                System.out.println("at "+ simpleDateFormat.format(date)+ " said:");
                if(message.getT() instanceof String){
                    System.out.println(message.getT());
                    System.out.println();
                    break;
                }
                System.out.println("Not String message");
            default:
                System.out.println("Illegal message type" + messageType +" received");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Connecting to Server...");
        super.channelActive(ctx);
        ByteBuf byteBuf= new UnpooledUnsafeDirectByteBuf(ByteBufAllocator.DEFAULT,256,Integer.MAX_VALUE);
        byteBuf.writeByte(0);  // Message Type 0: register to a room
        byteBuf.writeInt(ChatRoom.INSTANCE.roomId);
        byteBuf.writeBytes(ChatRoom.INSTANCE.keyMap.get("publicKey").getEncoded());
        ctx.channel().writeAndFlush(byteBuf);
        ChatRoom.INSTANCE.localChannel = ctx.channel();
        System.out.println("Connection established!");
        ChatRoom.INSTANCE.established = true;

    }
}
