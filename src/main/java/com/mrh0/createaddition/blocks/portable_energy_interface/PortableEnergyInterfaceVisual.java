package com.mrh0.createaddition.blocks.portable_energy_interface;

//import com.jozufozu.flywheel.api.MaterialManager;
//import com.jozufozu.flywheel.api.instance.DynamicInstance;
//import com.jozufozu.flywheel.api.instance.TickableInstance;
//import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
//import com.jozufozu.flywheel.core.Materials;
//import com.jozufozu.flywheel.core.PartialModel;
//import com.jozufozu.flywheel.core.materials.model.ModelData;
//import com.jozufozu.flywheel.util.AnimationTickHolder;
import dev.engine_room.flywheel.api.instance.Instance;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class PortableEnergyInterfaceVisual extends ActorVisual {

    private final PIInstance instance;

    public PortableEnergyInterfaceVisual(VisualizationContext context, VirtualRenderWorld world, MovementContext movementContext) {
        super(context, world, movementContext);

        instance = new PIInstance(context.instancerProvider(), movementContext.state, movementContext.localPos, false);

        instance.middle.light(localBlockLight(), 0);
        instance.top.light(localBlockLight(), 0);
    }

    @Override
    public void beginFrame() {
        LerpedFloat lf = PortableEnergyInterfaceMovement.getAnimation(context);
        instance.tick(lf.settled());
        instance.beginFrame(lf.getValue(AnimationTickHolder.getPartialTicks()));
    }

    @Override
    protected void _delete() {
        instance.remove();
    }

    public static class PIInstance {
        private final InstancerProvider instancerProvider;
        private final BlockState blockState;
        private final BlockPos instancePos;
        private final float angleX;
        private final float angleY;

        private boolean lit;
        TransformedInstance middle;
        TransformedInstance top;

        public PIInstance(InstancerProvider instancerProvider, BlockState blockState, BlockPos instancePos, boolean lit) {
            this.instancerProvider = instancerProvider;
            this.blockState = blockState;
            this.instancePos = instancePos;
            Direction facing = blockState.getValue(PortableEnergyInterfaceBlock.FACING);
            angleX = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
            angleY = AngleHelper.horizontalAngle(facing);
            this.lit = lit;

            middle = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(PortableEnergyInterfaceRenderer.getMiddleForState(blockState, lit)))
                    .createInstance();
            top = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(PortableEnergyInterfaceRenderer.getTopForState(blockState)))
                    .createInstance();
        }

        public void beginFrame(float progress) {
            middle.setIdentityTransform()
                    .translate(instancePos)
                    .center()
                    .rotateYDegrees(angleY)
                    .rotateXDegrees(angleX)
                    .uncenter();

            top.setIdentityTransform()
                    .translate(instancePos)
                    .center()
                    .rotateYDegrees(angleY)
                    .rotateXDegrees(angleX)
                    .uncenter();

            middle.translate(0, progress * 0.5f + 0.375f, 0);
            top.translate(0, progress, 0);

            middle.setChanged();
            top.setChanged();
        }

        public void tick(boolean lit) {
            if (this.lit != lit) {
                this.lit = lit;
                instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(PortableEnergyInterfaceRenderer.getMiddleForState(blockState, lit)))
                        .stealInstance(middle);
            }
        }

        public void remove() {
            middle.delete();
            top.delete();
        }

        public void collectCrumblingInstances(Consumer<Instance> consumer) {
            consumer.accept(middle);
            consumer.accept(top);
        }
    }

	/*
	@Override
	public void init() {
		instance.init(isLit());
	}

	@Override
	public void tick() {
		instance.tick(isLit());
	}

	@Override
	public void beginFrame() {
		instance.beginFrame(blockEntity.getExtensionDistance(AnimationTickHolder.getPartialTicks()));
	}

	@Override
	public void updateLight() {
		relight(pos, instance.middle, instance.top);
	}

	@Override
	public void remove() {
		instance.remove();
	}

	private boolean isLit() {
		return blockEntity.isConnected();
	}
	*/

    // I have no idea what PI stands for, but I'm guessing it's Portable Interface, so it works
    // for PORTABLE energy INTERFACE I guess.
	/*
	public static class PIInstance {
		private final VisualizationContext materialManager;
		private final BlockState blockState;
		private final BlockPos instancePos;
		private final float angleX;
		private final float angleY;
		private boolean lit;
		TransformedInstance middle;
		TransformedInstance top;

		public PIInstance(VisualizationContext materialManager, BlockState blockState, BlockPos instancePos) {
			this.materialManager = materialManager;
			this.blockState = blockState;
			this.instancePos = instancePos;
			Direction facing = blockState.getValue(PortableEnergyInterfaceBlock.FACING);
			this.angleX = facing == Direction.UP ? 0.0F : (facing == Direction.DOWN ? 180.0F : 90.0F);
			this.angleY = AngleHelper.horizontalAngle(facing);
		}

		public void init(boolean lit) {
			this.lit = lit;
			PartialModel middleForState = lit ? CAPartials.PORTABLE_ENERGY_INTERFACE_MIDDLE_POWERED : CAPartials.PORTABLE_ENERGY_INTERFACE_MIDDLE;
			this.middle = this.materialManager.defaultSolid().material(Materials.TRANSFORMED).getModel(middleForState, this.blockState).createInstance();
			this.top = this.materialManager.defaultSolid().material(Materials.TRANSFORMED).getModel(CAPartials.PORTABLE_ENERGY_INTERFACE_TOP, this.blockState).createInstance();
		}

		public void beginFrame(float progress) {
			this.middle.loadIdentity().translate(this.instancePos).centre().rotateY(this.angleY).rotateX(this.angleX).unCentre();
			this.top.loadIdentity().translate(this.instancePos).centre().rotateY(this.angleY).rotateX(this.angleX).unCentre();
			this.middle.translate(0.0D, progress * 0.5F + 0.375F, 0.0D);
			this.top.translate(0.0D, progress, 0.0D);
		}

		public void tick(boolean lit) {
			if (this.lit != lit) {
				this.lit = lit;
				PartialModel middleForState = lit ? CAPartials.PORTABLE_ENERGY_INTERFACE_MIDDLE_POWERED : CAPartials.PORTABLE_ENERGY_INTERFACE_MIDDLE;
				this.materialManager.defaultSolid().material(Materials.TRANSFORMED).getModel(middleForState, this.blockState).stealInstance(this.middle);
			}

		}

		public void remove() {
			this.middle.delete();
			this.top.delete();
		}
	}
	*/
}