package es.nullbyte.megastructureblock.client.guis.blocks.megastructureblock;
import com.google.common.collect.ImmutableList;
import es.nullbyte.megastructureblock.blocks.blockentities.MegaStructureBlockEntity;
import es.nullbyte.megastructureblock.blocks.blockentities.megastructure.MegaStructureMode;
import es.nullbyte.megastructureblock.blocks.megastructures.MegaStructureBlock;
import es.nullbyte.megastructureblock.networking.packets.payloads.MegaStructureDataPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MegaStructureBlockEditScreen extends Screen {
    private static final Component NAME_LABEL = Component.translatable("structure_block.structure_name");
    private static final Component POSITION_LABEL = Component.translatable("structure_block.position");
    private static final Component SIZE_LABEL = Component.translatable("structure_block.size");
    private static final Component INTEGRITY_LABEL = Component.translatable("structure_block.integrity");
    private static final Component CUSTOM_DATA_LABEL = Component.translatable("structure_block.custom_data");
    private static final Component INCLUDE_ENTITIES_LABEL = Component.translatable("structure_block.include_entities");
    private static final Component DETECT_SIZE_LABEL = Component.translatable("structure_block.detect_size");
    private static final Component SHOW_AIR_LABEL = Component.translatable("structure_block.show_air");
    private static final Component SHOW_BOUNDING_BOX_LABEL = Component.translatable("structure_block.show_boundingbox");
    private static final ImmutableList<MegaStructureMode> ALL_MODES = ImmutableList.copyOf(MegaStructureMode.values());
    private static final ImmutableList<MegaStructureMode> DEFAULT_MODES = ALL_MODES.stream()
            .filter(p_169859_ -> p_169859_ != MegaStructureMode.DATA)
            .collect(ImmutableList.toImmutableList());
    private final MegaStructureBlockEntity structure;
    private Mirror initialMirror = Mirror.NONE;
    private Rotation initialRotation = Rotation.NONE;
    private MegaStructureMode initialMode = MegaStructureMode.DATA;
    private boolean initialEntityIgnoring;
    private boolean initialShowAir;
    private boolean initialShowBoundingBox;
    private EditBox nameEdit;
    private EditBox posXEdit;
    private EditBox posYEdit;
    private EditBox posZEdit;
    private EditBox sizeXEdit;
    private EditBox sizeYEdit;
    private EditBox sizeZEdit;
    private EditBox integrityEdit;
    private EditBox seedEdit;
    private EditBox dataEdit;
    private Button saveButton;
    private Button loadButton;
    private Button rot0Button;
    private Button rot90Button;
    private Button rot180Button;
    private Button rot270Button;
    private Button detectButton;
    private CycleButton<Boolean> includeEntitiesButton;
    private CycleButton<Mirror> mirrorButton;
    private CycleButton<Boolean> toggleAirButton;
    private CycleButton<Boolean> toggleBoundingBox;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");

    public MegaStructureBlockEditScreen(MegaStructureBlockEntity structure) {
        super(Component.translatable(Blocks.STRUCTURE_BLOCK.getDescriptionId()));
        this.structure = structure;
        this.decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    private void onDone() {
        if (this.sendToServer(MegaStructureBlockEntity.UpdateType.UPD8_AREA)) {
            this.minecraft.setScreen(null);
        }
    }

    private void onCancel() {
        this.structure.setMirror(this.initialMirror);
        this.structure.setRotation(this.initialRotation);
        this.structure.setMode(this.initialMode);
        this.structure.setIgnoreEntities(this.initialEntityIgnoring);
        this.structure.setShowAir(this.initialShowAir);
        this.structure.setShowBoundingBox(this.initialShowBoundingBox);
        this.minecraft.setScreen(null);
    }

    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, p_99460_ -> this.onDone()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, p_99457_ -> this.onCancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
        this.initialMirror = this.structure.getMirror();
        this.initialRotation = this.structure.getRotation();
        this.initialMode = this.structure.getMode();
        this.initialEntityIgnoring = this.structure.isIgnoreEntities();
        this.initialShowAir = this.structure.getShowAir();
        this.initialShowBoundingBox = this.structure.getShowBoundingBox();
        this.saveButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.save"), p_280866_ -> {
            if (this.structure.getMode() == MegaStructureMode.SAVE) {
                this.sendToServer(MegaStructureBlockEntity.UpdateType.SAVE_AREA);
                this.minecraft.setScreen(null);
            }
        }).bounds(this.width / 2 + 4 + 100, 185, 50, 20).build());
        this.loadButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.load"), p_280864_ -> {
            if (this.structure.getMode() == MegaStructureMode.LOAD) {
                this.sendToServer(MegaStructureBlockEntity.UpdateType.LOAD_AREA);
                this.minecraft.setScreen(null);
            }
        }).bounds(this.width / 2 + 4 + 100, 185, 50, 20).build());
        this.addRenderableWidget(
                CycleButton.<MegaStructureMode>builder(megaStructureMode -> Component.translatable("block.megastructureblock.label.swap mode"),this.initialMode)
                        .withValues(DEFAULT_MODES, ALL_MODES)
                        .displayOnlyValue()
                        .create(this.width / 2 - 4 - 150, 185, 50, 20, Component.literal("MODE"), (p_169846_, p_169847_) -> {
                            this.setBlockMode(p_169847_);
                        })
        );
        this.detectButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.detect_size"), p_280865_ -> {
            if (this.structure.getMode() == MegaStructureMode.SAVE) {
                this.sendToServer(MegaStructureBlockEntity.UpdateType.SCAN_AREA);
                this.minecraft.setScreen(null);
            }
        }).bounds(this.width / 2 + 4 + 100, 120, 50, 20).build());
        this.includeEntitiesButton = this.addRenderableWidget(
                CycleButton.onOffBuilder(!this.structure.isIgnoreEntities())
                        .displayOnlyValue()
                        .create(this.width / 2 + 4 + 100, 160, 50, 20, INCLUDE_ENTITIES_LABEL, (p_169861_, p_169862_) -> this.structure.setIgnoreEntities(!p_169862_))
        );
        this.mirrorButton = this.addRenderableWidget(
                CycleButton.builder(Mirror::symbol,this.initialMirror)
                        .withValues(Mirror.values())
                        .displayOnlyValue()
                        .create(this.width / 2 - 20, 185, 40, 20, Component.literal("MIRROR"), (p_169843_, p_169844_) -> this.structure.setMirror(p_169844_))
        );
        this.toggleAirButton = this.addRenderableWidget(
                CycleButton.onOffBuilder(this.structure.getShowAir())
                        .displayOnlyValue()
                        .create(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_AIR_LABEL, (p_169856_, p_169857_) -> this.structure.setShowAir(p_169857_))
        );
        this.toggleBoundingBox = this.addRenderableWidget(
                CycleButton.onOffBuilder(this.structure.getShowBoundingBox())
                        .displayOnlyValue()
                        .create(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_BOUNDING_BOX_LABEL, (p_169849_, p_169850_) -> this.structure.setShowBoundingBox(p_169850_))
        );
        this.rot0Button = this.addRenderableWidget(Button.builder(Component.literal("0"), p_99425_ -> {
            this.structure.setRotation(Rotation.NONE);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 - 1 - 40 - 1 - 40 - 20, 185, 40, 20).build());
        this.rot90Button = this.addRenderableWidget(Button.builder(Component.literal("90"), p_99415_ -> {
            this.structure.setRotation(Rotation.CLOCKWISE_90);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 - 1 - 40 - 20, 185, 40, 20).build());
        this.rot180Button = this.addRenderableWidget(Button.builder(Component.literal("180"), p_169854_ -> {
            this.structure.setRotation(Rotation.CLOCKWISE_180);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 + 1 + 20, 185, 40, 20).build());
        this.rot270Button = this.addRenderableWidget(Button.builder(Component.literal("270"), p_169841_ -> {
            this.structure.setRotation(Rotation.COUNTERCLOCKWISE_90);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 + 1 + 40 + 1 + 20, 185, 40, 20).build());
        this.nameEdit = new EditBox(this.font, this.width / 2 - 152, 40, 300, 20, Component.translatable("structure_block.structure_name")) {
            @Override
            public boolean charTyped(@NotNull CharacterEvent p_445809_) {
                return MegaStructureBlockEditScreen.this.isValidCharacterForName(this.getValue(), p_445809_.codepoint(), this.getCursorPosition()) && super.charTyped(p_445809_);
            }
        };
        this.nameEdit.setMaxLength(128);
        this.nameEdit.setValue(this.structure.getStructureName());
        this.addWidget(this.nameEdit);
        BlockPos blockpos = this.structure.getStructurePos();
        this.posXEdit = new EditBox(this.font, this.width / 2 - 152, 80, 80, 20, Component.translatable("structure_block.position.x"));
        this.posXEdit.setMaxLength(15);
        this.posXEdit.setValue(Integer.toString(blockpos.getX()));
        this.addWidget(this.posXEdit);
        this.posYEdit = new EditBox(this.font, this.width / 2 - 72, 80, 80, 20, Component.translatable("structure_block.position.y"));
        this.posYEdit.setMaxLength(15);
        this.posYEdit.setValue(Integer.toString(blockpos.getY()));
        this.addWidget(this.posYEdit);
        this.posZEdit = new EditBox(this.font, this.width / 2 + 8, 80, 80, 20, Component.translatable("structure_block.position.z"));
        this.posZEdit.setMaxLength(15);
        this.posZEdit.setValue(Integer.toString(blockpos.getZ()));
        this.addWidget(this.posZEdit);
        Vec3i vec3i = this.structure.getStructureSize();
        this.sizeXEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, Component.translatable("structure_block.size.x"));
        this.sizeXEdit.setMaxLength(15);
        this.sizeXEdit.setValue(Integer.toString(vec3i.getX()));
        this.addWidget(this.sizeXEdit);
        this.sizeYEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, Component.translatable("structure_block.size.y"));
        this.sizeYEdit.setMaxLength(15);
        this.sizeYEdit.setValue(Integer.toString(vec3i.getY()));
        this.addWidget(this.sizeYEdit);
        this.sizeZEdit = new EditBox(this.font, this.width / 2 + 8, 120, 80, 20, Component.translatable("structure_block.size.z"));
        this.sizeZEdit.setMaxLength(15);
        this.sizeZEdit.setValue(Integer.toString(vec3i.getZ()));
        this.addWidget(this.sizeZEdit);
        this.integrityEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, Component.translatable("structure_block.integrity.integrity"));
        this.integrityEdit.setMaxLength(15);
        this.integrityEdit.setValue(this.decimalFormat.format(this.structure.getIntegrity()));
        this.addWidget(this.integrityEdit);
        this.seedEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, Component.translatable("structure_block.integrity.seed"));
        this.seedEdit.setMaxLength(31);
        this.seedEdit.setValue(Long.toString(this.structure.getSeed()));
        this.addWidget(this.seedEdit);
        this.dataEdit = new EditBox(this.font, this.width / 2 - 152, 120, 240, 20, Component.translatable("structure_block.custom_data"));
        this.dataEdit.setMaxLength(128);
        this.dataEdit.setValue(this.structure.getMetaData());
        this.addWidget(this.dataEdit);
        this.updateDirectionButtons();
        this.updateMode(this.structure.getMode());
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public void renderBackground(GuiGraphics p_333749_, int p_333882_, int p_333946_, float p_334094_) {
        this.renderTransparentBackground(p_333749_);
    }

    private void setBlockMode(MegaStructureMode mode) {
        if (this.minecraft.level != null) {
            BlockPos pos = this.structure.getBlockPos();
            BlockState state = this.minecraft.level.getBlockState(pos);

            if (state.getBlock() instanceof MegaStructureBlock) {
                // Update blockstate in world, with client notification
                this.minecraft.level.setBlock(pos, state.setValue(MegaStructureBlock.MODE, mode), 3);

                // Also update block entity so UI stays in sync
                this.structure.setMode(mode);
            }
        }
        this.updateMode(mode);

    }
    @Override
    public void resize(int width, int height) {
        String s = this.nameEdit.getValue();
        String s1 = this.posXEdit.getValue();
        String s2 = this.posYEdit.getValue();
        String s3 = this.posZEdit.getValue();
        String s4 = this.sizeXEdit.getValue();
        String s5 = this.sizeYEdit.getValue();
        String s6 = this.sizeZEdit.getValue();
        String s7 = this.integrityEdit.getValue();
        String s8 = this.seedEdit.getValue();
        String s9 = this.dataEdit.getValue();
        this.init(width, height);
        this.nameEdit.setValue(s);
        this.posXEdit.setValue(s1);
        this.posYEdit.setValue(s2);
        this.posZEdit.setValue(s3);
        this.sizeXEdit.setValue(s4);
        this.sizeYEdit.setValue(s5);
        this.sizeZEdit.setValue(s6);
        this.integrityEdit.setValue(s7);
        this.seedEdit.setValue(s8);
        this.dataEdit.setValue(s9);
    }

    private void updateDirectionButtons() {
        this.rot0Button.active = true;
        this.rot90Button.active = true;
        this.rot180Button.active = true;
        this.rot270Button.active = true;
        switch (this.structure.getRotation()) {
            case NONE:
                this.rot0Button.active = false;
                break;
            case CLOCKWISE_180:
                this.rot180Button.active = false;
                break;
            case COUNTERCLOCKWISE_90:
                this.rot270Button.active = false;
                break;
            case CLOCKWISE_90:
                this.rot90Button.active = false;
        }
    }

    private void updateMode(MegaStructureMode structureMode) {
        this.nameEdit.setVisible(false);
        this.posXEdit.setVisible(false);
        this.posYEdit.setVisible(false);
        this.posZEdit.setVisible(false);
        this.sizeXEdit.setVisible(false);
        this.sizeYEdit.setVisible(false);
        this.sizeZEdit.setVisible(false);
        this.integrityEdit.setVisible(false);
        this.seedEdit.setVisible(false);
        this.dataEdit.setVisible(false);
        this.saveButton.visible = false;
        this.loadButton.visible = false;
        this.detectButton.visible = false;
        this.includeEntitiesButton.visible = false;
        this.mirrorButton.visible = false;
        this.rot0Button.visible = false;
        this.rot90Button.visible = false;
        this.rot180Button.visible = false;
        this.rot270Button.visible = false;
        this.toggleAirButton.visible = false;
        this.toggleBoundingBox.visible = false;
        switch (structureMode) {
            case SAVE:
                this.nameEdit.setVisible(true);
                this.posXEdit.setVisible(true);
                this.posYEdit.setVisible(true);
                this.posZEdit.setVisible(true);
                this.sizeXEdit.setVisible(true);
                this.sizeYEdit.setVisible(true);
                this.sizeZEdit.setVisible(true);
                this.saveButton.visible = true;
                this.detectButton.visible = true;
                this.includeEntitiesButton.visible = true;
                this.toggleAirButton.visible = true;
                break;
            case LOAD:
                this.nameEdit.setVisible(true);
                this.posXEdit.setVisible(true);
                this.posYEdit.setVisible(true);
                this.posZEdit.setVisible(true);
                this.integrityEdit.setVisible(true);
                this.seedEdit.setVisible(true);
                this.loadButton.visible = true;
                this.includeEntitiesButton.visible = true;
                this.mirrorButton.visible = true;
                this.rot0Button.visible = true;
                this.rot90Button.visible = true;
                this.rot180Button.visible = true;
                this.rot270Button.visible = true;
                this.toggleBoundingBox.visible = true;
                this.updateDirectionButtons();
                break;
            case CRNR:
                this.nameEdit.setVisible(true);
                break;
            case DATA:
                this.dataEdit.setVisible(true);
        }
    }

    private boolean sendToServer(MegaStructureBlockEntity.UpdateType updateType) {
        BlockPos blockpos = new BlockPos(
                this.parseCoordinate(this.posXEdit.getValue()), this.parseCoordinate(this.posYEdit.getValue()), this.parseCoordinate(this.posZEdit.getValue())
        );
        Vec3i vec3i = new Vec3i(
                this.parseCoordinate(this.sizeXEdit.getValue()), this.parseCoordinate(this.sizeYEdit.getValue()), this.parseCoordinate(this.sizeZEdit.getValue())
        );
        float f = this.parseIntegrity(this.integrityEdit.getValue());
        long i = this.parseSeed(this.seedEdit.getValue());

        ClientPacketDistributor.sendToServer(new MegaStructureDataPayload(
                this.structure.getBlockPos(),
                updateType,
                this.structure.getMode(),
                this.nameEdit.getValue(),
                blockpos,
                vec3i,
                this.structure.getMirror(),
                this.structure.getRotation(),
                this.dataEdit.getValue(),
                this.structure.isIgnoreEntities(),
                this.structure.getShowAir(),
                this.structure.getShowBoundingBox(),
                f,
                i));

        return true;
    }

    private long parseSeed(String seed) {
        try {
            return Long.valueOf(seed);
        } catch (NumberFormatException numberformatexception) {
            return 0L;
        }
    }

    private float parseIntegrity(String integrity) {
        try {
            return Float.valueOf(integrity);
        } catch (NumberFormatException numberformatexception) {
            return 1.0F;
        }
    }

    private int parseCoordinate(String coordinate) {
        try {
            return Integer.parseInt(coordinate);
        } catch (NumberFormatException numberformatexception) {
            return 0;
        }
    }

    @Override
    public void onClose() {
        this.onCancel();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        } else if (event.isConfirmation()) {
            this.onDone();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void render(GuiGraphics p_281951_, int p_99407_, int p_99408_, float p_99409_) {
        super.render(p_281951_, p_99407_, p_99408_, p_99409_);
        MegaStructureMode structuremode = this.structure.getMode();
        p_281951_.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFA0A0A0);
        if (structuremode != MegaStructureMode.DATA) {
            p_281951_.drawString(this.font, NAME_LABEL, this.width / 2 - 153, 30, 0xFFA0A0A0);
            this.nameEdit.render(p_281951_, p_99407_, p_99408_, p_99409_);
        }

        if (structuremode == MegaStructureMode.LOAD || structuremode == MegaStructureMode.SAVE) {
            p_281951_.drawString(this.font, POSITION_LABEL, this.width / 2 - 153, 70, 0xFFA0A0A0);
            this.posXEdit.render(p_281951_, p_99407_, p_99408_, p_99409_);
            this.posYEdit.render(p_281951_, p_99407_, p_99408_, p_99409_);
            this.posZEdit.render(p_281951_, p_99407_, p_99408_, p_99409_);
            p_281951_.drawString(this.font, INCLUDE_ENTITIES_LABEL, this.width / 2 + 154 - this.font.width(INCLUDE_ENTITIES_LABEL), 150, 0xFFA0A0A0);
        }

        if (structuremode == MegaStructureMode.SAVE) {
            p_281951_.drawString(this.font, SIZE_LABEL, this.width / 2 - 153, 110, 0xFFA0A0A0);
            this.sizeXEdit.render(p_281951_, p_99407_, p_99408_, p_99409_);
            this.sizeYEdit.render(p_281951_, p_99407_, p_99408_, p_99409_);
            this.sizeZEdit.render(p_281951_, p_99407_, p_99408_, p_99409_);
            p_281951_.drawString(this.font, DETECT_SIZE_LABEL, this.width / 2 + 154 - this.font.width(DETECT_SIZE_LABEL), 110, 0xFFA0A0A0);
            p_281951_.drawString(this.font, SHOW_AIR_LABEL, this.width / 2 + 154 - this.font.width(SHOW_AIR_LABEL), 70, 0xFFA0A0A0);
        }

        if (structuremode == MegaStructureMode.LOAD) {
            p_281951_.drawString(this.font, INTEGRITY_LABEL, this.width / 2 - 153, 110, 0xFFA0A0A0);
            this.integrityEdit.render(p_281951_, p_99407_, p_99408_, p_99409_);
            this.seedEdit.render(p_281951_, p_99407_, p_99408_, p_99409_);
            p_281951_.drawString(this.font, SHOW_BOUNDING_BOX_LABEL, this.width / 2 + 154 - this.font.width(SHOW_BOUNDING_BOX_LABEL), 70, 0xFFA0A0A0);
        }

        if (structuremode == MegaStructureMode.DATA) {
            p_281951_.drawString(this.font, CUSTOM_DATA_LABEL, this.width / 2 - 153, 110, 0xFFA0A0A0);
            this.dataEdit.render(p_281951_, p_99407_, p_99408_, p_99409_);
        }

        p_281951_.drawString(this.font, structuremode.getDisplayName(), this.width / 2 - 153, 174, 0xFFA0A0A0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
