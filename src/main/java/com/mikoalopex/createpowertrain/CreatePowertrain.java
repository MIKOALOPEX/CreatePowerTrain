package com.mikoalopex.createpowertrain;
import com.mikoalopex.createpowertrain.content.blocks.rope_connector_master.RopeMasterBlock;
import com.mikoalopex.createpowertrain.content.blocks.rope_connector_master.RopeMasterCompatibility;
import com.mikoalopex.createpowertrain.content.blocks.rope_connector_master.RopeMasterBlockEntity;
import com.mikoalopex.createpowertrain.content.blocks.rope_connector_master.RopeMasterMenu;
import com.mikoalopex.createpowertrain.content.blocks.rope_connector_master.RopeMasterScreen;
import com.mikoalopex.createpowertrain.content.blocks.copycat_door.CopycatDoors;
import com.mikoalopex.createpowertrain.content.blocks.copycat_door.CopycatDoorClient;
import java.util.EnumMap;
import java.util.List;
import com.mikoalopex.createpowertrain.content.kinetics.coupling.BallCouplings;
import com.mikoalopex.createpowertrain.content.fluids.coupling.FluidCouplings;
import com.mikoalopex.createpowertrain.content.blocks.physical_hose.PhysicalHoses;
import com.mikoalopex.createpowertrain.content.blocks.physical_hose.PhysicalHoseRenderer;
import com.mikoalopex.createpowertrain.content.fluids.coupling.FluidCouplingRenderer;
import com.mikoalopex.createpowertrain.content.fluids.coupling.FluidCouplingClient;
import com.mikoalopex.createpowertrain.content.kinetics.coupling.BallCouplingScreen;
import com.mikoalopex.createpowertrain.content.kinetics.coupling.BallCouplingRenderer;
import com.mikoalopex.createpowertrain.content.kinetics.coupling.StressCouplingCompatibility;
import com.mikoalopex.createpowertrain.content.blocks.fire_hose.HoseBracketBlock;
import com.mikoalopex.createpowertrain.content.blocks.fire_hose.HoseBracketBlockEntity;
import com.mikoalopex.createpowertrain.content.blocks.fire_hose.HoseBracketRenderer;
import com.mikoalopex.createpowertrain.content.blocks.smart_rope_connector.SmartRopeCompatibility;
import com.mikoalopex.createpowertrain.content.blocks.rope_latch.RopeLatchCompatibility;
import com.mikoalopex.createpowertrain.content.items.configurator.MultifunctionConfiguratorItem;
import com.mikoalopex.createpowertrain.content.items.configurator.MultifunctionConfiguratorMenu;
import com.mikoalopex.createpowertrain.content.items.configurator.MultifunctionConfiguratorScreen;
import com.mikoalopex.createpowertrain.integration.sable.SableStructureCompat;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.equipment.armor.BacktankItem;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.item.TooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.mojang.logging.LogUtils;
import net.createmod.catnip.lang.FontHelper.Palette;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import com.mikoalopex.createpowertrain.content.kinetics.rocker.Rockers;


@Mod(CreatePowertrain.MODID)
public class CreatePowertrain {
 public static final String MODID="createpowertrain";
 public static final Logger LOGGER=LogUtils.getLogger();
 public static ResourceLocation path(String path) { return ResourceLocation.fromNamespaceAndPath(MODID,path); }
public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);
public static final DeferredRegister.DataComponents DATA_COMPONENTS =
		DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);
public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SMART_ROPE_SELECTION =
		DATA_COMPONENTS.registerComponentType("smart_rope_selection",
			builder -> builder.persistent(com.mojang.serialization.Codec.BOOL)
				.networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL));
public static final DeferredBlock<HoseBracketBlock> HOSE_BRACKET = BLOCKS.register("hose_bracket",
		() -> SableStructureCompat.createHoseBracketBlock(BlockBehaviour.Properties.of().strength(1.5f).noOcclusion().dynamicShape()));
public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HoseBracketBlockEntity>> HOSE_BRACKET_BE =
		BLOCK_ENTITY_TYPES.register("hose_bracket",
			() -> BlockEntityType.Builder.of(SableStructureCompat::createHoseBracketBlockEntity, HOSE_BRACKET.get()).build(null));
public static final DeferredBlock<Block> SMART_ROPE_CONNECTOR = BLOCKS.register("smart_rope_connector",
		() -> SmartRopeCompatibility.createBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
			.strength(3.0f).sound(SoundType.METAL).noOcclusion()));
public static final DeferredItem<BlockItem> SMART_ROPE_CONNECTOR_ITEM =
		ITEMS.register("smart_rope_connector", () -> new com.mikoalopex.createpowertrain.compatibility.CompatibleBlockItem(SMART_ROPE_CONNECTOR.get(), new Item.Properties()));
public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<net.minecraft.world.level.block.entity.BlockEntity>> SMART_ROPE_CONNECTOR_BE =
		BLOCK_ENTITY_TYPES.register("smart_rope_connector",
			() -> BlockEntityType.Builder.of(SmartRopeCompatibility::createEntity, SMART_ROPE_CONNECTOR.get()).build(null));
public static final DeferredBlock<RopeMasterBlock> ROPE_MASTER = BLOCKS.register("rope_connector_master",
        () -> RopeMasterCompatibility.createBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
            .strength(3.0f).sound(SoundType.METAL).noOcclusion()));
public static final DeferredItem<BlockItem> ROPE_MASTER_ITEM =
        ITEMS.register("rope_connector_master", () -> new com.mikoalopex.createpowertrain.compatibility.CompatibleBlockItem(ROPE_MASTER.get(), new Item.Properties()));
public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RopeMasterBlockEntity>> ROPE_MASTER_BE =
        BLOCK_ENTITY_TYPES.register("rope_connector_master",
            () -> BlockEntityType.Builder.of(RopeMasterBlockEntity::new, ROPE_MASTER.get()).build(null));
public static final DeferredHolder<MenuType<?>, MenuType<RopeMasterMenu>> ROPE_MASTER_MENU =
        MENU_TYPES.register("rope_connector_master", () -> IMenuTypeExtension.create(RopeMasterMenu::new));
public static final DeferredBlock<Block> ROPE_HOOK = BLOCKS.register("rope_hook",
		() -> RopeLatchCompatibility.createBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
			.strength(3.0f).sound(SoundType.METAL).noOcclusion()));
public static final DeferredItem<BlockItem> ROPE_HOOK_ITEM = ITEMS.register("rope_hook", () -> new com.mikoalopex.createpowertrain.compatibility.CompatibleBlockItem(ROPE_HOOK.get(), new Item.Properties()));
public static final DeferredBlock<Block> ROPE_LATCH = BLOCKS.register("rope_latch",
		() -> RopeLatchCompatibility.createBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
			.strength(3.0f).sound(SoundType.METAL).noOcclusion()));
public static final DeferredItem<BlockItem> ROPE_LATCH_ITEM = ITEMS.register("rope_latch", () -> new com.mikoalopex.createpowertrain.compatibility.CompatibleBlockItem(ROPE_LATCH.get(), new Item.Properties()));
public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<net.minecraft.world.level.block.entity.BlockEntity>> ROPE_LATCH_BE =
		BLOCK_ENTITY_TYPES.register("rope_latch",
			() -> BlockEntityType.Builder.of(RopeLatchCompatibility::createEntity, ROPE_LATCH.get(), ROPE_HOOK.get()).build(null));
public static final DeferredItem<MultifunctionConfiguratorItem> MULTIFUNCTION_CONFIGURATOR_ITEM =
		ITEMS.register("multifunction_configurator", () -> new MultifunctionConfiguratorItem(new Item.Properties().stacksTo(1)));
public static final DeferredHolder<MenuType<?>, MenuType<MultifunctionConfiguratorMenu>> MULTIFUNCTION_CONFIGURATOR_MENU =
		MENU_TYPES.register("multifunction_configurator",
			() -> IMenuTypeExtension.create(MultifunctionConfiguratorMenu::new));
 public static final DeferredHolder<CreativeModeTab,CreativeModeTab> CREATIVE_TAB=CREATIVE_MODE_TABS.register("tab",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.createpowertrain")).icon(()->Rockers.ITEM.get().getDefaultInstance()).displayItems((parameters,output)->{
 output.accept(Rockers.ITEM.get()); output.accept(CopycatDoors.TRAPDOOR_ITEM.get()); output.accept(CopycatDoors.SLIDING_ITEM.get()); output.accept(CopycatDoors.FOLDING_ITEM.get());
 output.accept(PhysicalHoses.ITEM.get());
 output.accept(PhysicalHoses.COPYCAT_ITEM.get());
 output.accept(BallCouplings.ITEM.get());
 output.accept(FluidCouplings.ITEM.get());
 output.accept(SMART_ROPE_CONNECTOR_ITEM.get());
 output.accept(ROPE_HOOK_ITEM.get());
 output.accept(ROPE_MASTER_ITEM.get());
 output.accept(MULTIFUNCTION_CONFIGURATOR_ITEM.get());
 }).build());
 public CreatePowertrain(IEventBus bus,ModContainer container){
 com.mikoalopex.createpowertrain.compatibility.RequiredPhysics.initialize();
 BallCouplings.init(); FluidCouplings.init(); PhysicalHoses.init(); Rockers.init(); CopycatDoors.init();
 BLOCKS.register(bus); ITEMS.register(bus); BLOCK_ENTITY_TYPES.register(bus); MENU_TYPES.register(bus); CREATIVE_MODE_TABS.register(bus); DATA_COMPONENTS.register(bus);
 bus.addListener(this::registerCapabilities);
 bus.addListener(this::commonSetup);
 container.registerConfig(ModConfig.Type.SERVER,Config.SPEC); container.registerConfig(ModConfig.Type.CLIENT,ClientConfig.SPEC);
 }
 private void commonSetup(FMLCommonSetupEvent event){event.enqueueWork(()->{
 CopycatDoors.registerBehaviours();
 com.mikoalopex.createpowertrain.integration.sableschematic.SableSchematicCompat.register();
 });}
 private void registerCapabilities(RegisterCapabilitiesEvent event){
 event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,ROPE_MASTER_BE.get(),(be,side)->be.getItemHandler());
 event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,PhysicalHoses.BLOCK_ENTITY.get(),(be,side)->be.getFluidHandler(side));
 event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,FluidCouplings.BLOCK_ENTITY.get(),(be,side)->be.getFluidHandler(side));
 }
 @EventBusSubscriber(modid=MODID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
 public static class ClientModEvents {
 @SubscribeEvent public static void setup(FMLClientSetupEvent event){PartialModels.init();event.enqueueWork(() -> {
     for (var entry : ITEMS.getEntries()) TooltipModifier.REGISTRY.register(entry.get(),
         new ItemDescription.Modifier(entry.get(), net.createmod.catnip.lang.FontHelper.Palette.STANDARD_CREATE));
 });}
 @SubscribeEvent public static void models(ModelEvent.RegisterAdditional event){PartialModels.registerAdditional(event);}
 @SubscribeEvent public static void menus(RegisterMenuScreensEvent event){event.register(ROPE_MASTER_MENU.get(),RopeMasterScreen::new);event.register(BallCouplings.MENU.get(),BallCouplingScreen::new);event.register(FluidCouplings.MENU.get(),BallCouplingScreen::new);event.register(MULTIFUNCTION_CONFIGURATOR_MENU.get(),MultifunctionConfiguratorScreen::new);}
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){
 RopeLatchCompatibility.registerRenderer(event);SmartRopeCompatibility.registerRenderer(event);
 event.registerBlockEntityRenderer(BallCouplings.BLOCK_ENTITY.get(),BallCouplingRenderer::new);
 event.registerBlockEntityRenderer(FluidCouplings.BLOCK_ENTITY.get(),FluidCouplingRenderer::new);
 event.registerBlockEntityRenderer(PhysicalHoses.BLOCK_ENTITY.get(),PhysicalHoseRenderer::new);
 event.registerBlockEntityRenderer(HOSE_BRACKET_BE.get(),HoseBracketRenderer::new);
 event.registerBlockEntityRenderer(Rockers.BLOCK_ENTITY.get(),com.mikoalopex.createpowertrain.content.kinetics.rocker.RockerRenderer::new);
 }
 }
 @EventBusSubscriber(modid=MODID,value=Dist.CLIENT)
 public static class ClientEvents {
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){SmartRopeCompatibility.clientTick();CopycatDoorClient.tick();FluidCouplingClient.tick();com.mikoalopex.createpowertrain.content.kinetics.rocker.RockerClient.tick();}
 @SubscribeEvent public static void input(InputEvent.InteractionKeyMappingTriggered event){if(event.isUseItem()&&SmartRopeCompatibility.use()){event.setSwingHand(false);event.setCanceled(true);}}
 @SubscribeEvent public static void scroll(InputEvent.MouseScrollingEvent event){if(SmartRopeCompatibility.scroll(event.getScrollDeltaY()))event.setCanceled(true);}
 }
}
