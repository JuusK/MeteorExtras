package me.juusk.meteorextras.modules;

import me.juusk.meteorextras.MeteorExtras;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeathSpots extends Module {
    private static final Color GREEN = new Color(25, 225, 25);
    private static final Color ORANGE = new Color(225, 105, 25);
    private static final Color RED = new Color(225, 25, 25);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgFill = settings.createGroup("Fill");

    // General

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale.")
        .defaultValue(1)
        .min(0)
        .build()
    );

    private final Setting<Boolean> fullHeight = sgGeneral.add(new BoolSetting.Builder()
        .name("full-height")
        .description("Displays the height as the player's full height.")
        .defaultValue(true)
        .build()
    );

    // Render

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color.")
        .defaultValue(new SettingColor(255, 0, 0, 55))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color.")
        .defaultValue(new SettingColor(255, 0, 0))
        .build()
    );

    private final Setting<SettingColor> nameColor = sgRender.add(new ColorSetting.Builder()
        .name("name-color")
        .description("The name color.")
        .defaultValue(new SettingColor(255, 255, 255))
        .build()
    );

    private final Setting<SettingColor> nameBackgroundColor = sgRender.add(new ColorSetting.Builder()
        .name("name-background-color")
        .description("The name background color.")
        .defaultValue(new SettingColor(0, 0, 0, 75))
        .build()
    );

    private final Setting<Boolean> autoFill = sgFill.add(new BoolSetting.Builder()
        .name("auto-fill")
        .description("If the death spot should be automatically filled in with blocks.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<Block>> blocks = sgFill.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Blocks to fill the death spot in with.")
        .defaultValue(Blocks.OBSIDIAN)
        .visible(autoFill::get)
        .build()
    );


    private final Setting<Integer> delay = sgFill.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay in ticks between placing blocks.")
        .defaultValue(1)
        .min(1)
        .visible(autoFill::get)
        .build()
    );


    private final Setting<Integer> blocksPerTick = sgFill.add(new IntSetting.Builder()
        .name("blocks-per-tick")
        .description("How many blocks to place in one tick.")
        .defaultValue(1)
        .min(1)
        .visible(autoFill::get)
        .build()
    );

    private final Setting<Boolean> rotate = sgFill.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates towards the block when placing.")
        .defaultValue(true)
        .visible(autoFill::get)
        .build()
    );


    private final List<Entry> players = new ArrayList<>();

    private final List<PlayerListEntry> lastPlayerList = new ArrayList<>();
    private final List<PlayerEntity> lastPlayers = new ArrayList<>();

    private DimensionType lastDimension;

    public DeathSpots() {
        super(MeteorExtras.CATEGORY, "DeathSpots", "Displays a box where another player has died at.");
        lineColor.onChanged();
    }

    @Override
    public void onActivate() {
        lastPlayerList.addAll(mc.getNetworkHandler().getPlayerList());
        updateLastPlayers();
        lastDimension = mc.world.getDimension();
    }

    @Override
    public void onDeactivate() {
        players.clear();
        lastPlayerList.clear();
    }

    private void updateLastPlayers() {
        lastPlayers.clear();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity) lastPlayers.add((PlayerEntity) entity);
        }
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        if (event.entity instanceof PlayerEntity) {
            int toRemove = -1;

            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).uuid.equals(event.entity.getUuid())) {
                    toRemove = i;
                    break;
                }
            }

            if (toRemove != -1) {
                players.remove(toRemove);
            }
        }
    }

    int placeTimer = 0;
    @EventHandler
    private void onTick(TickEvent.Post event) {
        updateLastPlayers();

        for (PlayerEntity player : lastPlayers) {
            if (player.getHealth() <= 0 && !player.equals(mc.player)) {
                boolean alreadyTracked = players.stream()
                    .anyMatch(entry -> entry.uuid.equals(player.getUuid()));

                if (!alreadyTracked) {
                    add(new Entry(player));

                }
            }
        }

        if (autoFill.get() && !players.isEmpty()) {
            if (placeTimer <= 0) {
                int blocksPlaced = 0;

                for (Entry entry : new ArrayList<>(players)) {
                    if (blocksPlaced >= blocksPerTick.get()) break;

                    blocksPlaced += entry.fillBlocks(blocksPerTick.get() - blocksPlaced);

                    if (entry.isFullyFilled()) {
                        players.remove(entry);
                    }
                }

                placeTimer = delay.get();
            } else {
                placeTimer--;
            }
        }

        DimensionType dimension = mc.world.getDimension();
        if (dimension != lastDimension) players.clear();
        lastDimension = dimension;
    }

    private void add(Entry entry) {
        players.removeIf(player -> player.uuid.equals(entry.uuid));
        players.add(entry);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        for (Entry player : players) player.render3D(event);
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        for (Entry player : players) player.render2D(event.drawContext.getMatrices());
    }

    @Override
    public String getInfoString() {
        return Integer.toString(players.size());
    }

    private static final Vector3d pos = new Vector3d();

    private class Entry {
        public final double x, y, z;
        public final double xWidth, zWidth, halfWidth, height;

        public final UUID uuid;
        public final String name;


        private final List<BlockPos> blocksToFill;
        private int currentBlockIndex = 0;

        public Entry(PlayerEntity entity) {
            halfWidth = entity.getWidth() / 2;
            x = entity.getX() - halfWidth;
            y = entity.getY();
            z = entity.getZ() - halfWidth;

            xWidth = entity.getBoundingBox().getLengthX();
            zWidth = entity.getBoundingBox().getLengthZ();
            height = entity.getBoundingBox().getLengthY();

            uuid = entity.getUuid();
            name = entity.getName().getString();
            blocksToFill = new ArrayList<>();
            if (autoFill.get()) {
                int minX = (int) Math.floor(x);
                int minY = (int) Math.floor(y);
                int minZ = (int) Math.floor(z);
                int maxX = (int) Math.floor(x + xWidth);
                int maxY = (int) Math.floor(y + height);
                int maxZ = (int) Math.floor(z + zWidth);

                for (int bx = minX; bx <= maxX; bx++) {
                    for (int by = minY; by <= maxY; by++) {
                        for (int bz = minZ; bz <= maxZ; bz++) {
                            BlockPos bp = new BlockPos(bx, by, bz);
                            if (mc.world.getBlockState(bp).isReplaceable()) {
                                blocksToFill.add(bp);
                            }
                        }
                    }
                }
            }
        }

        public int fillBlocks(int maxBlocks) {
            if (!autoFill.get() || blocks.get().isEmpty()) return 0;

            int placed = 0;

            while (currentBlockIndex < blocksToFill.size() && placed < maxBlocks) {
                BlockPos bp = blocksToFill.get(currentBlockIndex);

                FindItemResult item = InvUtils.findInHotbar(itemStack ->
                    blocks.get().contains(net.minecraft.block.Block.getBlockFromItem(itemStack.getItem()))
                );

                if (!item.found()) {
                    currentBlockIndex++;
                    continue;
                }

                if (BlockUtils.place(bp, item, rotate.get(), 0)) {
                    placed++;
                }

                currentBlockIndex++;
            }

            return placed;
        }

        public boolean isFullyFilled() {
            return autoFill.get() && currentBlockIndex >= blocksToFill.size();
        }


        public void render3D(Render3DEvent event) {
            if (fullHeight.get()) event.renderer.box(x, y, z, x + xWidth, y + height, z + zWidth, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            else event.renderer.sideHorizontal(x, y, z, x + xWidth, z, sideColor.get(), lineColor.get(), shapeMode.get());
        }

        public void render2D(MatrixStack matrices) {
            if (!PlayerUtils.isWithinCamera(x, y, z, mc.options.getViewDistance().getValue() * 16)) return;

            TextRenderer text = TextRenderer.get();
            double scale = DeathSpots.this.scale.get();
            pos.set(x + halfWidth, y + height + 0.5, z + halfWidth);

            if (!NametagUtils.to2D(pos, scale)) return;

            NametagUtils.begin(pos);



            // Render background
            double i = text.getWidth(name) / 2.0;
            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(-i, 0, i * 2, text.getHeight(), nameBackgroundColor.get());
            Renderer2D.COLOR.render(matrices);

            // Render name and health texts
            text.beginBig();
            text.render(name, -i, 0, nameColor.get());
            text.end();

            NametagUtils.end();
        }
    }
}
