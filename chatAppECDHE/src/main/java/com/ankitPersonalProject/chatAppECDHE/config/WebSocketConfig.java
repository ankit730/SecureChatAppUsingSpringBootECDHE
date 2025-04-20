package com.ankitPersonalProject.chatAppECDHE.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.ankitPersonalProject.chatAppECDHE.server.ServerSocketHandler;

import java.util.Scanner;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private ServerSocketHandler serverSocketHandler;
    @Autowired
    public WebSocketConfig(ServerSocketHandler serverSocketHandler)
    {
        this.serverSocketHandler = serverSocketHandler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(serverSocketHandler, "/chat").setAllowedOrigins("*");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while(true)
        {
            System.out.println("Enter number");
            int x = sc.nextInt();
            System.out.println(x);
            if(x < 0) break;
        }
    }
}
