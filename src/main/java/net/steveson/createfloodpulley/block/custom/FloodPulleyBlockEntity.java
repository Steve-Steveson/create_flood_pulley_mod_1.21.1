package net.steveson.createfloodpulley.block.custom;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyBlock;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyFluidHandler;
import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.simibubi.create.content.fluids.transfer.FluidFillingBehaviour;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.steveson.createfloodpulley.block.CFPBlockEntities;
import net.steveson.createfloodpulley.util.MoreIcons;

import java.util.List;

public class FloodPulleyBlockEntity extends KineticBlockEntity {

    LerpedFloat offset;
    boolean isMoving;

    private SmartFluidTank internalTank;
    private FluidDrainingBehaviour drainer;
    private FluidFillingBehaviour filler;
    private HosePulleyFluidHandler handler;

    public ScrollOptionBehaviour<ShapeMode> shapeMode;

    public enum ShapeMode implements INamedIconOptions {

        SHAPE_CUBE(MoreIcons.I_CUBE),
        SHAPE_CYLINDER(MoreIcons.I_CYLINDER)
        ;

        private String translationKey;
        private AllIcons icon;

        private ShapeMode(AllIcons icon) {
            this.icon = icon;
            translationKey = "create_flood_pulley.shape_mode." + Lang.asId(name());
        }

        @Override
        public AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }
    }


    public FloodPulleyBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        offset = LerpedFloat.linear()
                .startWithValue(0);
        isMoving = true;
        internalTank = new SmartFluidTank(1500, this::onTankContentsChanged);
        handler = new HosePulleyFluidHandler(internalTank, filler, drainer,
                () -> worldPosition.below((int) Math.ceil(offset.getValue())), () -> !this.isMoving);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CFPBlockEntities.FLOOD_PULLEY.get(),
                (be, context) -> {
                    if (context == null || FloodPulleyBlock.hasPipeTowards(be.level, be.worldPosition, be.getBlockState(), context))
                        return be.handler;
                    return null;
                }
        );
    }

//

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean addToGoggleTooltip = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
//
        return addToGoggleTooltip;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        drainer = new FluidDrainingBehaviour(this);
        filler = new FluidFillingBehaviour(this);
        behaviours.add(drainer);
        behaviours.add(filler);
        super.addBehaviours(behaviours);
        shapeMode = new ScrollOptionBehaviour<>(ShapeMode.class, Component.translatable("create_flood_pulley.shape_mode"),this, getShapeModeSlot());

        behaviours.add(shapeMode);
    }

    protected ValueBoxTransform getShapeModeSlot() {
        return new CenteredSideValueBoxTransform((state, d) -> d == Direction.UP);
    }




    protected void onTankContentsChanged(FluidStack contents) {}

    @Override
    public void onSpeedChanged(float previousSpeed) {
        isMoving = true;
        if (getSpeed() == 0) {
            offset.forceNextSync();
            offset.setValue(Math.round(offset.getValue()));
            isMoving = false;
        }

        if (isMoving) {
            float newOffset = offset.getValue() + getMovementSpeed();
            if (newOffset < 0)
                isMoving = false;
            if (!level.getBlockState(worldPosition.below((int) Math.ceil(newOffset)))
                    .canBeReplaced()) {
                isMoving = false;
            }
            if (isMoving) {
                drainer.reset();
                filler.reset();
            }
        }

        super.onSpeedChanged(previousSpeed);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().expandTowards(0, -offset.getValue(), 0);
    }

    @Override
    public void tick() {
        super.tick();
        float newOffset = offset.getValue() + getMovementSpeed();
        if (newOffset < 0) {
            newOffset = 0;
            isMoving = false;
        }
        if (!level.getBlockState(worldPosition.below((int) Math.ceil(newOffset)))
                .canBeReplaced()) {
            newOffset = (int) newOffset;
            isMoving = false;
        }
        if (getSpeed() == 0)
            isMoving = false;

        offset.setValue(newOffset);
        invalidateRenderBoundingBox();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level.isClientSide)
            return;
        if (isMoving)
            return;

        int ceil = (int) Math.ceil(offset.getValue() + getMovementSpeed());
        if (getMovementSpeed() > 0 && level.getBlockState(worldPosition.below(ceil))
                .canBeReplaced()) {
            isMoving = true;
            drainer.reset();
            filler.reset();
            return;
        }

        sendData();
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        if (clientPacket)
            offset.forceNextSync();
        compound.put("Offset", offset.writeNBT());
        compound.put("Tank", internalTank.writeToNBT(registries, new CompoundTag()));
        super.write(compound, registries, clientPacket);
//
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        offset.readNBT(compound.getCompound("Offset"), clientPacket);

        internalTank.readFromNBT(registries, compound.getCompound("Tank"));
        super.read(compound, registries, clientPacket);
//
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateCapabilities();
    }

    public float getMovementSpeed() {
        float movementSpeed = convertToLinear(getSpeed());
        if (level.isClientSide)
            movementSpeed *= ServerSpeedProvider.get();
        return movementSpeed;
    }

    public float getInterpolatedOffset(float pt) {
        return Math.max(offset.getValue(pt), 3 / 16f);
    }
}
