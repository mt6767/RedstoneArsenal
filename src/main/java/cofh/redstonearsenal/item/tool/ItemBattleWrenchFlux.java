package cofh.redstonearsenal.item.tool;

import buildcraft.api.tools.IToolWrench;
import cofh.api.block.IDismantleable;
import cofh.api.item.IToolHammer;
import cofh.core.util.helpers.BlockHelper;
import cofh.core.util.helpers.ServerHelper;
import crazypants.enderio.api.tool.ITool;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

import javax.annotation.Nonnull;

@Optional.InterfaceList ({ @Optional.Interface (iface = "buildcraft.api.tools.IToolWrench", modid = "buildcraftcore"), @Optional.Interface (iface = "crazypants.enderio.api.tool.ITool", modid = "enderio") })
public class ItemBattleWrenchFlux extends ItemSwordFlux implements IToolHammer, IToolWrench, ITool {

	public ItemBattleWrenchFlux(ToolMaterial toolMaterial) {

		super(toolMaterial);
		setHarvestLevel("wrench", 1);
		damage = 5;
		damageCharged = 8;
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {

		return true;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase player) {

		entity.rotationYaw += 90;
		entity.rotationYaw %= 360;
		return super.hitEntity(stack, entity, player);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		return ServerHelper.isClientWorld(world) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItemDamage() > 0) {
			stack.setItemDamage(0);
		}
		if (!player.capabilities.isCreativeMode && getEnergyStored(stack) < getEnergyPerUse(stack)) {
			return EnumActionResult.PASS;
		}
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if (world.isAirBlock(pos)) {
			return EnumActionResult.PASS;
		}
		PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, pos, side, new Vec3d(hitX, hitY, hitZ));
		if (MinecraftForge.EVENT_BUS.post(event) || event.getResult() == Result.DENY || event.getUseBlock() == Result.DENY || event.getUseItem() == Result.DENY) {
			return EnumActionResult.PASS;
		}
		if (ServerHelper.isServerWorld(world) && player.isSneaking() && block instanceof IDismantleable && ((IDismantleable) block).canDismantle(world, pos, state, player)) {
			((IDismantleable) block).dismantleBlock(world, pos, state, player, false);
			if (!player.capabilities.isCreativeMode) {
				useEnergy(stack, false);
			}
			return EnumActionResult.SUCCESS;
		}
		if (BlockHelper.canRotate(block)) {
			world.setBlockState(pos, BlockHelper.rotateVanillaBlock(world, state, pos), 3);
			if (!player.capabilities.isCreativeMode) {
				useEnergy(stack, false);
			}
			player.swingArm(hand);
			return ServerHelper.isServerWorld(world) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
		} else if (!player.isSneaking() && block.rotateBlock(world, pos, side)) {
			if (!player.capabilities.isCreativeMode) {
				useEnergy(stack, false);
			}
			player.swingArm(hand);
			return ServerHelper.isServerWorld(world) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
		}
		return EnumActionResult.PASS;
	}

	/* IToolHammer */
	@Override
	public boolean isUsable(ItemStack item, EntityLivingBase user, BlockPos pos) {

		if (user instanceof EntityPlayer) {
			if (((EntityPlayer) user).capabilities.isCreativeMode) {
				return true;
			}
		}
		return getEnergyStored(item) >= getEnergyPerUse(item);
	}

	@Override
	public boolean isUsable(ItemStack item, EntityLivingBase user, Entity entity) {

		if (user instanceof EntityPlayer) {
			if (((EntityPlayer) user).capabilities.isCreativeMode) {
				return true;
			}
		}
		return getEnergyStored(item) >= getEnergyPerUse(item);
	}

	@Override
	public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {

		if (user instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) user;
			if (!player.capabilities.isCreativeMode) {
				useEnergy(player.getHeldItemMainhand(), false);
			}
		}
	}

	@Override
	public void toolUsed(ItemStack item, EntityLivingBase user, Entity entity) {

		if (user instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) user;
			if (!player.capabilities.isCreativeMode) {
				useEnergy(player.getHeldItemMainhand(), false);
			}
		}
	}

	/* IMPLEMENTABLES */

	/* ITool */
	@Override
	public boolean shouldHideFacades(@Nonnull ItemStack stack, @Nonnull EntityPlayer player) {

		return false;
	}

	@Override
	public boolean canUse(@Nonnull EnumHand stack, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) {

		return true;
	}

	@Override
	public void used(@Nonnull EnumHand stack, @Nonnull EntityPlayer player, @Nonnull BlockPos pos) {

	}

	/* IToolWrench */
	@Override
	public boolean canWrench(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {

		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {

	}

}
