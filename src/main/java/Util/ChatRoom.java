package Util;

import io.netty.channel.Channel;

import java.security.Key;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public enum ChatRoom {
    INSTANCE;
    public boolean established = false;
    public Channel localChannel;
    public int roomId;
    public Map<String, Key> keyMap = new LinkedHashMap<String,Key>();
    public List<PublicKey> publicKeyList = new CopyOnWriteArrayList<>();
    public int sendMessageTimes = 0;
    public boolean sendAllMessage(){
        return sendMessageTimes == publicKeyList.size();
    }

    public void initSendMessageTimes(){
        sendMessageTimes = 0;
    }
}
