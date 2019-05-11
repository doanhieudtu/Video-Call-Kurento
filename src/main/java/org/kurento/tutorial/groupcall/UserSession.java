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

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.kurento.client.Continuation;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.MediaPipeline;
import org.kurento.client.MediaState;
import org.kurento.client.MediaStateChangedEvent;
import org.kurento.client.RecorderEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.kurento.tutorial.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;

/**
 *
 * @author Ivan Gracia (izanmail@gmail.com)
 * @since 4.3.1
 */
public class UserSession implements Closeable {

  private static final Logger log = LoggerFactory.getLogger(UserSession.class);

  private final User user;
  private final WebSocketSession session;

  private final MediaPipeline pipeline;
  private final String roomName;
  private final WebRtcEndpoint outgoingMedia;
  private final ConcurrentMap<String, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();

  public UserSession(final User user, String roomName, final WebSocketSession session,
      MediaPipeline pipeline) {

    this.pipeline = pipeline;
    this.user = user;
    this.session = session;
    this.roomName = roomName;
    this.outgoingMedia = new WebRtcEndpoint.Builder(pipeline).build();
    this.outgoingMedia.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {
      @Override
      public void onEvent(IceCandidateFoundEvent event) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "iceCandidate");
        response.addProperty("name", user.getEmail());
        response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
        try {
          synchronized (session) {
            session.sendMessage(new TextMessage(response.toString()));
          }
        } catch (IOException e) {
          log.debug(e.getMessage());
        }
      }
    });
    this.outgoingMedia.addMediaStateChangedListener(new EventListener<MediaStateChangedEvent>() {
		@Override
		public void onEvent(MediaStateChangedEvent event) {
			// TODO Auto-generated method stub
	    	if (event.getNewState() == MediaState.CONNECTED){
	    		RecorderEndpoint recorderEndpoint = new RecorderEndpoint.Builder(
	            pipeline,"file:///home/media/Desktop/server_extention/file/abc.mp4").build();
	            outgoingMedia.connect(recorderEndpoint);
	    	    recorderEndpoint.setMaxOutputBitrate(2000);
	    	    recorderEndpoint.record();
	    		}
		}
	});
    
  }
  public WebRtcEndpoint getOutgoingWebRtcPeer() {
    return outgoingMedia;
  }

 
  public WebSocketSession getSession() {
    return session;
  }

  /**
   * The room to which the user is currently attending.
   *
   * @return The room
   */
  public String getRoomName() {
    return this.roomName;
  }

  public void receiveVideoFrom(UserSession sender, String sdpOffer) throws IOException {
    log.info("USER {}: connecting with {} in room {}", this.user.getEmail(), sender.getUser().getEmail(), this.roomName);

    log.trace("USER {}: SdpOffer for {} is {}", this.user.getEmail(), sender.getUser().getEmail(), sdpOffer);

    final String ipSdpAnswer = this.getEndpointForUser(sender).processOffer(sdpOffer);
    final JsonObject scParams = new JsonObject();
    scParams.addProperty("id", "receiveVideoAnswer");
    scParams.addProperty("name", sender.getUser().getEmail());
    scParams.addProperty("sdpAnswer", ipSdpAnswer);

    log.trace("USER {}: SdpAnswer for {} is {}", this.user.getEmail(), sender.user.getEmail(), ipSdpAnswer);
    this.sendMessage(scParams);
    log.debug("gather candidates");
    this.getEndpointForUser(sender).gatherCandidates();
  }

  private WebRtcEndpoint getEndpointForUser(final UserSession sender) {
    if (sender.getUser().getEmail().equals(user.getEmail())) {
      log.debug("PARTICIPANT {}: configuring loopback", this.user.getEmail());
      return outgoingMedia;
    }
    log.debug("PARTICIPANT {}: receiving video from {}", this.user.getEmail(), sender.getUser().getEmail());

    WebRtcEndpoint incoming = incomingMedia.get(sender.getUser().getEmail());
    if (incoming == null) {
      log.debug("PARTICIPANT {}: creating new endpoint for {}", this.user.getEmail(), sender.getUser().getEmail());
      incoming = new WebRtcEndpoint.Builder(pipeline).build();

      incoming.addIceCandidateFoundListener(new EventListener<IceCandidateFoundEvent>() {

        @Override
        public void onEvent(IceCandidateFoundEvent event) {
          JsonObject response = new JsonObject();
          response.addProperty("id", "iceCandidate");
          response.addProperty("name", sender.getUser().getEmail());
          response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
          try {
            synchronized (session) {
              session.sendMessage(new TextMessage(response.toString()));
            }
          } catch (IOException e) {
            log.debug(e.getMessage());
          }
        }
      });

      incomingMedia.put(sender.getUser().getEmail(), incoming);
    }

    log.debug("PARTICIPANT {}: obtained endpoint for {}", this.user.getEmail(), sender.getUser().getEmail());
    sender.getOutgoingWebRtcPeer().connect(incoming);

    return incoming;
  }

  public void cancelVideoFrom(final UserSession sender) {
    this.cancelVideoFrom(sender.getUser().getEmail());
  }

  public void cancelVideoFrom(final String senderName) {
    log.debug("PARTICIPANT {}: canceling video reception from {}", this.user.getEmail(), senderName);
    final WebRtcEndpoint incoming = incomingMedia.remove(senderName);

    log.debug("PARTICIPANT {}: removing endpoint for {}", this.user.getEmail(), senderName);
    incoming.release(new Continuation<Void>() {
      @Override
      public void onSuccess(Void result) throws Exception {
        log.trace("PARTICIPANT {}: Released successfully incoming EP for {}",
            UserSession.this.user.getEmail(), senderName);
      }

      @Override
      public void onError(Throwable cause) throws Exception {
        log.warn("PARTICIPANT {}: Could not release incoming EP for {}", UserSession.this.user.getEmail(),
            senderName);
      }
    });
  }

  @Override
  public void close() throws IOException {
    log.debug("PARTICIPANT {}: Releasing resources", this.user.getEmail());
    for (final String remoteParticipantName : incomingMedia.keySet()) {

      log.trace("PARTICIPANT {}: Released incoming EP for {}", this.user.getEmail(), remoteParticipantName);

      final WebRtcEndpoint ep = this.incomingMedia.get(remoteParticipantName);

      ep.release(new Continuation<Void>() {

        @Override
        public void onSuccess(Void result) throws Exception {
          log.trace("PARTICIPANT {}: Released successfully incoming EP for {}",
              UserSession.this.user.getEmail(), remoteParticipantName);
        }

        @Override
        public void onError(Throwable cause) throws Exception {
          log.warn("PARTICIPANT {}: Could not release incoming EP for {}", UserSession.this.user.getEmail(),
              remoteParticipantName);
        }
      });
    }

    outgoingMedia.release(new Continuation<Void>() {

      @Override
      public void onSuccess(Void result) throws Exception {
        log.trace("PARTICIPANT {}: Released outgoing EP", UserSession.this.user.getEmail());
      }

      @Override
      public void onError(Throwable cause) throws Exception {
        log.warn("USER {}: Could not release outgoing EP", UserSession.this.user.getEmail());
      }
    });
  }

  public void sendMessage(JsonObject message) throws IOException {
    log.debug("USER {}: Sending message {}", user.getEmail(), message);
    synchronized (session) {
      session.sendMessage(new TextMessage(message.toString()));
    }
  }

  public void addCandidate(IceCandidate candidate, String name) {
    if (this.user.getEmail().compareTo(name) == 0) {
      outgoingMedia.addIceCandidate(candidate);
    } else {
      WebRtcEndpoint webRtc = incomingMedia.get(name);
      if (webRtc != null) {
        webRtc.addIceCandidate(candidate);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof UserSession)) {
      return false;
    }
    UserSession other = (UserSession) obj;
    boolean eq = user.getEmail().equals(other.getUser().getEmail());
    eq &= roomName.equals(other.roomName);
    return eq;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + user.getEmail().hashCode();
    result = 31 * result + roomName.hashCode();
    return result;
  }

public User getUser() {
	return user;
}
  
  
  
}
