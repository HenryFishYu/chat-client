package Util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MessageUtil {
    Date date = new Date();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public ByteBuf getEncryptedStringMessageByteBuf(PublicKey othersPublicKey, PublicKey ownPublicKey,String message) throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
        ByteBuf byteBuf = new UnpooledUnsafeDirectByteBuf(ByteBufAllocator.DEFAULT,512,2048);
        byteBuf.writeByte(16); // Message Type 16: Chatting message from other users
        byteBuf.writeBytes(othersPublicKey.getEncoded()); // 162 bytes
        byteBuf.writeBytes(ownPublicKey.getEncoded()); //162 bytes
        ByteBuf tempByteBuf = new UnpooledUnsafeDirectByteBuf(ByteBufAllocator.DEFAULT,512,2048);
        tempByteBuf.writeByte(0); // Requires decode : String message - 0
        tempByteBuf.writeBytes(message.getBytes());
        byte[] needEncryptionBytes = new byte[tempByteBuf.readableBytes()];
        tempByteBuf.readBytes(needEncryptionBytes);
        byteBuf.writeBytes(EncryptionUtil.encryptMessageWithPublicKey(needEncryptionBytes,othersPublicKey));
        return byteBuf;
    }

    public void sendEncryptedStringMessage(List<PublicKey> othersPublicKeyList, PublicKey ownPublicKey, Channel channel, String message) throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
        for(PublicKey othersPublicKey : othersPublicKeyList){
            channel.writeAndFlush(getEncryptedStringMessageByteBuf(othersPublicKey,ownPublicKey,message)).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    ChatRoom.INSTANCE.sendMessageTimes++;
                    if(ChatRoom.INSTANCE.sendAllMessage()){
                        System.out.println("Send message Successful");
                        System.out.println(simpleDateFormat.format(date)+" you said:");
                        System.out.println(message);
                    }
                    ChatRoom.INSTANCE.initSendMessageTimes();
                }
            });
        }

    }

    public Message decryptMessage(ByteBuf byteBuf) throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
        byteBuf.readBytes(162);
        byte[] fromPublicKeys = new byte[162];
        byteBuf.readBytes(fromPublicKeys);
        byte[] encryptedbytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(encryptedbytes);
        ByteBuf messageByteBuf = new UnpooledUnsafeDirectByteBuf(ByteBufAllocator.DEFAULT,512,2048);
        messageByteBuf.writeBytes(EncryptionUtil.decryptMessageWithPrivateKey(encryptedbytes, (PrivateKey) ChatRoom.INSTANCE.keyMap.get("privateKey")));
        int type = messageByteBuf.readByte();
        switch (type){
            case 0:
                Message<String> stringMessage = new Message<String>();
                byte[] messageBytes = new byte[messageByteBuf.readableBytes()];
                messageByteBuf.readBytes(messageBytes);
                stringMessage.setT(new String(messageBytes));
                stringMessage.setPublicKey(EncryptionUtil.getPublicKeyFromBytes(fromPublicKeys));
                return stringMessage;
            default:
                System.out.println("Unknown message type");
                return null;
        }
    }
}
