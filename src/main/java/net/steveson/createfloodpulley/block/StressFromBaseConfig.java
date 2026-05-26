package net.steveson.createfloodpulley.block;


import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CStress;

//This class allows my Flood Pulley to get its stress value from the config for the Hose Pulley from base Create.
//I copied it from Create: Bits 'n' Bobs, which is allowed under the MIT License.

public class StressFromBaseConfig {

    public static void register() {
        BlockStressValues.IMPACTS.registerProvider((p) -> {
            if (CFPBlocks.FLOOD_PULLEY.is(p)) {
                final CStress stress = AllConfigs.server().kinetics.stressValues;
                return stress.getImpact(AllBlocks.HOSE_PULLEY.get());
            }
            return null;
        });
    }

}
