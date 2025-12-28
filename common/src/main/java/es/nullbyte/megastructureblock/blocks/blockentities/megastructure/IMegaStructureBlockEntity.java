package es.nullbyte.megastructureblock.blocks.blockentities.megastructure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BoundingBoxRenderable;


/**
 * Common Block Entity Interface for the MegasSTructure block so that it can be safely referenced from common code.
 */
public interface IMegaStructureBlockEntity extends BoundingBoxRenderable {


    // Structure name handling
    String getStructureName();
    boolean hasStructureName();
    void setStructureName(String structureName);
    void setStructureName(Identifier structureName);

    // Author
    void createdBy(LivingEntity author);


    // Position and size
    BlockPos getStructurePos();
    void setStructurePos(BlockPos structurePos);
    Vec3i getStructureSize();
    void setStructureSize(Vec3i structureSize);

    // Transformations
    Mirror getMirror();
    void setMirror(Mirror mirror);
    Rotation getRotation();
    void setRotation(Rotation rotation);

    // MetaData
    String getMetaData();
    void setMetaData(String metaData);

    // Mode
    MegaStructureMode getMode();
    void setMode(MegaStructureMode mode);

    // Flags
    boolean isIgnoreEntities();
    void setIgnoreEntities(boolean ignoreEntities);
    boolean isPowered();
    void setPowered(boolean powered);
    boolean getShowAir();
    void setShowAir(boolean showAir);
    boolean getShowBoundingBox();
    void setShowBoundingBox(boolean showBoundingBox);

    // Integrity and seed
    float getIntegrity();
    void setIntegrity(float integrity);
    long getSeed();
    void setSeed(long seed);

    // Operations
    boolean detectSize();
    boolean saveStructure();
    boolean saveStructure(boolean write);
    boolean saveMegaStructure(boolean writeToDisk);
    boolean placeStructureIfSameSize(ServerLevel level);
    boolean loadStructureInfo(ServerLevel level);
    void placeStructure(ServerLevel level);
    void unloadStructure();
    boolean isStructureLoadable();

    // Possibly default or static helper methods could be in a separate utility class

}
