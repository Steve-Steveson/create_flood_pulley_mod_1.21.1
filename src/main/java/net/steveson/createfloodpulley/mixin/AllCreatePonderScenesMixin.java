package net.steveson.createfloodpulley.mixin;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderScenes;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.steveson.createfloodpulley.foundation.ponder.create.UseBaseCreatePonderScenes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//This class copied from Create: Bits 'n' Bobs, which is allowed under the MIT License.
@Mixin(value = AllCreatePonderScenes.class, remap = false)
public class AllCreatePonderScenesMixin {

    @Inject(method = "register", at = @At("TAIL"))
    private static void register(PonderSceneRegistrationHelper<ResourceLocation> helper, CallbackInfo ci) {
        UseBaseCreatePonderScenes.register(helper);
    }
}
