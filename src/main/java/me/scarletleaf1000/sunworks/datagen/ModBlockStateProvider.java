package me.scarletleaf1000.sunworks.datagen;


import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.block.ModBlocks;
import me.scarletleaf1000.sunworks.block.custom.cable.AbstractPipeBlock;
import me.scarletleaf1000.sunworks.block.custom.cable.CableTier;
import me.scarletleaf1000.sunworks.block.custom.cable.EnergyPipeBlock;
import me.scarletleaf1000.sunworks.block.custom.cable.PipeConnection;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Sunworks.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.HELIOLITE_BLOCK);
        blockWithItem(ModBlocks.BUDDING_HELIOLITE);

        blockWithItem(ModBlocks.CINDERITE_ORE);
        blockWithItem(ModBlocks.DEEPSLATE_CINDERITE_ORE);
        blockWithItem(ModBlocks.SILVER_ORE);
        blockWithItem(ModBlocks.DEEPSLATE_SILVER_ORE);
        blockWithItem(ModBlocks.CINDERITE_BLOCK);
        blockWithItem(ModBlocks.SILVER_BLOCK);
        blockWithItem(ModBlocks.RAW_CINDERITE_BLOCK);
        blockWithItem(ModBlocks.RAW_SILVER_BLOCK);
        blockWithItem(ModBlocks.ELECTRUM_BLOCK);

        horizontalFaceBlock(ModBlocks.SOLAR_ALLOY_SMELTER, true, true);
        horizontalFaceBlock(ModBlocks.ALLOY_SMELTER, true, false);

        simpleBlock(ModBlocks.SOLAR_PANEL.get(), models().getExistingFile(modLoc("block/solar_panel")));

        clusterBlock(ModBlocks.HELIOLITE_CLUSTER);
        clusterBlock(ModBlocks.LARGE_HELIOLITE_BUD);
        clusterBlock(ModBlocks.MEDIUM_HELIOLITE_BUD);
        clusterBlock(ModBlocks.SMALL_HELIOLITE_BUD);

        for (CableTier tier : CableTier.values()) {
            PipeModels models = pipeModels(tier);
            pipeBlockState(ModBlocks.ENERGY_PIPES.get(tier).get(), models);
        }

    }

    private record PipeVariant(ModelFile coreFace, ModelFile coreItem, ModelFile arm, ModelFile panel) {
    }

    private record PipeModels(PipeVariant unpowered, PipeVariant powered) {
    }

    /**
     * Builds the (shared, per-tier) pipe core/arm/panel models for both the unpowered and
     * powered (actively transferring energy) textures. Every direction reuses these same
     * three model files via rotation, and every tier reuses this same geometry via only
     * swapping out the single 16x16 texture - see the texture layout documented alongside
     * {@link AbstractPipeBlock}.
     */
    private PipeModels pipeModels(CableTier tier) {
        ResourceLocation texture = modLoc("block/" + tier.getTextureName());
        ResourceLocation poweredTexture = modLoc("block/" + tier.getTextureName() + "_powered");

        return new PipeModels(
                buildPipeVariant(pipeModelName(tier, "unpowered"), texture, "cutout"),
                buildPipeVariant(pipeModelName(tier, "powered"), poweredTexture, "translucent"));
    }

    private PipeVariant buildPipeVariant(String baseName, ResourceLocation texture, String renderType) {

        // Only ever a single face of the 6x6x6 core cube, built facing UP by default and
        // reused via rotation (like the arm/panel below). It is only shown per-direction when
        // that side has no pipe/extract connection - once an arm covers a side, the matching
        // core face is entirely hidden geometry, so we skip rendering it to avoid the arm's
        // transparent regions revealing the core bleeding through underneath.
        ModelFile coreFace = models().getBuilder(baseName + "_core_face")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType(renderType)
                .texture("texture", texture)
                .texture("particle", texture)
                .element()
                    .from(5, 5, 5).to(11, 11, 11)
                    .face(Direction.UP).uvs(10f, 0f, 16f, 6f).texture("#texture").end()
                .end();

        // Only used for the item icon, which needs the full cube rather than a single face.
        ModelFile coreItem = models().getBuilder(baseName + "_core_item")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType(renderType)
                .texture("texture", texture)
                .texture("particle", texture)
                .element()
                    .from(5, 5, 5).to(11, 11, 11)
                    .allFaces((direction, face) -> face.uvs(10f, 0f, 16f, 6f).texture("#texture"))
                .end();

        ModelFile arm = models().getBuilder(baseName + "_arm")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType(renderType)
                .texture("texture", texture)
                .texture("particle", texture)
                .element()
                    .from(5, 11, 5).to(11, 16, 11)
                    .face(Direction.NORTH).uvs(0f, 0f, 5f, 6f).rotation(ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90).texture("#texture").end()
                    .face(Direction.SOUTH).uvs(0f, 0f, 5f, 6f).rotation(ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90).texture("#texture").end()
                    .face(Direction.EAST).uvs(0f, 0f, 5f, 6f).rotation(ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90).texture("#texture").end()
                    .face(Direction.WEST).uvs(0f, 0f, 5f, 6f).rotation(ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90).texture("#texture").end()
                .end();

        ModelFile panel = models().getBuilder(baseName + "_panel")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .renderType(renderType)
                .texture("texture", texture)
                .texture("particle", texture)
                .element()
                    .from(4, 15, 4).to(12, 16, 12)
                    .face(Direction.UP).uvs(8f, 6f, 16f, 14f).texture("#texture").end()
                    .face(Direction.DOWN).uvs(8f, 6f, 9f, 14f).rotation(ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90).texture("#texture").end()
                    .face(Direction.NORTH).uvs(8f, 6f, 9f, 14f).rotation(ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90).texture("#texture").end()
                    .face(Direction.SOUTH).uvs(8f, 6f, 9f, 14f).rotation(ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90).texture("#texture").end()
                    .face(Direction.EAST).uvs(8f, 6f, 9f, 14f).rotation(ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90).texture("#texture").end()
                    .face(Direction.WEST).uvs(8f, 6f, 9f, 14f).rotation(ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90).texture("#texture").end()
                .end();

        return new PipeVariant(coreFace, coreItem, arm, panel);
    }

    private void pipeBlockState(Block block, PipeModels pipeModels) {
        PipeVariant unpowered = pipeModels.unpowered();
        PipeVariant powered = pipeModels.powered();

        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);

        for (Direction direction : Direction.values()) {
            int x = direction == Direction.DOWN ? 180 : direction.getAxis().isHorizontal() ? 90 : 0;
            int y = switch (direction) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };

            EnumProperty<PipeConnection> property = AbstractPipeBlock.PROPERTY_BY_DIRECTION.get(direction);

            builder.part().modelFile(unpowered.coreFace()).rotationX(x).rotationY(y).addModel()
                    .condition(property, PipeConnection.NONE)
                    .condition(EnergyPipeBlock.POWERED, false);
            builder.part().modelFile(powered.coreFace()).rotationX(x).rotationY(y).addModel()
                    .condition(property, PipeConnection.NONE)
                    .condition(EnergyPipeBlock.POWERED, true);

            builder.part().modelFile(unpowered.arm()).rotationX(x).rotationY(y).addModel()
                    .condition(property, PipeConnection.PIPE, PipeConnection.MACHINE)
                    .condition(EnergyPipeBlock.POWERED, false);
            builder.part().modelFile(powered.arm()).rotationX(x).rotationY(y).addModel()
                    .condition(property, PipeConnection.PIPE, PipeConnection.MACHINE)
                    .condition(EnergyPipeBlock.POWERED, true);

            builder.part().modelFile(unpowered.panel()).rotationX(x).rotationY(y).addModel()
                    .condition(property, PipeConnection.MACHINE)
                    .condition(EnergyPipeBlock.POWERED, false);
            builder.part().modelFile(powered.panel()).rotationX(x).rotationY(y).addModel()
                    .condition(property, PipeConnection.MACHINE)
                    .condition(EnergyPipeBlock.POWERED, true);
        }

        simpleBlockItem(block, unpowered.coreItem());
    }

    private String pipeModelName(CableTier tier, String part) {
        return "cable/" + tier.getName() + "_" + part;
    }

    private void clusterBlock(DeferredBlock<? extends Block> block) {
        ModelFile model = models().cross(block.getId().getPath(), blockTexture(block.get())).renderType("cutout");
        getVariantBuilder(block.get()).forAllStatesExcept(state -> {
            Direction facing = state.getValue(AmethystClusterBlock.FACING);
            int x = facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 90 : 0;
            int y = switch (facing) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y).build();
        }, AmethystClusterBlock.WATERLOGGED);
    }

    private void horizontalFaceBlock(DeferredBlock<Block> block, boolean hasOnOffTexture) {
        horizontalFaceBlock(block, hasOnOffTexture, false);
    }

    private void horizontalFaceBlock(DeferredBlock<Block> block, boolean hasOnOffTexture, boolean hasBottomTexture) {
        String name = block.getId().getPath();
        ResourceLocation side = modLoc("block/" + name + "_side");
        ResourceLocation front = modLoc("block/" + name + "_front");
        ResourceLocation top = modLoc("block/" + name + "_top");
        ResourceLocation bottom = hasBottomTexture ? modLoc("block/" + name + "_bottom") : top;

        ModelFile offModel = hasBottomTexture
                ? models().orientableWithBottom(name, side, front, bottom, top)
                : models().orientable(name, side, front, top);
        ModelFile onModel = !hasOnOffTexture
                ? offModel
                : hasBottomTexture
                        ? models().orientableWithBottom(name + "_on", side, modLoc("block/" + name + "_front_on"), bottom, top)
                        : models().orientable(name + "_on", side, modLoc("block/" + name + "_front_on"), top);

        getVariantBuilder(block.get()).forAllStates(state -> {
            ModelFile model = hasOnOffTexture && state.getValue(BlockStateProperties.LIT) ? onModel : offModel;
            int yRot = switch (state.getValue(HorizontalDirectionalBlock.FACING)) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(model).rotationY(yRot).build();
        });

        simpleBlockItem(block.get(), offModel);
    }

    private void blockWithItem(DeferredBlock<Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}
