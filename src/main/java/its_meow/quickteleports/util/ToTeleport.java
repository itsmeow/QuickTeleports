package its_meow.quickteleports.util;

public class ToTeleport extends Teleport {

	private final String requester;
	private final String destination;
	
	public ToTeleport(String requester, String destination) {
		super(Teleport.TPType.TP_TO);
		this.requester = requester;
		this.destination = destination;
	}

	public String getRequester() {
		return requester;
	}
	
	public String getDestination() {
		return destination;
	}

	@Override
	public String getSubject() {
		return destination;
	}
}
