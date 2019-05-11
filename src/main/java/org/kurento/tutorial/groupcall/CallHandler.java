/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.kurento.tutorial.groupcall;

import java.io.IOException;

import org.kurento.client.IceCandidate;
import org.kurento.tutorial.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * 
 * @author Ivan Gracia (izanmail@gmail.com)
 * @since 4.3.1
 */
public class CallHandler extends TextWebSocketHandler {

  private static final Logger log = LoggerFactory.getLogger(CallHandler.class);

  private static final Gson gson = new GsonBuilder().create();

  @Autowired
  private RoomManager roomManager;

  @Autowired
  private UserRegistry registry;

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

    final UserSession user = registry.getBySession(session);

    if (user != null) {
      log.debug("Incoming message from user '{}': {}", user.getUser().getEmail(), jsonMessage);
    } else {
      log.debug("Incoming message from new user: {}", jsonMessage);
    }

    switch (jsonMessage.get("id").getAsString()) {
      case "joinRoom":
        joinRoom(jsonMessage, session);
        break;
      case "sendMessage":
          sendMessage(jsonMessage, session);
          break;
      case "receiveVideoFrom":
        final String senderName = jsonMessage.get("sender").getAsString();
        final UserSession sender = registry.getByName(senderName);
        final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
        user.receiveVideoFrom(sender, sdpOffer);
        break;
      case "leaveRoom":
        leaveRoom(user);
        break;
        /* đang giao tiếp*/
      case "onIceCandidate":
        JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
        if (user != null) {
          IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
              candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
          user.addCandidate(cand, jsonMessage.get("name").getAsString());
        }
        break;
        /* đang giao tiếp*/
      default:
        break;
    }
  }
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    UserSession user = registry.removeBySession(session);
    try {
    	roomManager.getRoom(user.getRoomName()).leave(user);
	} catch (Exception e) {
		// TODO: handle exception
	}
  }
  private void sendMessage(JsonObject params, WebSocketSession session) throws IOException{
	  /*get name and data*/
	  String name= params.get("name").getAsString();
	  String data = params.get("data").getAsString();
	  /*get name and data*/
	  UserSession userSession = registry.getBySession(session);//get session
	  Room room = roomManager.getRoom(userSession.getRoomName());//get room
	  /*create message*/
	  JsonObject passData= new JsonObject();
	  passData.addProperty("id", "inComingMessage");
	  passData.addProperty("name", name);
	  passData.addProperty("data", data);
	  /*create message*/
	  /*send message for participant*/
	  for (UserSession participant : room.getParticipants()) {
	  participant.sendMessage(passData);
	  }
	  /*send message for participants*/
  }
  private void joinRoom(JsonObject params, WebSocketSession session) throws IOException {
    final String roomName = params.get("room").getAsString();
    final String name = params.get("name").getAsString();
    log.info("PARTICIPANT {}: trying to join room {}", name, roomName);
	User client= new User();
	client.setEmail(name);
    Room room = roomManager.getRoom(roomName);
    final UserSession user = room.join(client, session);
    registry.register(user);
  }

  private void leaveRoom(UserSession user) throws IOException {
    final Room room = roomManager.getRoom(user.getRoomName());
    room.leave(user);
    if (room.getParticipants().isEmpty()) {
      roomManager.removeRoom(room);
    }
  }
}
