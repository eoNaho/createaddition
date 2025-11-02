package com.mrh0.createaddition.blocks.alternator;

import com.mrh0.createaddition.index.CABlockEntities;
import com.mrh0.createaddition.shapes.CAShapes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.HashMap;
import java.util.Map;

public class AlternatorBlock extends DirectionalKineticBlock implements IBE<AlternatorBlockEntity>, IRotate {

    // Use o método que já existe no CAShapes
    public static final VoxelShape ALTERNATOR_SHAPE_BASE = Shapes.or(
            Block.box(0, 3, 0, 16, 13, 16),
            Block.box(2, 0, 2, 14, 14, 14)
    );

    private static final Map<Direction, VoxelShape> ALTERNATOR_SHAPES = makeShapes();

    private static Map<Direction, VoxelShape> makeShapes() {
        Map<Direction, VoxelShape> shapes = new HashMap<>();


        for (Direction dir : Direction.values()) {
            shapes.put(dir, ALTERNATOR_SHAPE_BASE);
        }

        return shapes;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return ALTERNATOR_SHAPES.get(state.getValue(FACING));
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if ((context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown()) || preferred == null)
            return super.getStateForPlacement(context);
        return defaultBlockState().setValue(FACING, preferred);
    }

    @Override
    public boolean hideStressImpact() {
        return false;
    }

    public AlternatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public BlockEntityType<? extends AlternatorBlockEntity> getBlockEntityType() {
        return CABlockEntities.ALTERNATOR.get();
    }

    @Override
    public Class<AlternatorBlockEntity> getBlockEntityClass() {
        return AlternatorBlockEntity.class;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlternatorBlockEntity(getBlockEntityType(), pos, state);
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.MEDIUM;
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        BlockEntity tileentity = state.hasBlockEntity() ? worldIn.getBlockEntity(pos) : null;
        if(tileentity != null) {
            if(tileentity instanceof AlternatorBlockEntity) {
                ((AlternatorBlockEntity)tileentity).updateCache();
            }
        }
    }
}