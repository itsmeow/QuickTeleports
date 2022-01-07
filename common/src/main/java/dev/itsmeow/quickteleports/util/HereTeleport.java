package dev.itsmeow.quickteleports.util;

public class HereTeleport implements Teleport {

	private final String requester;
	private final String brought;
	
	public HereTeleport(String requester, String brought) {
		this.requester = requester;
		this.brought = brought;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	@Override
	public String getSubject() {
		return brought;
	}
}
