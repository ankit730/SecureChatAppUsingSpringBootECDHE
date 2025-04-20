package com.ankitPersonalProject.chatAppECDHE.server;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ServerSocketHandler extends TextWebSocketHandler
{
    private Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    ClientMessageHandler clientMessageHandler;
    @Autowired
    public ServerSocketHandler(ClientMessageHandler clientMessageHandler)
    {
        this.clientMessageHandler = clientMessageHandler;
        clientMessageHandler.setActiveSessions(activeSessions);
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session)
    {
//        if(null != session)
//        {
//            activeSessions.put(session.getId(), session);
//            System.out.println("New connection from client");
//        }
//        else
//        {
//            System.out.println("Invalid session");
//        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
    {
        String payload = message.getPayload();
        JSONObject json = new JSONObject(payload);
        String header = json.getString("header");
        switch(header)
        {
            case "Client Hello":
                clientMessageHandler.handleClientHello(session, json);
                break;
            case "Client Public Key":
                clientMessageHandler.handleClientPublicKey(session, json);
                break;
            case "Client Application Data":
                clientMessageHandler.handleClientApplicationData(session, json);
                break;
//            case "Session End":
//                clientMessageHandler.handleDisconnection(session, json);
//                break;
            default:
                System.out.println("Invalid payload from Client");

        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
    {
        String disconnectedClient = null;
        for(Map.Entry<String, WebSocketSession> entry : activeSessions.entrySet())
        {
            if(entry.getValue().equals(session))
            {
                disconnectedClient = entry.getKey();
                break;
            }
        }

        if(null != disconnectedClient)
        {
            activeSessions.remove(disconnectedClient);
            clientMessageHandler.broadcastToClients(disconnectedClient);
        }

        System.out.println("Session closed : " + session.getId());
    }


}
