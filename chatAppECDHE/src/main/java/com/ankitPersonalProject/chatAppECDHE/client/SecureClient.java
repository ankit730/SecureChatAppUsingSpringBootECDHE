package com.ankitPersonalProject.chatAppECDHE.client;

import com.ankitPersonalProject.chatAppECDHE.utils.ECDHE;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SecureClient
{
    public  static String userID;
    public  static WebSocketSession session;
    public static Scanner sc;
    public static String publicKey;
    public static Map<String, String> peerPublicKey = new ConcurrentHashMap<>();
    public static Set<String> peersAlreadyReceivedPubKey = new HashSet<>();
    public static void main(String[] args)
    {
        sc = new Scanner(System.in);
        System.out.println("Enter your ID(name) : ");
        userID = sc.nextLine();

        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketHandler handler = new ClientSocketHandler();
        try
        {
            session = client.doHandshake(handler, "ws://localhost:8080/chat").get();
        }
        catch (Exception e){}
        System.out.println(session.isOpen() +"ANKIT");
        // time to say "Client Hello"
        JSONObject clientHelloJson = new JSONObject();
        clientHelloJson.put("header", "Client Hello");
        clientHelloJson.put("source", userID);
        try
        {
            session.sendMessage(new TextMessage(clientHelloJson.toString()));
            System.out.println("Sending client Hello from : "+userID);
        }
        catch (Exception e){}

        ECDHE ecdhe = new ECDHE();
        try {
            ecdhe.generateEphemeralKeys();
        }
        catch(Exception e){}
        publicKey = ecdhe.getEncodedPublicKey();
        while(true) {
            System.out.println("Enter Client Id to start chatting : ");
            String peerId = sc.nextLine();
            if(peerId.equals("QUIT")) break;
            // Thread inputThread = new Thread(() -> {
            while (true) {


                //if the peer public key is not present exchange public key
                if (!peerPublicKey.containsKey(peerId)) {
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                    }
                    JSONObject clientPubKeyJson = new JSONObject();
                    clientPubKeyJson.put("header", "Client Public Key");
                    clientPubKeyJson.put("source", userID);
                    clientPubKeyJson.put("destination", peerId);
                    clientPubKeyJson.put("publicKey", publicKey);

                    try {
                        System.out.println("sending public key to the peer");
                        session.sendMessage(new TextMessage(clientPubKeyJson.toString()));
                        peersAlreadyReceivedPubKey.add(peerId);
                    } catch (Exception e) {
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {

                    }

                    System.out.println(peerPublicKey.size() + "map size");
                }

                System.out.println("Message : ");
                String msgToSend = sc.nextLine();
                if(msgToSend.equals("EXIT")) break;
                if (peerPublicKey.containsKey(peerId)) {
                    String peerPublicKeyEncoded = peerPublicKey.get(peerId);
                    String applicationData = null;
                    try {
                        ecdhe.generateSharedSecret(peerPublicKeyEncoded);
                    } catch (Exception e) {

                    }

                    try {
                        applicationData = ecdhe.encrypt(msgToSend);
                    } catch (Exception e) {

                    }

                    JSONObject applicationDataJson = new JSONObject();
                    applicationDataJson.put("header", "Client Application Data");
                    applicationDataJson.put("source", userID);
                    applicationDataJson.put("destination", peerId);
                    applicationDataJson.put("Application Data", applicationData);

                    try {
                        session.sendMessage(new TextMessage(applicationDataJson.toString()));
                    } catch (IOException e) {

                    }
                }


            }
        }
//        });
//        inputThread.start();
    }
}
