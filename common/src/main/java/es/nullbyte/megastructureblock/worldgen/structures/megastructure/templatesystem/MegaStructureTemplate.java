package es.nullbyte.megastructureblock.worldgen.structures.megastructure.templatesystem;

import com.google.common.annotations.VisibleForTesting;
import es.nullbyte.megastructureblock.tools.AxisAlignedChunkArea;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class represents the logic to serialize, deserialize and spawn a megastructure template file on the world.
 * Subchunks are regular structure files (for now)
 */
public class MegaStructureTemplate extends StructureTemplate {

    private Vec3i size;//X AND Z ARE CHUNKS Y IS BLOCKS
    private String author;
    private String name;

    private final LinkedHashMap<Long, ResourceLocation> subChunkReferences = new LinkedHashMap<>(); //This can
    private ResourceLocation structureBase;
    public MegaStructureTemplate() {
        this("?");
    }

    public MegaStructureTemplate(String name) {
        this.size = Vec3i.ZERO;
        this.author = "?";
        this.name = name;
    }

    @Override
    public Vec3i getSize() {
        return this.size;
    }

    public void setSize(Vec3i size) {
        this.size = size;
    }
    @Override
    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public @NotNull String getAuthor() {
        return this.author;
    }

    public void setStructureBase(ResourceLocation structureBase) {
        this.structureBase = structureBase;
    }

    public ResourceLocation getStructureBase() {
        return this.structureBase;
    }

    public ResourceLocation getChunkLoc (int x, int z) {
        return subChunkReferences.get(ChunkPos.asLong(x, z));
    }

    /**
     * This method populates the header megaStructure data on the class. It doesn't manage sub chunks or
     * tags, just cretes the list of subchunks
     * @param activeCornerGlobalPosition
     * @param size
     */

    public void fillHeaderMetaData(BlockPos activeCornerGlobalPosition, Vec3i size,
                                                          ResourceLocation structureName) {
        if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
            //Calculate chunks and serialize
            // Calculate the opposite corner of the bounding box (corner block)
            BlockPos passiveCornerGlobalPosition = activeCornerGlobalPosition.offset(size).offset(-1, -1, -1);

            //As with the templates themselves, we will ALWAYS set the origin chunk to be the one closest to the origin:
            BlockPos startingPos = new BlockPos(Math.min(activeCornerGlobalPosition.getX(), passiveCornerGlobalPosition.getX()), Math.min(activeCornerGlobalPosition.getY(), passiveCornerGlobalPosition.getY()), Math.min(activeCornerGlobalPosition.getZ(), passiveCornerGlobalPosition.getZ()));
            BlockPos endPos = new BlockPos(Math.max(activeCornerGlobalPosition.getX(), passiveCornerGlobalPosition.getX()), Math.max(activeCornerGlobalPosition.getY(), passiveCornerGlobalPosition.getY()), Math.max(activeCornerGlobalPosition.getZ(), passiveCornerGlobalPosition.getZ()));

            //Crete the array of chunktemplates the aaca correclty encloses the chunks of the megastructure
            AxisAlignedChunkArea chunkArea = new AxisAlignedChunkArea(startingPos, endPos, size.getY());

            //Compute the number of rows and cols and crete the array
            this.name = structureName.getPath();
            this.size = new BlockPos(chunkArea.getNumberOfChunkColumns(), size.getY(),chunkArea.getNumberOfChunkRows());
            structureBase = structureName;
            //Fill references
            for (int i = 0; i < size.getZ(); i++) {
                for (int j = 0; j < size.getX(); j++) {
                    MegaStructureTemplate tempTemplate = new MegaStructureTemplate();

                    // Generate unique chunk paths
                    ResourceLocation chunkResource = ResourceLocation.fromNamespaceAndPath(
                            structureName.getNamespace(),
                            structureName.getPath() + "/chunk" + j + "_" + i
                    );

                    tempTemplate.setStructureBase(chunkResource);
                    tempTemplate.setSize(size);
                    subChunkReferences.put(ChunkPos.asLong(j, i), chunkResource);
                }
            }
        }
    }



    @Override
    public Vec3i getSize(Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                return new Vec3i(this.size.getZ(), this.size.getY(), this.size.getX());
            default:
                return this.size;
        }
    }

    @Override
    public BoundingBox getBoundingBox(BlockPos startPos, Rotation rotation, BlockPos pivotPos, Mirror mirror) {
        return getBoundingBox(startPos, rotation, pivotPos, mirror, this.size);
    }

    @Override
    public BlockPos getZeroPositionWithTransform(BlockPos targetPos, Mirror mirror, Rotation rotation) {
        return getZeroPositionWithTransform(targetPos, mirror, rotation, this.getSize().getX(), this.getSize().getZ());
    }

    @VisibleForTesting
    protected static BoundingBox getBoundingBox(BlockPos startPos, Rotation rotation, BlockPos pivotPos, Mirror mirror, Vec3i size) {
        Vec3i vec3i = size.offset(-1, -1, -1);
        BlockPos blockpos = transform(BlockPos.ZERO, mirror, rotation, pivotPos);
        BlockPos blockpos1 = transform(BlockPos.ZERO.offset(vec3i), mirror, rotation, pivotPos);
        return BoundingBox.fromCorners(blockpos, blockpos1).move(startPos);
    }

    public static BlockPos getZeroPositionWithTransform(BlockPos pos, Mirror mirror, Rotation rotation, int sizeX, int sizeZ) {
        --sizeX;
        --sizeZ;
        int i = mirror == Mirror.FRONT_BACK ? sizeX : 0;
        int j = mirror == Mirror.LEFT_RIGHT ? sizeZ : 0;
        BlockPos blockpos = pos;
        switch (rotation) {
            case COUNTERCLOCKWISE_90 -> blockpos = pos.offset(j, 0, sizeX - i);
            case CLOCKWISE_90 -> blockpos = pos.offset(sizeZ - j, 0, i);
            case CLOCKWISE_180 -> blockpos = pos.offset(sizeX - i, 0, sizeZ - j);
            case NONE -> blockpos = pos.offset(i, 0, j);
        }

        return blockpos;
    }

    /**
     * Serializes the metadata header on an nbt tag
     * @param tag
     * @return
     */
    @Override
    public CompoundTag save(CompoundTag tag) {
        if(subChunkReferences == null ||subChunkReferences.isEmpty()) {
            tag.put("chunkDataList", new ListTag());
            return tag;
        } else{

            ListTag chunkDataList = new ListTag();

            for (int  i = 0; i < size.getZ(); i++) {
                for(int j = 0; j < size.getX(); j++) {
                    CompoundTag chunkData = new CompoundTag();
                    chunkData.put("chunk_relative_pos",newIntegerList(j,i));
                    chunkData.putString("templateReference",subChunkReferences.get(ChunkPos.asLong(j,i)).toString());
                    chunkDataList.add(chunkData);
                }
            }
            tag.put("chunkDataList", chunkDataList);

        }

        tag.put("megastructuresize", this.newIntegerList(size.getX(), size.getY(), size.getZ()));
        tag.putString("name", this.name);

        return NbtUtils.addCurrentDataVersion(tag);
    }


    /**
     * NBT deserialization of a megastructure chunk file
     * tagtype for lists:
     * 0: Byte
     * 1: Short
     * 2: Int
     * 3: Long
     * 4: Float
     * 5: Double
     * 6: Byte Array
     * 7: String
     * 8: List
     * 9: List of List (nested list)
     * 10: CompoundTag
     * 11: Int Array
     * 12: Long Array
     * @param blockGetter
     * @param tag
     */
    @Override
    public void load(HolderGetter<Block> blockGetter, CompoundTag tag) {

        // Deserializing the megastructure size
        ListTag megaStructureSize = tag.getList("megastructuresize").get();
        this.size = new Vec3i(megaStructureSize.getInt(0).get(), megaStructureSize.getInt(1).get(), megaStructureSize.getInt(2).get());
        this.name = tag.getString("name").get();
        // Deserialize the chunkDataList (list of CompoundTags)
        ListTag chunkDataList = tag.getList("chunkDataList").get();
        for (int i = 0; i < chunkDataList.size(); ++i) {
            CompoundTag chunkData = chunkDataList.getCompound(i).get();

            // Retrieve the chunk relative position (x, z)
            ListTag chunkRelativePos = chunkData.getList("chunk_relative_pos").get(); // Expecting a list of integers
            int chunkX = chunkRelativePos.getInt(0).get(); // Assuming 0th index is x
            int chunkZ = chunkRelativePos.getInt(1).get(); // Assuming 1st index is z

            // Retrieve the template reference (likely a ResourceLocation)
            String templateReferenceStr = chunkData.getString("templateReference").get();
            ResourceLocation templateReference = ResourceLocation.parse(templateReferenceStr); // Convert to ResourceLocation
            subChunkReferences.put(ChunkPos.asLong(chunkX, chunkZ), templateReference);
//            this.templateChunkList[chunkZ][chunkX] = new MegaStructureTemplate(templateReference); // Example of how you might load the template

        }
    }


    private ListTag newIntegerList(int... values) {
        ListTag listtag = new ListTag();

        for(int i : values) {
            listtag.add(IntTag.valueOf(i));
        }

        return listtag;
    }

}
