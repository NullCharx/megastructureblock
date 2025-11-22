package es.nullbyte.megastructureblock.blocks.blockentities;

import com.mojang.serialization.Codec;
import es.nullbyte.megastructureblock.blocks.ModBlockDefintions;
import es.nullbyte.megastructureblock.blocks.blockentities.megastructure.IMegaStructureBlockEntity;
import es.nullbyte.megastructureblock.blocks.blockentities.megastructure.MegaStructureMode;
import es.nullbyte.megastructureblock.blocks.megastructures.MegaStructureBlock;
import es.nullbyte.megastructureblock.client.guis.ClientScreens;
import es.nullbyte.megastructureblock.worldgen.structures.megastructure.templatesystem.MegaStructureTemplate;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.List;


//TODO separate the header and structurechunks in two buttons manual with more shit!!!
public class MegaStructureBlockEntity extends BlockEntity implements IMegaStructureBlockEntity {

    private static final int SCAN_CORNER_BLOCKS_RANGE = 5;
    public static final int MAX_OFFSET_PER_AXIS = 16*80;
    public static final int MAX_SIZE_PER_AXIS = MAX_OFFSET_PER_AXIS*2;
    public static final String AUTHOR_TAG = "author";
    public static final String MEGASTRUCTURE_PREFIX = "mgst_";

    @Nullable
    private ResourceLocation structureName;
    private String author = "";
    private String metaData = "";
    private BlockPos structurePos = new BlockPos(0, 1, 0);
    private Vec3i structureSize;
    private Mirror mirror;
    private Rotation rotation;
    private MegaStructureMode mode;
    private boolean ignoreEntities;
    private boolean powered;
    private boolean showAir;
    private boolean showBoundingBox;
    private float integrity;
    private long seed;
    private final BlockPos absolutePositioning = new BlockPos(0, 0, 0);

    public MegaStructureBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityDefintions.MEGASTRUCTURE_BLOCK_ENTITY.get(), pos, blockState);
        this.structureSize = Vec3i.ZERO;
        this.mirror = Mirror.NONE;
        this.rotation = Rotation.NONE;
        this.ignoreEntities = true;
        this.showBoundingBox = true;
        this.integrity = 1.0F;
        this.mode = blockState.getValue(MegaStructureBlock.MODE);
    }

    //https://github.com/neoforged/.github/blob/main/primers/1.21.6/index.md
    @Override
    //protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("name", this.getStructureName());
        out.putString("author", this.author);
        out.putString("metadata", this.metaData);
        out.putInt("posX", this.structurePos.getX());
        out.putInt("posY", this.structurePos.getY());
        out.putInt("posZ", this.structurePos.getZ());
        out.putInt("sizeX", this.structureSize.getX());
        out.putInt("sizeY", this.structureSize.getY());
        out.putInt("sizeZ", this.structureSize.getZ());
        out.putString("rotation", this.rotation.toString());
        out.putString("mirror", this.mirror.toString());
        out.putString("mode", this.mode.toString());
        out.putBoolean("ignoreEntities", this.ignoreEntities);
        out.putBoolean("powered", this.powered);
        out.putBoolean("showair", this.showAir);
        out.putBoolean("showboundingbox", this.showBoundingBox);
        out.putFloat("integrity", this.integrity);
        out.putLong("seed", this.seed);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        this.setStructureName(in.getStringOr("name",""));
        this.author = in.getStringOr("author","");
        this.metaData = in.getStringOr("metadata","");
        int i = Mth.clamp(in.getIntOr("posX",420), -MAX_OFFSET_PER_AXIS, MAX_OFFSET_PER_AXIS);
        int j = Mth.clamp(in.getIntOr("posY",-69), -MAX_OFFSET_PER_AXIS, MAX_OFFSET_PER_AXIS);
        int k = Mth.clamp(in.getIntOr("posZ", 777), -MAX_OFFSET_PER_AXIS, MAX_OFFSET_PER_AXIS);
        this.structurePos = new BlockPos(i, j, k);
        int l = Mth.clamp(in.getIntOr("sizeX",13), 0, MAX_SIZE_PER_AXIS);
        int i1 = Mth.clamp(in.getIntOr("sizeY",5), 0, MAX_SIZE_PER_AXIS);
        int j1 = Mth.clamp(in.getIntOr("sizeZ",8), 0, MAX_SIZE_PER_AXIS);
        this.structureSize = new Vec3i(l, i1, j1);

        try {
            this.rotation = Rotation.valueOf(in.getStringOr("rotation",""));
        } catch (IllegalArgumentException var12) {
            this.rotation = Rotation.NONE;
        }

        try {
            this.mirror = Mirror.valueOf(in.getStringOr("mirror",""));
        } catch (IllegalArgumentException var11) {
            this.mirror = Mirror.NONE;
        }

        try {
            this.mode = MegaStructureMode.valueOf(in.getStringOr("mode",""));
        } catch (IllegalArgumentException var10) {
            this.mode = MegaStructureMode.DATA;
        }

        this.ignoreEntities = in.getBooleanOr("ignoreEntities",true);
        this.powered = in.getBooleanOr("powered",false);
        this.showAir = in.getBooleanOr("showair",false);
        this.showBoundingBox = in.getBooleanOr("showboundingbox", true);

        float integrityf = in.getFloatOr("integrity",Float.MIN_VALUE);
        this.integrity = integrityf != Float.MIN_VALUE ? integrityf : 1.0F;

        this.seed = in.getLongOr("seed",1);
        this.updateBlockState();
    }

    private void updateBlockState() {
        if (this.level != null) {
            BlockPos blockpos = this.getBlockPos();
            BlockState blockstate = this.level.getBlockState(blockpos);
            if (blockstate.is(Blocks.STRUCTURE_BLOCK)) {
                this.level.setBlock(blockpos, blockstate.setValue(MegaStructureBlock.MODE, this.mode), 2);
            }
        }

    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }

    public boolean usedBy(Player player) {
        if (!player.canUseGameMasterBlocks()) {
            return false;
        } else {
            if (level.isClientSide) {
                ClientScreens.openMegaStructureBlockSreen(this);
            }
            return true;
        }
    }

    public String getStructureName() {
        return this.structureName == null ? "" : this.structureName.toString();
    }

    public boolean hasStructureName() {
        return this.structureName != null;
    }

    public void setStructureName(@Nullable String structureName) {
        this.setStructureName(StringUtil.isNullOrEmpty(structureName) ? null : ResourceLocation.tryParse(parseMegaStructureName(structureName)));
    }

    private String parseMegaStructureName(@Nullable String structureName) {
        // Handle null or empty input early
        if (StringUtil.isNullOrEmpty(structureName)) {
            return structureName;  // No change if it's null or empty
        }

        int namespaceIndex = structureName.indexOf(':');  // Check for namespace presence

        if (namespaceIndex >= 0) {
            // If the name has a namespace, get the part after ':'
            String actualName = structureName.substring(namespaceIndex + 1);
            // Check if the prefix is correct, otherwise prepend it
            if (!actualName.startsWith(MEGASTRUCTURE_PREFIX)) {
                structureName = structureName.substring(0, namespaceIndex + 1) + MEGASTRUCTURE_PREFIX + actualName;
            }
        } else {
            // If the name has no namespace, check if it starts with the prefix
            if (!structureName.startsWith(MEGASTRUCTURE_PREFIX)) {
                structureName = MEGASTRUCTURE_PREFIX + structureName;
            }
        }

        return structureName;
    }

    public void setStructureName(@Nullable ResourceLocation structureName) {
        this.structureName = structureName;
    }

    public void createdBy(LivingEntity author) {
        this.author = author.getName().getString();
    }

    public BlockPos getStructurePos() {
        return this.structurePos;
    }

    public void setStructurePos(BlockPos structurePos) {
        this.structurePos = structurePos;
    }

    public Vec3i getStructureSize() {
        return this.structureSize;
    }

    public void setStructureSize(Vec3i structureSize) {
        this.structureSize = structureSize;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public void setMirror(Mirror mirror) {
        this.mirror = mirror;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public String getMetaData() {
        return this.metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public MegaStructureMode getMode() {
        return this.mode;
    }

    public void setMode(MegaStructureMode mode) {
        this.mode = mode;
        BlockState blockstate = this.level.getBlockState(this.getBlockPos());
        if (blockstate.is(Blocks.STRUCTURE_BLOCK)) {
            this.level.setBlock(this.getBlockPos(), blockstate.setValue(MegaStructureBlock.MODE, mode), 2);
        }

    }

    public boolean isIgnoreEntities() {
        return this.ignoreEntities;
    }

    public void setIgnoreEntities(boolean ignoreEntities) {
        this.ignoreEntities = ignoreEntities;
    }

    public float getIntegrity() {
        return this.integrity;
    }

    public void setIntegrity(float integrity) {
        this.integrity = integrity;
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public boolean detectSize() {
        if (this.mode != MegaStructureMode.SAVE) {
            return false;
        } else {
            BlockPos blockpos = this.getBlockPos();
            int i = 512;
            BlockPos blockpos1 = new BlockPos(blockpos.getX() - 80, this.level.getMinY(), blockpos.getZ() - 80);
            BlockPos blockpos2 = new BlockPos(blockpos.getX() + 80, this.level.getMaxY(), blockpos.getZ() + 80);
            Stream<BlockPos> stream = this.getRelatedCorners(blockpos1, blockpos2);
            return calculateEnclosingBoundingBox(blockpos, stream).filter((p_155790_) -> {
                int j = p_155790_.maxX() - p_155790_.minX();
                int k = p_155790_.maxY() - p_155790_.minY();
                int l = p_155790_.maxZ() - p_155790_.minZ();
                if (j > 1 && k > 1 && l > 1) {
                    this.structurePos = new BlockPos(p_155790_.minX() - blockpos.getX() + 1, p_155790_.minY() - blockpos.getY() + 1, p_155790_.minZ() - blockpos.getZ() + 1);
                    this.structureSize = new Vec3i(j - 1, k - 1, l - 1);
                    this.setChanged();
                    BlockState blockstate = this.level.getBlockState(blockpos);
                    this.level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
                    return true;
                } else {
                    return false;
                }
            }).isPresent();
        }
    }


    private Stream<BlockPos> getRelatedCorners(BlockPos minPos, BlockPos maxPos) {
        // Stream of block positions in the specified range
        Stream<BlockPos> var10000 = BlockPos.betweenClosedStream(minPos, maxPos)
                .filter(p_272561_ -> this.level.getBlockState(p_272561_).is(ModBlockDefintions.MEGASTRUCTURE_BLOCK.get()));

        Level level = this.level;
        Objects.requireNonNull(level);

        // Fixing the ambiguity by using a lambda expression
        return var10000.map(level::getBlockEntity)  // Explicitly reference getBlockEntity with BlockPos
                .filter(blockEntity -> blockEntity instanceof MegaStructureBlockEntity) // Filter for MegaStructureBlockEntity
                .map(p_155785_ -> (MegaStructureBlockEntity) p_155785_) // Cast to MegaStructureBlockEntity
                .filter(blockEntity -> blockEntity.mode == MegaStructureMode.CRNR && Objects.equals(this.structureName, blockEntity.structureName)) // Additional filtering
                .map(BlockEntity::getBlockPos); // Map to BlockPos
    }


    private static Optional<BoundingBox> calculateEnclosingBoundingBox(BlockPos pos, Stream<BlockPos> relatedCorners) {
        Iterator<BlockPos> iterator = relatedCorners.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        } else {
            BlockPos blockpos = iterator.next();
            BoundingBox boundingbox = new BoundingBox(blockpos);
            if (iterator.hasNext()) {
                Objects.requireNonNull(boundingbox);
                iterator.forEachRemaining(boundingbox::encapsulate);
            } else {
                boundingbox.encapsulate(pos);
            }

            return Optional.of(boundingbox);
        }
    }

    public boolean saveStructure() {
        return saveStructure(true);
    }

    public boolean saveStructure(boolean write) {
        if  (this.mode == MegaStructureMode.SAVE) {
            return this.saveMegaStructure(write);
        } else {
            return false;
        }
    }
    /**
     *
     * Here we do not care about the logic of positioning the megastructure in the world. Here we just need to
     * serialize the megastructure into constitutent chunks.
     * @param writeToDisk
     * @return
     */
    public boolean saveMegaStructure(boolean writeToDisk) {
        if (this.structureName == null) {
            return false;
        } else {
            BlockPos blockpos = this.getBlockPos().offset(this.structurePos);
            ServerLevel serverlevel = (ServerLevel)this.level;
            StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

            //The first structure template created is the header with the common metadata.
            MegaStructureTemplate megaStructureHeaderTemplate;
            try {
                megaStructureHeaderTemplate = (MegaStructureTemplate) structuretemplatemanager.getOrCreate(this.structureName);
            } catch (ResourceLocationException var8) {
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            //Fill the header data
            megaStructureHeaderTemplate.fillHeaderMetaData(blockpos, structureSize, structureName);
            megaStructureHeaderTemplate.setAuthor(this.author);
            if (writeToDisk) {
                try {
                    if (!structuretemplatemanager.save(this.structureName)){
                        return false;
                    }
                } catch (ResourceLocationException var7) {
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            BlockPos passiveCornerGlobalPosition = blockpos.offset(structureSize).offset(-1, -1, -1);

            //As with the templates themselves, we will ALWAYS set the origin chunk to be the one closest to the origin:
            BlockPos startingPos = new BlockPos(Math.min(blockpos.getX(), passiveCornerGlobalPosition.getX()), Math.min(blockpos.getY(), passiveCornerGlobalPosition.getY()), Math.min(blockpos.getZ(), passiveCornerGlobalPosition.getZ()));
            ChunkPos startingChunk = new ChunkPos(startingPos);

            for (int i = 0; i< megaStructureHeaderTemplate.getSize().getX(); i++){
                for (int j = 0; j< megaStructureHeaderTemplate.getSize().getZ(); j++){

                    BlockPos chunkBlockPos = new BlockPos(startingChunk.getMinBlockX()+(16*j),startingPos.getY(),startingChunk.getMinBlockZ()+(16*i)); //maybe this + the size works¿?
                    Vec3i chunkSize = new Vec3i(16,structureSize.getY(), 16);
                    StructureTemplate chunKTemplate;
                    ResourceLocation chunKTemplateLoc =  megaStructureHeaderTemplate.getChunkLoc(j,i);

                    try {
                        chunKTemplate = structuretemplatemanager.getOrCreate(chunKTemplateLoc);
                    } catch (ResourceLocationException var8) {
                        return false;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }

                    chunKTemplate.fillFromWorld(level, chunkBlockPos, chunkSize, !ignoreEntities,  List.of(Blocks.STRUCTURE_VOID));//Stream.concat(List.of().stream(), Stream.of(Blocks.STRUCTURE_VOID)).toList());
                    megaStructureHeaderTemplate.setAuthor(this.structureName.toString());

                    if (writeToDisk) {
                        try {
                            if (!structuretemplatemanager.save(chunKTemplateLoc)){
                                return false;
                            }
                        } catch (ResourceLocationException var7) {
                            return false;
                        } catch (Exception e) {
                                    e.printStackTrace();
                            return false;
                        }
                    }
                }
            }

        return true;
        }
    }

    public static RandomSource createRandom(long seed) {
        return seed == 0L ? RandomSource.create(Util.getMillis()) : RandomSource.create(seed);
    }

    public boolean placeStructureIfSameSize(ServerLevel level) {
        if (this.mode == MegaStructureMode.LOAD && this.structureName != null) {
            MegaStructureTemplate structuretemplate = (MegaStructureTemplate)level.getStructureManager().get(this.structureName).orElse(null);
            if (structuretemplate == null) {
                return false;
            } else if (structuretemplate.getSize().equals(this.structureSize)) { //The problem is somehow here¿
                return this.placeStructure(level, structuretemplate);
            } else {
                this.loadStructureInfo(structuretemplate);
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean loadStructureInfo(ServerLevel level) {
        MegaStructureTemplate structuretemplate = this.getStructureTemplate(level);
        if (structuretemplate == null) {
            return false;
        } else {
            this.loadStructureInfo(structuretemplate);
            return true;
        }
    }

    private void loadStructureInfo(MegaStructureTemplate structureTemplate) {
        this.author = !StringUtil.isNullOrEmpty(structureTemplate.getAuthor()) ? structureTemplate.getAuthor() : "";
        this.structureSize = structureTemplate.getSize();
        this.setChanged();
    }
    private void loadStructureInfo(StructureTemplate structureTemplate) {
        this.author = !StringUtil.isNullOrEmpty(structureTemplate.getAuthor()) ? structureTemplate.getAuthor() : "";
        this.structureSize = structureTemplate.getSize();
        this.setChanged();
    }
    public void placeStructure(ServerLevel level) {
        MegaStructureTemplate structuretemplate = this.getStructureTemplate(level);
        if (structuretemplate != null) {
            this.placeStructure(level, structuretemplate);
        }

    }

    @Nullable
    private MegaStructureTemplate getStructureTemplate(ServerLevel level) {
        return this.structureName == null ? null : (MegaStructureTemplate) level.getStructureManager().get(this.structureName).orElse(null);
    }

    private boolean placeStructure(ServerLevel level, MegaStructureTemplate headerTemplate) {
        this.loadStructureInfo(headerTemplate);
        BlockPos blockpos = this.getBlockPos().offset(this.structurePos);
        ServerLevel serverlevel = (ServerLevel)this.level;
        StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

        BlockPos passiveCornerGlobalPosition = blockpos.offset(structureSize).offset(-1, -1, -1);
        BlockPos startingPos = new BlockPos(Math.min(blockpos.getX(), passiveCornerGlobalPosition.getX()), Math.min(blockpos.getY(), passiveCornerGlobalPosition.getY()), Math.min(blockpos.getZ(), passiveCornerGlobalPosition.getZ()));
        ChunkPos startingChunk = new ChunkPos(startingPos);
        for(int i = 0; i < headerTemplate.getSize().getX(); i++) {
            for(int j = 0; i < headerTemplate.getSize().getZ(); j++) {
                Optional<StructureTemplate> megaStructureChunkOpt;
                try {
                    megaStructureChunkOpt = structuretemplatemanager.get(headerTemplate.getChunkLoc(j,i));

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                if (!megaStructureChunkOpt.isPresent()){
                    return false;
                }
                StructureTemplate megaStructureChunk = megaStructureChunkOpt.get();
                this.loadStructureInfo(megaStructureChunk);
                StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings()).setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(this.ignoreEntities);
                if (this.integrity < 1.0F) {
                    structureplacesettings.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
                }
                BlockPos chunkBlockPos = new BlockPos(startingChunk.getMinBlockX()+(16*j),startingPos.getY(),startingChunk.getMinBlockZ()+(16*i));
//                Vec3i chunkSize = new Vec3i(16,structureSize.getY(), 16);
//                MegaStructureTemplate chunKTemplate;
                if (!megaStructureChunk.placeInWorld(level, chunkBlockPos, chunkBlockPos, structureplacesettings, createRandom(this.seed), 2)){
                    return false;
                }


            }
        }
        return true;
    }

    public void unloadStructure() {
        if (this.structureName != null) {
            ServerLevel serverlevel = (ServerLevel)this.level;
            StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();
            structuretemplatemanager.remove(this.structureName);
        }

    }

    public boolean isStructureLoadable() {
        if (this.mode == MegaStructureMode.LOAD && !this.level.isClientSide && this.structureName != null) {
            ServerLevel serverlevel = (ServerLevel)this.level;
            StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

            try {
                return structuretemplatemanager.get(this.structureName).isPresent();
            } catch (ResourceLocationException var4) {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    public boolean getShowAir() {
        return this.showAir;
    }

    public void setShowAir(boolean showAir) {
        this.showAir = showAir;
    }

    public boolean getShowBoundingBox() {
        return this.showBoundingBox;
    }

    public void setShowBoundingBox(boolean showBoundingBox) {
        this.showBoundingBox = showBoundingBox;
    }

    public enum UpdateType implements  StringRepresentable{
        UPD8_AREA,
        SAVE_AREA,
        LOAD_AREA,
        SCAN_AREA;
        public static final Codec<UpdateType> CODEC = StringRepresentable.fromEnum(UpdateType::values);

        UpdateType() {
        }

        @Override
        public String getSerializedName() {
            return "updateType";
        }
    }

}
