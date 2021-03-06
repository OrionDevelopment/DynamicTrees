package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.cells.LeafClusters;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FeatureGenBush implements IGenFeature {

	private Species species;
	private int radius = 2;
	private IBlockState logState = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
	private IBlockState leavesState = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockOldLeaf.CHECK_DECAY, false);
	private IBlockState secondaryLeavesState = null;
	
	public FeatureGenBush(Species species) {
		this.species = species;
	}
	
	public FeatureGenBush setRadius(int radius) {
		this.radius = radius;
		return this;
	}
	public FeatureGenBush setLogState(IBlockState logState) {
		this.logState = logState;
		return this;
	}
	public FeatureGenBush setLeavesState(IBlockState leavesState) {
		this.leavesState = leavesState;
		return this;
	}
	public FeatureGenBush setSecondaryLeavesState(IBlockState secondaryLeavesState) {
		this.secondaryLeavesState = secondaryLeavesState;
		return this;
	}
	
	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints, SafeChunkBounds safeBounds) {
		Vec3d vTree = new Vec3d(treePos).addVector(0.5, 0.5, 0.5);

		for (int i = 0; i < 2; i++) {
			int rad = MathHelper.clamp(radius, 2, world.rand.nextInt(radius - 1) + 2);
			Vec3d v = vTree.add(new Vec3d(1, 0, 0).scale(rad).rotateYaw((float) (world.rand.nextFloat() * Math.PI * 2)));

			BlockPos pos = CoordUtils.findGround(world, new BlockPos(v));
			IBlockState soilBlockState = world.getBlockState(pos);
			
			pos = pos.up();
			if (!world.getBlockState(pos).getMaterial().isLiquid() && species.isAcceptableSoil(world, pos, soilBlockState)) {
				world.setBlockState(pos, logState);
				
				SimpleVoxmap leafMap = LeafClusters.bush;
				MutableBlockPos leafPos = new MutableBlockPos();
				for (MutableBlockPos dPos : leafMap.getAllNonZero()) {
					leafPos.setPos( pos.getX() + dPos.getX(), pos.getY() + dPos.getY(), pos.getZ() + dPos.getZ() );
					if ((coordHashCode(leafPos) % 5) != 0 && safeBounds.inBounds(leafPos, false) && world.getBlockState(leafPos).getBlock().isReplaceable(world, leafPos)) {
						world.setBlockState(leafPos, (secondaryLeavesState == null || world.rand.nextInt(4) != 0) ? leavesState : secondaryLeavesState);
					}
				}
			}
		}
	}

	public static int coordHashCode(BlockPos pos) {
		int hash = (pos.getX() * 4111 ^ pos.getY() * 271 ^ pos.getZ() * 3067) >> 1;
		return hash & 0xFFFF;
	}

}
