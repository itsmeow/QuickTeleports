package its_meow.quickteleports.util;

public abstract class Teleport {
	
	public final TPType TYPE;
	
	public Teleport(TPType type) {
		this.TYPE = type;
	}
	
	public abstract String getRequester();
	public abstract String getSubject();
	
	public static enum TPType {
		TP_TO,
		TP_BRING;
	}
	
}
