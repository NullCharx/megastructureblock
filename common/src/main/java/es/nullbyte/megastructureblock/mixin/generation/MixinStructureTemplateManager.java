package es.nullbyte.megastructureblock.mixin.generation;

import com.mojang.datafixers.DataFixer;
import es.nullbyte.megastructureblock.worldgen.structures.megastructure.templatesystem.MegaStructureTemplate;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@Mixin(StructureTemplateManager.class)
public class MixinStructureTemplateManager {
    @Mutable @Shadow @Final
    private HolderGetter<Block> blockLookup;
    @Mutable @Shadow @Final
    private DataFixer fixerUpper;
    @Mutable @Shadow @Final
    private Map<ResourceLocation, Optional<StructureTemplate>> structureRepository;

//Test the miin, If it doesnt work then you have to diwncast  everything coming from th template manager


    /**
     * Modifies the vanilla `StructureTemplateManager` to correctly load MegaStructures.
     * This method expects MegaStructure NBT data to contain a separate "MegaStructureData" tag
     * or any exclusive MegaStructure-related data inside the loaded tag.
     *
     * @param nbt The NBT data containing the structure information.
     * @param cir The callback info for modifying the return value.
     */
    @Inject(method = "readStructure(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;load(Lnet/minecraft/core/HolderGetter;Lnet/minecraft/nbt/CompoundTag;)V")
            ,cancellable = true)
    private void onReadStructure(CompoundTag nbt, CallbackInfoReturnable<StructureTemplate> cir) {
        if (nbt.contains("megastructuresize") || nbt.contains("ismegastructurechunk")) { // Check for MegaStructure
            MegaStructureTemplate megaTemplate = new MegaStructureTemplate(nbt.getString("name").get());
            int i = NbtUtils.getDataVersion(nbt, 500);
            megaTemplate.load(this.blockLookup, DataFixTypes.STRUCTURE.updateToCurrentVersion(this.fixerUpper, nbt, i));
            cir.setReturnValue(megaTemplate); // Override return value
        }
    }

    /**
     * Modifies the `getOrCreate` method in `StructureTemplateManager` to ensure that
     * `MegaStructureTemplate` instances are correctly instantiated instead of regular `StructureTemplate` instances.
     *
     * This method assumes that any MegaStructure has the prefix `"mgst_"` at the beginning
     * of its name (after the namespace). This behavior aligns with the expected naming convention
     * used by MegaStructure blocks/entities.
     *
     * @param id  The `ResourceLocation` of the structure being retrieved or created.
     * @param cir The callback info for modifying the return value.
     */
    @Inject(method = "getOrCreate", at = @At(value = "NEW",target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;"),
            cancellable = true)
    private void onGetOrCreate(ResourceLocation id, CallbackInfoReturnable<StructureTemplate> cir) {
        String path = id.getPath();
        if (path.startsWith("mgst_") && !path.contains("/")) {
            // Replace the StructureTemplate with a subclass
            MegaStructureTemplate customTemplate = new MegaStructureTemplate();
            this.structureRepository.put(id, Optional.of(customTemplate));
            // Return the new instance
            cir.setReturnValue(customTemplate);
        }
    }

    @Inject (method = "tryLoad", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    private void onTryLoading(ResourceLocation id, CallbackInfoReturnable<Optional<MegaStructureTemplate>> cir) {
        if(cir.getReturnValue().get() instanceof MegaStructureTemplate) {
            cir.setReturnValue(Optional.of(cir.getReturnValue().get()));
        }
    }

    @Inject (method = "loadFromResource", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    private void onLoadingFromResource(ResourceLocation id, CallbackInfoReturnable<Optional<MegaStructureTemplate>> cir) {
        if(cir.getReturnValue().get() instanceof MegaStructureTemplate) {
            cir.setReturnValue(Optional.of(cir.getReturnValue().get()));
        }
    }

    @Inject (method = "loadFromTestStructures", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    private void onLoadingFromTestStructure(ResourceLocation id, CallbackInfoReturnable<Optional<MegaStructureTemplate>> cir) {
        if(cir.getReturnValue().isPresent() && cir.getReturnValue().get() instanceof MegaStructureTemplate) {
            cir.setReturnValue(Optional.of(cir.getReturnValue().get()));
        }
    }


    @Inject (method = "loadFromGenerated", at = @At(value = "RETURN"), cancellable = true)
    private void onLoadingFromGenerated(ResourceLocation id, CallbackInfoReturnable<Optional<MegaStructureTemplate>> cir) {
        if(cir.getReturnValue().isPresent() && cir.getReturnValue().get() instanceof MegaStructureTemplate) {
            cir.setReturnValue(Optional.of(cir.getReturnValue().get()));
        }
    }

    @Inject (method = "loadFromSnbt", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void onLoadingFromSnbt(ResourceLocation id, Path path, CallbackInfoReturnable<Optional<MegaStructureTemplate>> cir) {
        if(cir.getReturnValue().isPresent() && cir.getReturnValue().get() instanceof MegaStructureTemplate) {
            cir.setReturnValue(Optional.of(cir.getReturnValue().get()));
        }

    }



//    @Inject(method = "readStructure(Ljava/io/InputStream;)Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;",
//            at = @At(value = "RETURN", ordinal = 0), cancellable = true)
//    private void onReadCompressedStructure(InputStream stream, CallbackInfoReturnable<MegaStructureTemplate> cir) {
//        if(cir.getReturnValue() instanceof MegaStructureTemplate) {
//            cir.setReturnValue((MegaStructureTemplate)cir.getReturnValue());
//        }
//    }

}