package netty.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import netty.decoder.EncryptionDecoder;
import netty.encoder.EncryptionEncoder;
import netty.handler.ChatClientHandler;

public class ChatClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline channelPipeline = socketChannel.pipeline();
        channelPipeline
                .addFirst(new EncryptionDecoder())
                .addFirst(new LengthFieldBasedFrameDecoder(2048,0,4,0,4))

                .addLast(new ChatClientHandler())
                .addLast(new EncryptionEncoder());

    }
}
