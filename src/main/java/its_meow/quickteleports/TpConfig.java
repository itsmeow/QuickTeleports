package its_meow.quickteleports;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public class TpConfig {
	
	public static TpConfig CONFIG = null;

    public static ForgeConfigSpec SERVER_CONFIG = null;

    public static void setupConfig() {
        final Pair<TpConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(TpConfig::new);
        SERVER_CONFIG = specPair.getRight();
        CONFIG = specPair.getLeft();
    }
	
	public IntValue timeout;
	
	public TpConfig(ForgeConfigSpec.Builder builder) {
	    timeout = builder.comment("Timeout for teleport requests, in seconds.").defineInRange("teleport_request_timeout", 30, 0, Integer.MAX_VALUE);
	    builder.build();
	}
	
}
