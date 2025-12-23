package net.satisfy.earthernware.client;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.satisfy.earthernware.client.gui.KilnGui;
import net.satisfy.earthernware.client.gui.PotteryTableGui;
import net.satisfy.earthernware.client.renderer.AbstractStorageBlockEntityRenderer;
import net.satisfy.earthernware.client.renderer.StorageBlockEntityRenderer;
import net.satisfy.earthernware.client.renderer.FlowerPotRenderer;
import net.satisfy.earthernware.client.renderer.PlanterRenderer;
import net.satisfy.earthernware.client.renderer.TallFlowerPotRenderer;
import net.satisfy.earthernware.client.util.StoragePaintTintUtil;
import net.satisfy.earthernware.core.block.BudVaseBlock;
import net.satisfy.earthernware.core.block.CupellaBlock;
import net.satisfy.earthernware.core.block.GardenPotBlock;
import net.satisfy.earthernware.core.block.PlanterBlock;
import net.satisfy.earthernware.core.block.PlantBowlBlock;
import net.satisfy.earthernware.core.block.VaseBlock;
import net.satisfy.earthernware.core.registry.EntityTypeRegistry;
import net.satisfy.earthernware.core.registry.ScreenHandlerRegistry;
import net.satisfy.earthernware.core.registry.StorageTypeRegistry;

import static net.satisfy.earthernware.core.registry.ObjectRegistry.*;

public class EarthernwareClient {

    public static void onInitializeClient() {
        RenderTypeRegistry.register(
                RenderType.cutout(),
                POTTERY_TABLE.get(),
                BRICK_PLANTER.get(),
                BRICK_GARDEN_POT.get(),
                BRICK_SQUARE_POT.get(),
                BRICK_VASE.get(),
                BRICK_BUD_VASE.get(),
                BRICK_CUPELLA.get(),
                BRICK_PLANT_BOWL.get(),
                BRICK_AMPHORE.get(),
                RED_BRICK_PLANTER.get(),
                RED_BRICK_GARDEN_POT.get(),
                RED_BRICK_SQUARE_POT.get(),
                RED_BRICK_VASE.get(),
                RED_BRICK_BUD_VASE.get(),
                RED_BRICK_CUPELLA.get(),
                RED_BRICK_PLANT_BOWL.get(),
                RED_BRICK_AMPHORE.get(),
                YELLOW_BRICK_PLANTER.get(),
                YELLOW_BRICK_GARDEN_POT.get(),
                YELLOW_BRICK_SQUARE_POT.get(),
                YELLOW_BRICK_VASE.get(),
                YELLOW_BRICK_BUD_VASE.get(),
                YELLOW_BRICK_CUPELLA.get(),
                YELLOW_BRICK_PLANT_BOWL.get(),
                YELLOW_BRICK_AMPHORE.get(),
                WHITE_BRICK_PLANTER.get(),
                WHITE_BRICK_GARDEN_POT.get(),
                WHITE_BRICK_SQUARE_POT.get(),
                WHITE_BRICK_VASE.get(),
                WHITE_BRICK_BUD_VASE.get(),
                WHITE_BRICK_CUPELLA.get(),
                WHITE_BRICK_PLANT_BOWL.get(),
                WHITE_BRICK_AMPHORE.get(),
                BLUE_BRICK_PLANTER.get(),
                BLUE_BRICK_GARDEN_POT.get(),
                BLUE_BRICK_SQUARE_POT.get(),
                BLUE_BRICK_VASE.get(),
                BLUE_BRICK_BUD_VASE.get(),
                BLUE_BRICK_CUPELLA.get(),
                BLUE_BRICK_PLANT_BOWL.get(),
                BLUE_BRICK_AMPHORE.get(),
                DARK_BRICK_PLANTER.get(),
                DARK_BRICK_GARDEN_POT.get(),
                DARK_BRICK_SQUARE_POT.get(),
                DARK_BRICK_VASE.get(),
                DARK_BRICK_BUD_VASE.get(),
                DARK_BRICK_CUPELLA.get(),
                DARK_BRICK_PLANT_BOWL.get(),
                DARK_BRICK_AMPHORE.get()
        );

        ColorHandlerRegistry.registerBlockColors((state, world, pos, tintIndex) -> {
            if (world == null || pos == null) {
                return -1;
            }
            return BiomeColors.getAverageWaterColor(world, pos);
        }, POTTERY_TABLE.get());

        ColorHandlerRegistry.registerItemColors((stack, tintIndex) -> {
            if (tintIndex != 0) {
                return -1;
            }
            return 4159204;
        }, POTTERY_TABLE.get().asItem());

        registerClientScreens();
        registerStorageTypeRenderers();
        registerBlockTransforms();
        registerBlockEntityRenderer();

        StoragePaintTintUtil.register();
    }

    public static void preInitClient() {
        registerEntityRenderers();
        registerEntityModelLayer();
    }

    public static void registerEntityRenderers() {
    }

    public static void registerEntityModelLayer() {
    }

    public static void registerStorageTypeRenderers() {
        AbstractStorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.PLANTER, new PlanterRenderer());
        AbstractStorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.FLOWER_POT, new FlowerPotRenderer());
        AbstractStorageBlockEntityRenderer.registerStorageType(StorageTypeRegistry.TALL_FLOWER_POT, new TallFlowerPotRenderer());
    }

    public static void registerBlockTransforms() {
        AbstractStorageBlockEntityRenderer.registerClassTransform(PlanterBlock.class, new AbstractStorageBlockEntityRenderer.RenderTransform(0.225f, 0.25f, 0f, 1.0f, -90.0f));
        AbstractStorageBlockEntityRenderer.registerClassTransform(VaseBlock.class, new AbstractStorageBlockEntityRenderer.RenderTransform(-0.5f, 0.8f, -0.5f, 1.0f, 0.0f));
        AbstractStorageBlockEntityRenderer.registerClassTransform(BudVaseBlock.class, new AbstractStorageBlockEntityRenderer.RenderTransform(-0.5f, 0.6f, -0.5f, 1.0f, 0.0f));
        AbstractStorageBlockEntityRenderer.registerClassTransform(CupellaBlock.class, new AbstractStorageBlockEntityRenderer.RenderTransform(-0.5f, 0.3f, -0.5f, 1.0f, 0.0f));
        AbstractStorageBlockEntityRenderer.registerClassTransform(PlantBowlBlock.class, new AbstractStorageBlockEntityRenderer.RenderTransform(-0.5f, 0.4f, -0.5f, 1.0f, 0.0f));
        AbstractStorageBlockEntityRenderer.registerClassTransform(GardenPotBlock.class, new AbstractStorageBlockEntityRenderer.RenderTransform(-0.5f, 0.6f, -0.5f, 1.0f, 0.0f));
    }

    private static void registerClientScreens() {
        MenuRegistry.registerScreenFactory(ScreenHandlerRegistry.POTTERY_TABLE_SCREEN_HANDLER.get(), PotteryTableGui::new);
        MenuRegistry.registerScreenFactory(ScreenHandlerRegistry.KILN_SCREEN_HANDLER.get(), KilnGui::new);
    }

    public static void registerBlockEntityRenderer() {
        BlockEntityRendererRegistry.register(EntityTypeRegistry.STORAGE_BLOCK_ENTITY.get(), context -> new StorageBlockEntityRenderer());
    }
}