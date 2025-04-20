package com.ankitPersonalProject.chatAppECDHE.server;

import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@Component
public class ClientMessageHandler
{
    private Map<String, WebSocketSession> activeSessions;
    public ClientMessageHandler(Map<String, WebSocketSession> session)
    {
        this.activeSessions = session;
    }

    public void setActiveSessions(Map<String, WebSocketSession> sessions)
    {
        this.activeSessions = sessions;
    }
    public void handleClientHello(WebSocketSession session, JSONObject json)
    {
        //example payload
        /*
        {
            header : "Client Hello"
            clientId : <name>
        }
         */

        String clientId = json.getString("source");
        activeSessions.put(clientId, session);
        System.out.println(" Saved client session" + session.getId() +" for user " + clientId);

        //now acknowledge the client
        try
        {
            JSONObject serverHelloJson = new JSONObject();
            serverHelloJson.put("header", "Server Hello");
            serverHelloJson.put("Accepted Session", clientId);
            session.sendMessage(new TextMessage(serverHelloJson.toString()));
        }
        catch (Exception e)
        {

        }
    }

    public void handleClientPublicKey(WebSocketSession session, JSONObject json)
    {
        // example payload
        //Client Public Key :<pub key>:peerId
        String src = json.getString("source");
        String dest = json.getString("destination");
        String pubKey = json.getString("publicKey");

        WebSocketSession peerSession = activeSessions.getOrDefault(dest, null);
        if(null == peerSession)
        {
            System.out.println("The peer is invalid. Please check again");
            System.exit(0);
        }
        if(!peerSession.isOpen())
        {
            System.out.println("Peer session is not open");
            return;
        }
        try {
            JSONObject keyShareJson = new JSONObject();
            keyShareJson.put("header", "Client Public Key");
            keyShareJson.put("source", src);
            keyShareJson.put("PUBLIC KEY", pubKey);
            peerSession.sendMessage(new TextMessage(keyShareJson.toString()));
            System.out.println("sent public key from server");
        }
        catch (Exception e)
        {

        }
    }

    public void handleClientApplicationData(WebSocketSession session, JSONObject json)
    {
        // example payload
        // Client Application Data:<encrypted chat message>:peerId

        String src = json.getString("source");
        String dest = json.getString("destination");
        String applicationData = json.getString("Application Data");

        WebSocketSession peerSession = activeSessions.getOrDefault(dest, null);
        if(null == peerSession)
        {
            System.out.println("The peer is invalid. Please check again");
            return;
        }
        if(!peerSession.isOpen())
        {
            System.out.println("Peer session is not open");
            return;
        }

        try {
            JSONObject applicationDataJson = new JSONObject();
            applicationDataJson.put("header", "Application Data");
            applicationDataJson.put("source", src);
            applicationDataJson.put("Application Data", applicationData);
            peerSession.sendMessage(new TextMessage(applicationDataJson.toString()));
        }
        catch (Exception e)
        {

        }
    }

    public void broadcastToClients(String disconnectedClient)
    {
        if(null == disconnectedClient)
        {
            System.out.println("Invalid ID for disconnectedCLient, cannot broadcast");
            return;
        }

        JSONObject disconnectBroadcastJson = new JSONObject();
        disconnectBroadcastJson.put("header", "End Session");
        disconnectBroadcastJson.put("clientID", disconnectedClient);
        for(Map.Entry<String, WebSocketSession> entry : activeSessions.entrySet())
        {
            WebSocketSession session = entry.getValue();
            try
            {
                session.sendMessage(new TextMessage(disconnectBroadcastJson.toString()));
                System.out.println("Sent Broadcast message to : " + entry.getKey());
            }
            catch (Exception e){}

        }
    }
}
