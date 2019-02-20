package its_meow.quickteleports;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class BasicTeleporter extends Teleporter{

	public BasicTeleporter(WorldServer worldIn) {
		super(worldIn);
	}

	@Override
	public void placeInPortal(Entity entityIn, float rotationYaw){}

	@Override
	public boolean placeInExistingPortal(Entity entityIn, float p_180620_2_) {
		return false;
	}

}