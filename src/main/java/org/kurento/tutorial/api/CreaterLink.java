package org.kurento.tutorial.api;

import java.util.UUID;

public class CreaterLink {
	public String getLink() {
		UUID code= UUID.randomUUID();
		return "https://localhost:8443/join-call?room="+code.toString();
	}
}
