package com.nighthawk.spring_portfolio.mvc.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import groovy.util.logging.Log4j2;

import org.springframework.stereotype.Component;


@Component
@Log4j2
public class SocketController {

    private static final Logger log = LogManager.getLogger(SocketController.class);


    @Autowired
    private SocketIOServer socketServer;

    @Autowired
    private UserQueueManager userQueueManager;


    public SocketController(SocketIOServer socketServer){
        this.socketServer=socketServer;

        this.socketServer.addConnectListener(onUserConnectWithSocket);
        this.socketServer.addDisconnectListener(onUserDisconnectWithSocket);

        this.socketServer.addEventListener("messageSendToUser", Message.class, onSendMessage);

    }


    public ConnectListener onUserConnectWithSocket = new ConnectListener() {
        
        @Override
        public void onConnect(SocketIOClient client) {
            log.info("Perform operation on user connect in controller");
        }
    };


    public DisconnectListener onUserDisconnectWithSocket = new DisconnectListener() {
        @Override
        public void onDisconnect(SocketIOClient client) {
            log.info("Perform operation on user disconnect in controller");
        }
    };

    public DataListener<Message> onSendMessage = new DataListener<Message>() {
        @Override
        public void onData(SocketIOClient client, Message message, AckRequest acknowledge) throws Exception {


            log.info(message.getSenderName()+" user send message to user "+message.getTargetUserName()+" and message is "+message.getMessage());
            socketServer.getBroadcastOperations().sendEvent(message.getTargetUserName(),client, message);

            acknowledge.sendAckData("Message send to target user successfully");
        }
    };

    public void checkUserInQueue(SocketIOClient client, int userId) {
        boolean exists = userQueueManager.isUserIdInQueue(userId);
        client.sendEvent("checkUserResponse", exists);
    }


}