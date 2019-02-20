package its_meow.quickteleports;

import net.minecraftforge.common.config.Config;

@Config(modid = Ref.MOD_ID)
public class TpConfig {
	
	@Config.Comment("Timeout for teleport requests, in seconds.")
	public static int tp_request_timeout = 30;
	
}
