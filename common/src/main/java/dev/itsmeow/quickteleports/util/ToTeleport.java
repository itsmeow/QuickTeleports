package dev.itsmeow.quickteleports.util;

public class ToTeleport implements Teleport {

	private final String requester;
	private final String destination;
	
	public ToTeleport(String requester, String destination) {
		this.requester = requester;
		this.destination = destination;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	@Override
	public String getSubject() {
		return destination;
	}
}
