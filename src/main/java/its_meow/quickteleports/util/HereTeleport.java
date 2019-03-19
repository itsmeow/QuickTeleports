package its_meow.quickteleports.util;

public class HereTeleport extends Teleport {

	private final String requester;
	private final String brought;
	
	public HereTeleport(String requester, String brought) {
		super(Teleport.TPType.TP_TO);
		this.requester = requester;
		this.brought = brought;
	}

	public String getRequester() {
		return requester;
	}
	
	public String getDestination() {
		return brought;
	}

	@Override
	public String getSubject() {
		return brought;
	}
}
