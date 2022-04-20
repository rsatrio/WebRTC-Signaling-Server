package com.rizky.signalling;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rizky.signaling.model.SignalData;
import com.rizky.signaling.model.SignalType;



public class SignalingHandler extends TextWebSocketHandler{


	List<WebSocketSession>sessions = new LinkedList<WebSocketSession>();
	ConcurrentHashMap<String,WebSocketSession>sessionMap = new ConcurrentHashMap<String,WebSocketSession>();
	final ObjectMapper map1=new ObjectMapper();
	Logger log1=LoggerFactory.getLogger(SignalingHandler.class);

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

		final String  msg1=message.getPayload();
		SignalData sigData=map1.readValue(msg1, SignalData.class);
		log1.debug("Receive message from client:",msg1);

		SignalData sigResp=new SignalData();

		if(sigData.getType().equalsIgnoreCase(SignalType.Login.toString()))	{
			SignalData sigResp2=new SignalData();
			String userId=UUID.randomUUID().toString();
			sigResp2.setUserId("signaling");
			sigResp2.setType(SignalType.UserId.toString());
			sigResp2.setData(userId);
			sessionMap.put(userId, session);
			session.sendMessage(new TextMessage(map1.writeValueAsString(sigResp2)));

			return ;
		}
		else if(sigData.getType().equalsIgnoreCase(SignalType.NewMember.toString()))	{

			sessionMap.values().forEach(a->{

				SignalData sigResp2=new SignalData();
				sigResp2.setUserId(sigData.getUserId());
				sigResp2.setType(SignalType.NewMember.toString());			
				try	{
					//Check if websocket is open
					if(a.isOpen())	{
						log1.debug("Sending New Member from",sigData.getUserId());
						a.sendMessage(new TextMessage(map1.writeValueAsString(sigResp2)));
					}
				}
				catch(Exception e)	{
					log1.error("Error Sending message:",e);
				}
			});

			
			return ;
		}
		else if(sigData.getType().equalsIgnoreCase(SignalType.Offer.toString()))	{
			sigResp=new SignalData();
			sigResp.setUserId(sigData.getUserId());
			sigResp.setType(SignalType.Offer.toString());
			sigResp.setData(sigData.getData());
			sigResp.setToUid(sigData.getToUid());
			sessionMap.get(sigData.getToUid()).sendMessage(new TextMessage(map1.writeValueAsString(sigResp)));
			

		}
		else if(sigData.getType().equalsIgnoreCase(SignalType.Answer.toString()))	{
			sigResp=new SignalData();
			sigResp.setUserId(sigData.getUserId());
			sigResp.setType(SignalType.Answer.toString());
			sigResp.setData(sigData.getData());
			sigResp.setToUid(sigData.getToUid());
			sessionMap.get(sigData.getToUid()).sendMessage(new TextMessage(map1.writeValueAsString(sigResp)));
			

		}
		else if(sigData.getType().equalsIgnoreCase(SignalType.Ice.toString()))	{
			sigResp=new SignalData();
			sigResp.setUserId(sigData.getUserId());
			sigResp.setType(SignalType.Ice.toString());
			sigResp.setData(sigData.getData());
			sigResp.setToUid(sigData.getToUid());
			sessionMap.get(sigData.getToUid()).sendMessage(new TextMessage(map1.writeValueAsString(sigResp)));
			

		}


	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		
		sessions.add(session);
		super.afterConnectionEstablished(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		
		sessions.remove(session);
		super.afterConnectionClosed(session, status);
	}
}
