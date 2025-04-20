package com.ankitPersonalProject.chatAppECDHE.client;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ClientSocketHandler extends TextWebSocketHandler
{
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
    {
        String payload = message.getPayload();
        JSONObject json = new JSONObject(payload);
        System.out.println("got message");
        String header = json.getString("header");
        switch(header)
        {
            case "Client Public Key":
                String peerId = json.getString("source");
                String pubKey = json.getString("PUBLIC KEY");
                SecureClient.peerPublicKey.put(peerId, pubKey);
                //SecureClient.peerPublicKey.put(SecureClient.userID, SecureClient.publicKey);
                System.out.println("Received public key from "+peerId);
                System.out.println(SecureClient.peerPublicKey.size());
                if(!SecureClient.peersAlreadyReceivedPubKey.contains(peerId))
                {
                    JSONObject clientPubKeyJson = new JSONObject();
                    clientPubKeyJson.put("header", "Client Public Key");
                    clientPubKeyJson.put("source", SecureClient.userID);
                    clientPubKeyJson.put("destination", peerId);
                    clientPubKeyJson.put("publicKey", SecureClient.publicKey);

                    try {
                        System.out.println("sending public key to the peer");
                        session.sendMessage(new TextMessage(clientPubKeyJson.toString()));
                        SecureClient.peersAlreadyReceivedPubKey.add(peerId);
                    } catch (Exception e) {
                    }
                }
                break;
            case "Application Data":
                peerId = json.getString("source");
                String applicationData = json.getString("Application Data");
                System.out.println(peerId +" : " +applicationData);
                break;
            case "Server Hello":
                String isAcceptedForClient = json.getString("Accepted Session");
                System.out.println("Accepted session for : "+isAcceptedForClient);
                break;
            default:
                System.out.println("Invalid message received");
                System.out.println(header + " INVALID");

        }
    }
}
