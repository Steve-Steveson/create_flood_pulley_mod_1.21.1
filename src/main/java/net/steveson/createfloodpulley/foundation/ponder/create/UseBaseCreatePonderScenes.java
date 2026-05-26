package net.steveson.createfloodpulley.foundation.ponder.create;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.simibubi.create.infrastructure.ponder.scenes.fluid.HosePulleyScenes;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.steveson.createfloodpulley.block.CFPBlocks;

//This class based on code from Create: Bits 'n' Bobs, which is allowed under the MIT License.
public class UseBaseCreatePonderScenes {

    public static void register(final PonderSceneRegistrationHelper<ResourceLocation> helper) {
        final PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);


        HELPER.forComponents(CFPBlocks.FLOOD_PULLEY)
                .addStoryBoard("hose_pulley/intro", HosePulleyScenes::intro, AllCreatePonderTags.FLUIDS, AllCreatePonderTags.KINETIC_APPLIANCES)
                .addStoryBoard("hose_pulley/level", HosePulleyScenes::level);
    }
}
