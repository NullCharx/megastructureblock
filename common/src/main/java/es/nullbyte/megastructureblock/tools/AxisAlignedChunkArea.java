package es.nullbyte.megastructureblock.tools;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an axis-aligned chunk area defined by a bounding box and radii.
 * The max radius represents the whole area.
 * The min radius represents the radius below which the bounding box should NOT operate.
 */
public class AxisAlignedChunkArea extends AABB {

    private final int generalRadius; /** The maximum radius from the center to the outer edge on chunks **/
    private final int substractRadius; /** The minimum radius from the center to the inner edge on chunks **/
    private final TaskShape taskShape; /** Shape algorithm used for the task **/
    public static final Codec<AxisAlignedChunkArea> EASY_CHUNKPOS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ChunkPos.CODEC.fieldOf("minChunk").forGetter(AxisAlignedChunkArea::getMinChunkPos),
                    ChunkPos.CODEC.fieldOf("maxChunk").forGetter(AxisAlignedChunkArea::getMaxChunkPos),
                    Codec.INT.fieldOf("blockHeight").forGetter(AxisAlignedChunkArea::getHeight)
            ).apply(instance, AxisAlignedChunkArea::new
    ));

    /**
     * Generates am AACA of a single chunk that contains the selected block
     * @param chunkBlock
     */
    public AxisAlignedChunkArea(BlockPos chunkBlock) {
        this(chunkBlock,chunkBlock);
    }
    /**
     * Generates an AxisAlignedChunkArea with two opposite corners and 0 height
     * @param minCorner BlockPos of the closest chunk corner to world origin
     * @param maxCorner BlockPos of the furthest chunk corner from world origin
     */
    public AxisAlignedChunkArea(BlockPos minCorner, BlockPos maxCorner) {
        this(new ChunkPos(minCorner), new ChunkPos(maxCorner));
    }

    /**
     * Generates an AxisAlignedChunkArea with two opposite corners.
     * @param minCorner BlockPos of the closest chunk corner to world origin
     * @param maxCorner BlockPos of the furthest chunk corner from world origin
     * @param height height in blocks
     */
    public AxisAlignedChunkArea(BlockPos minCorner, BlockPos maxCorner, int height) {
        this(new ChunkPos(minCorner), new ChunkPos(maxCorner), height);
    }

    /**
     * Generates an AACA with no height
     * @param minChunk
     * @param maxChunk
     */
    public AxisAlignedChunkArea(ChunkPos minChunk, ChunkPos maxChunk) {
        this(minChunk,maxChunk,0);
    }
        /**
         * Builds a AACA from the two corners in a square shape
         * @param minChunk
         * @param maxChunk
         */
    public AxisAlignedChunkArea(ChunkPos minChunk, ChunkPos maxChunk, int height) {
        super(minChunk.getMinBlockX(), 0 , minChunk.getMinBlockZ(),
                maxChunk.getMaxBlockX(), height, maxChunk.getMaxBlockZ());

        // Get chunk coordinates
        int minChunkX = minChunk.x;
        int minChunkZ = minChunk.z;
        int maxChunkX = maxChunk.x;
        int maxChunkZ = maxChunk.z;

        // Compute center in chunk space
        int centerChunkX = (minChunkX + maxChunkX) / 2;
        int centerChunkZ = (minChunkZ + maxChunkZ) / 2;

        // Compute radius in chunk space
        int maxRadius = Math.max(Math.abs(maxChunkX - centerChunkX), Math.abs(maxChunkZ - centerChunkZ));

        this.substractRadius = this.generalRadius = maxRadius;
        this.taskShape = TaskShape.SQUARE;

    }


    /**
     * Constructs an AxisAlignedChunkArea with specified center and radii.
     *
     * @param center The center position of the chunk area.
     * @param maxRadius The maximum radius from the center IN CHUNKS.
     * @param minRadius The minimum radius from the center IN CHUNKS.
     */
    public AxisAlignedChunkArea(ChunkPos center, int maxRadius, int minRadius, TaskShape taskShape) {
        super(
                (center.getMiddleBlockX() - (Math.max(maxRadius, minRadius) << 4)), 0, (center.getMiddleBlockZ() - (Math.max(maxRadius, minRadius) << 4)),
                (center.getMiddleBlockX() + (Math.max(maxRadius, minRadius) << 4)), 0, (center.getMiddleBlockZ() + (Math.max(maxRadius, minRadius) << 4))
        );

        // Save the radii in chunk units (shift right by 4 for chunk sizes)
        this.generalRadius = Math.max(maxRadius, minRadius);
        this.substractRadius = Math.min(maxRadius, minRadius);


        this.taskShape = taskShape;
    }

    /**
     * Determines if the chunk area has a donut shape.
     *
     * @return True if the area is a donut shape (maxRadius != minRadius), otherwise false.
     */
    public Boolean isADonutedShape() {
        return taskShape.equals(TaskShape.DONUT) || TaskShape.EMPANADA.equals(taskShape);
    }

    /**
     * Checks if a given position is within the inscribed circle of the chunk area.
     *
     * @param pos The position to check.
     * @param radius The radius to compare against.
     * @return True if the position is within the inscribed circle, otherwise false.
     */
    public boolean isWithinInscribedCircle(ChunkPos pos, double radius) {
        // Get the center of the bounding area
        Vec3 center = super.getCenter();

        // Calculate the squared distance from the position to the center
        double dx = pos.x - center.x;
        double dz = pos.z - center.z;
        double distanceSquared = (dx * dx) + (dz * dz);

        // Compare with the squared radius
        return distanceSquared <= (radius * radius);
    }

    /**
     * Checks if a given position is within the inscribed circle of the chunk area using the maximum radius.
     *
     * @param pos The position to check.
     * @return True if the position is within the inscribed circle, otherwise false.
     */
    public boolean isWithinInscribedCircle(ChunkPos pos) {
        return isWithinInscribedCircle(pos, generalRadius);
    }

    /**
     * Checks if a given position is within the inscribed donut shape of the chunk area (square or circle shape).
     * If the shape is not a donut or empanada, it calls the general isWithinBounds instead.
     *
     * @param pos The position to check.
     * @return True if the position is within the inscribed donut, otherwise false.
     */
    public boolean isWithinDonutBounds(ChunkPos pos) {
        if (!isADonutedShape()) return isWithinBounds(pos);

        if (taskShape == TaskShape.DONUT) {
            boolean isWithinInnerCircle = isWithinInscribedCircle(pos, substractRadius);
            boolean isWithinOuterCircle = isWithinInscribedCircle(pos, generalRadius);
            return (!isWithinInnerCircle && isWithinOuterCircle);
        } else if (taskShape == TaskShape.EMPANADA) {
            // Create the inner bounding box
            AABB innerBounds = super.deflate(generalRadius - substractRadius);
            // Check if within the outer box but not the inner box
            return super.contains(pos.getMinBlockX(), 0, pos.getMinBlockZ()) &&
                    !innerBounds.contains(pos.getMinBlockX(), 0, pos.getMinBlockZ());
        }
        return false;
    }

    /**
     * Checks if the given position is within the bounding area.
     *
     * @param pos The position to check.
     * @return True if the position is within bounds, otherwise false.
     */
    public boolean isWithinBounds(ChunkPos pos) {
        return super.contains(pos.getMinBlockX(), 0, pos.getMinBlockZ());
    }

    /**
     * Method that returns the number of chunk columns of the area.
     * @return Number of chunk columns (width in chunks)
     */
    public int getNumberOfChunkColumns() {
        return (int) ((super.getXsize()+1) / 16.0); // Convert block width to chunk width
    }

    /**
     * Method that returns the number of chunk rows of the area.
     * @return Number of chunk rows (height in chunks)
     */
    public int getNumberOfChunkRows() {
        return (int) ((super.getZsize()+1) / 16.0); // Convert block height to chunk height
    }

    /**
     * A method that given the chunk and shape, returns an array of chunk positions as long to generate.
     * Each chunk position is represented as a long of xz coordinates
     *
     * @return An array of chunk positions as long.
     */
    public long[] getChunkList() {
        int minChunkX = getMinChunkX();
        int maxChunkX = getMaxChunkX();
        int minChunkZ = getMinChunkZ();
        int maxChunkZ = getMaxChunkZ();
        List<Long> chunkPositions = new ArrayList<>();

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                ChunkPos chunkPosition = new ChunkPos(x, z);

                if (taskShape.equals(TaskShape.SQUARE)) {
                    chunkPositions.add(chunkPosition.toLong());
                } else if (isADonutedShape() && isWithinDonutBounds(chunkPosition)) {
                    chunkPositions.add(chunkPosition.toLong());
                } else if (!isADonutedShape() && isWithinInscribedCircle(chunkPosition)) {
                    chunkPositions.add(chunkPosition.toLong());
                }
            }
        }
        // Convert the list to an array and return it
        return chunkPositions.stream().mapToLong(Long::longValue).toArray();
    }

    public List<ChunkPos> getChunkPosList() {
        int minChunkX = getMinChunkX();
        int maxChunkX = getMaxChunkX();
        int minChunkZ = getMinChunkZ();
        int maxChunkZ = getMaxChunkZ();
        List<ChunkPos> chunkPositions = new ArrayList<>();

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                ChunkPos chunkPosition = new ChunkPos(x, z);

                if (taskShape.equals(TaskShape.SQUARE)) {
                    chunkPositions.add(chunkPosition);
                } else if (isADonutedShape() && isWithinDonutBounds(chunkPosition)) {
                    chunkPositions.add(chunkPosition);
                } else if (!isADonutedShape() && isWithinInscribedCircle(chunkPosition)) {
                    chunkPositions.add(chunkPosition);
                }
            }
        }
        // Convert the list to an array and return it
        return chunkPositions;
    }

    public boolean intersects(AABB axisAlignedBB) {
        return this.intersects(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
    }

    /**
     * Intersects chunk? (no y check)=
     * @param p_82315_
     * @param p_82316_
     * @param p_82317_
     * @param p_82318_
     * @param p_82319_
     * @param p_82320_
     * @return
     */
    public boolean intersects(double p_82315_, double p_82316_, double p_82317_, double p_82318_, double p_82319_, double p_82320_) {
        //return this.minX < p_82318_ && this.maxX > p_82315_ && this.minY <= p_82319_ && this.maxY >= p_82316_ && this.minZ < p_82320_ && this.maxZ > p_82317_;
        return this.minX < p_82318_ && this.maxX > p_82315_ && this.minZ < p_82320_ && this.maxZ > p_82317_;
    }

    public ChunkPos getMinChunkPos() {
        return new ChunkPos((int) super.minX >> 4, (int) super.minZ >> 4);
    }

    public ChunkPos getMaxChunkPos() {
        return new ChunkPos((int) super.maxX >> 4, (int) super.maxZ >> 4);
    }

    public int getHeight() {
        return (int)super.maxY;
    }

    // Getter methods for bounding box coordinates, adjusted for chunks

    public int getMinX() {
        return (int) super.minX;
    }

    public int getMinChunkX() {
        return (int) super.minX>>4;
    }

    public int getMaxX() {
        return (int) super.maxX;
    }

    public int getMaxChunkX() {
        return (int) super.maxX>>4;
    }

    public int getMinZ() {
        return (int) super.minZ;
    }

    public int getMinChunkZ() {
        return (int) super.minZ>>4;
    }

    public int getMaxZ() {
        return (int) super.maxZ;
    }

    public int getMaxChunkZ() {
        return (int) super.maxZ>>4;
    }

    public int getCenterX() {
        return (int) super.getCenter().x;
    }

    public int getCenterChunkX() {
        return (int) super.getCenter().x>>4;
    }

    public int getCenterZ() {
        return (int) super.getCenter().z;
    }

    public int getCenterChunkZ() {
        return (int) super.getCenter().z>>4;
    }

    public TaskShape getShape() {
        return taskShape;
    }

    /**
     * return the equivalent boundginx box object
     * @return
     */
    public BoundingBox getBoundingBox() {
        return new BoundingBox(
                (int) Math.floor(super.minX), (int) Math.floor(super.minY), (int) Math.floor(super.minZ),
                (int) Math.floor(super.maxX), (int) Math.floor(super.maxY), (int) Math.floor(super.maxZ)
        );
    }

}
