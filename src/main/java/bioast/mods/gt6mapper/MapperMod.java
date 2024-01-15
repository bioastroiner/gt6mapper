package bioast.mods.gt6mapper;

import bioast.mods.gt6mapper.hooks.GTHooks;
import bioast.mods.gt6mapper.item.ItemEmptyProspectMap;
import bioast.mods.gt6mapper.item.ItemProspectMap;
import bioast.mods.gt6mapper.network.MapPacketHandler;
import bioast.mods.gt6mapper.proxy.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.api.Abstract_Mod;
import gregapi.api.Abstract_Proxy;
import gregapi.data.*;
import gregapi.oredict.OreDictManager;
import gregapi.random.IHasWorldAndCoords;
import gregapi.recipes.Recipe;
import gregapi.tileentity.computer.ITileEntityUSBPort;
import gregapi.tileentity.delegate.DelegatorTileEntity;
import gregapi.util.CR;
import gregapi.util.OM;
import gregapi.util.ST;
import gregapi.util.UT;
import journeymap.client.JourneymapClient;
import journeymap.common.Journeymap;
import journeymap.server.JourneymapServer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static bioast.mods.gt6mapper.MapperMod.*;
import static gregapi.data.CS.*;

@Mod(modid = MODID, version = VERSION, name = MODNAME, dependencies = DEPENDENCIES)
public class MapperMod extends Abstract_Mod {
	public static final String DEPENDENCIES = "";//"required-after:gregapi";
	public static final String MODID = "GRADLETOKEN_MODID";
	public static final String MODNAME = "GRADLETOKEN_MODNAME";
	public static final String VERSION = "GRADLETOKEN_VERSION";
	public static final String GROUPNAME = "GRADLETOKEN_GROUPNAME";

	public static final Logger debug = LogManager.getLogger(MODID);
	public static gregapi.code.ModData MOD_DATA = new gregapi.code.ModData(MODID, MODNAME);
	public static MapperMod instance;
	@SidedProxy(clientSide = "bioast.mods.gt6mapper.proxy.ClientProxy", serverSide = "bioast.mods.gt6mapper.proxy.CommonProxy")
	public static CommonProxy proxy;
	public static ItemProspectMap mapWritten;
	public static Item mapEmpty;

	@Override
	public String getModID() {
		return MODID;
	}

	@Override
	public String getModName() {
		return MODNAME;
	}

	@Override
	public String getModNameForLog() {
		return MODNAME;
	}

	@Override
	public Abstract_Proxy getProxy() {
		return proxy;
	}

	// Do not change these 7 Functions. Just keep them this way.
	@cpw.mods.fml.common.Mod.EventHandler
	public final void onPreLoad(cpw.mods.fml.common.event.FMLPreInitializationEvent aEvent) {
		onModPreInit(aEvent);
	}

	@cpw.mods.fml.common.Mod.EventHandler
	public final void onLoad(cpw.mods.fml.common.event.FMLInitializationEvent aEvent) {
		onModInit(aEvent);
	}

	@cpw.mods.fml.common.Mod.EventHandler
	public final void onPostLoad(cpw.mods.fml.common.event.FMLPostInitializationEvent aEvent) {
		onModPostInit(aEvent);
	}

	@cpw.mods.fml.common.Mod.EventHandler
	public final void onServerStarting(cpw.mods.fml.common.event.FMLServerStartingEvent aEvent) {
		onModServerStarting(aEvent);
	}

	@cpw.mods.fml.common.Mod.EventHandler
	public final void onServerStarted(cpw.mods.fml.common.event.FMLServerStartedEvent aEvent) {
		onModServerStarted(aEvent);
	}

	@cpw.mods.fml.common.Mod.EventHandler
	public final void onServerStopping(cpw.mods.fml.common.event.FMLServerStoppingEvent aEvent) {
		onModServerStopping(aEvent);
	}

	@cpw.mods.fml.common.Mod.EventHandler
	public final void onServerStopped(cpw.mods.fml.common.event.FMLServerStoppedEvent aEvent) {
		onModServerStopped(aEvent);
	}

	@Override
	public void onModPreInit2(FMLPreInitializationEvent aEvent) {
		instance = this;
		proxy.preInit(aEvent);
		mapWritten = (ItemProspectMap) new ItemProspectMap().setUnlocalizedName("prospectingMap").setMaxStackSize(1);
		mapEmpty = new ItemEmptyProspectMap().setUnlocalizedName("emptyProspectingMap").setMaxStackSize(16);
		GameRegistry.registerItem(mapWritten, mapWritten.getUnlocalizedName(), MODID);
		GameRegistry.registerItem(mapEmpty, mapEmpty.getUnlocalizedName(), MODID);
		OreDictManager.registerOreSafe("mapCraftingTier0", ST.make(mapEmpty, 1, 0));
		OreDictManager.registerOreSafe("mapCraftingTier1", ST.make(mapEmpty, 1, 1));
		OreDictManager.registerOreSafe("mapCraftingTier2", ST.make(mapEmpty, 1, 2));
		OreDictManager.registerOreSafe("mapCraftingTier3", ST.make(mapEmpty, 1, 3));
		OreDictManager.registerOreSafe("mapCraftingTier4", ST.make(mapEmpty, 1, 4));
		LH.add(mapWritten.getUnlocalizedName(), "Geographical Prospecting Map");
		LH.add(mapEmpty.getUnlocalizedName(), "Empty Geographical Prospecting Map");
        /*
        //GameRegistry.registerTileEntity(CartographyTableTE.class,"tileCartographyTable");
        new MultiTileEntityRegistry("scanner.multitileentity");
*/
	}

	@Override
	public void onModInit2(FMLInitializationEvent aEvent) {
		MinecraftForge.EVENT_BUS.register(this);
//        FMLCommonHandler.instance().bus().register(eventListener); // we're getting events off this bus too

		proxy.init(aEvent);
		NetworkRegistry.INSTANCE.newEventDrivenChannel(ItemProspectMap.MAP_ID_NAME).register(new MapPacketHandler());
        /*
        MultiTileEntityBlock aMachine = MultiTileEntityBlock.getOrCreate(MD.GT.mID, "machine"      , MaterialMachines.instance , Block.soundTypeMetal, TOOL_wrench , 0, 0, 15, F, F
        );
        MultiTileEntityRegistry.getRegistry("scanner.multitileentity")
            .add("Cartography Table","Machines",0,0, MultiTileEntityCartographyTable.class,0,16,aMachine,
                UT.NBT.make(CS.NBT_MATERIAL, MT.Steel));
                */

		/*Player Start with larger map not smaller map as they are less accurate and more broad*/
		ItemStack mapHighScale = ST.make(mapEmpty,1,4);
		//UT.NBT.set(mapHighScale,UT.NBT.make("basic",true,""))
//        CR.shapeless(mapHighScale,CR.DEF,new Object[]{
//            ST.make(Items.map,1,W),
//        });
		CR.shaped(mapHighScale,CR.DEF,
				"I",
				"M",
				"",
				'M',ST.make(Items.map,1,W));
		RM.Printer.addFakeRecipe(F, ST.array(ST.make(mapEmpty, 1, W), IL.USB_Stick_1.getWithName(0, "Containing scanned Prospecting Map")), ST.array(ST.make(mapWritten, 1, W)), null, null, FL.array(FL.mul(DYE_FLUIDS_CHEMICAL[DYE_INDEX_Yellow], 1, 9, T), FL.mul(DYE_FLUIDS_CHEMICAL[DYE_INDEX_Magenta], 1, 9, T), FL.mul(DYE_FLUIDS_CHEMICAL[DYE_INDEX_Cyan], 1, 9, T), FL.mul(DYE_FLUIDS_CHEMICAL[DYE_INDEX_Black], 1, 9, T)), ZL_FS, 64, 16, 0);
		RM.ScannerVisuals.addFakeRecipe(F, ST.array(ST.make(mapWritten, 1, W), IL.USB_Stick_1.get(1)), ST.array(IL.USB_Stick_1.getWithName(1, "Containing scanned Prospecting Map"), ST.make(mapWritten, 1, W)), null, null, ZL_FS, ZL_FS, 64, 16, 0);
	}

	@SubscribeEvent
	public void onRecipeEvent(GTHooks.OnFindGTRecipeEvent event) {
		// TODO implement Copying & Printing Recipes for maps
		Recipe rRecipe = null;
		if (event.aRecipeMap == RM.Printer) {
			/* Print Maps in Machine*/
			ItemStack tUSB = null, tPaper = null;
			NBTTagCompound tData = null;
			for (ItemStack aInput : event.aInputs)
				if (aInput != null) {
					if (tData == null) {
						if (OM.is_(OD_USB_STICKS[1], aInput)) {
							if (!aInput.hasTagCompound()) return;
							tUSB = aInput;
							tData = tUSB.getTagCompound().getCompoundTag(NBT_USB_DATA);
						} else if (OM.is_(OD_USB_CABLES[1], aInput)) {
							if (event.aTileEntity == null) return;
							tUSB = aInput;
							for (byte tSide : ALL_SIDES_VALID_ONLY[tUSB.hasTagCompound() && tUSB.getTagCompound().hasKey(NBT_USB_DIRECTION) ? tUSB.getTagCompound().getByte(NBT_USB_DIRECTION) : SIDE_ANY]) {
								DelegatorTileEntity<TileEntity> tDelegator = event.aTileEntity.getAdjacentTileEntity(tSide);
								if (tDelegator.mTileEntity instanceof ITileEntityUSBPort) {
									tData = ((ITileEntityUSBPort) tDelegator.mTileEntity).getUSBData(tDelegator.mSideOfTileEntity, 1);
									if (tData != null) if (tData.hasNoTags()) tData = null;
									else break;
								}
							}
						} else {
							tPaper = aInput;
						}
					} else {
						tPaper = aInput;
					}
				}
			if (tData == null || tData.hasNoTags()) return;
			short tMapID = (!tData.hasKey(ItemProspectMap.NBT_TAG)) ? -1 : tData.getShort(ItemProspectMap.NBT_TAG);
			rRecipe = new Recipe(F, F, F,
					ST.array(ST.amount(1, tPaper), ST.amount(0, tUSB)),
					ST.array(ST.make(1, 1, tMapID))
					, null, null, FL.array(FL.mul(DYE_FLUIDS_CHEMICAL[DYE_INDEX_Black], 1, 9, T), FL.mul(DYE_FLUIDS_CHEMICAL[DYE_INDEX_Cyan], 1, 9, T), FL.mul(DYE_FLUIDS_CHEMICAL[DYE_INDEX_Magenta], 1, 9, T), FL.mul(DYE_FLUIDS_CHEMICAL[DYE_INDEX_Yellow], 1, 9, T)), null, 64, 16, 0);

		} else if (event.aRecipeMap == RM.ScannerVisuals) {
			/* Scan Maps in Machine*/
			ItemStack tUSB = null, tScanned = null;
			for (ItemStack aInput : event.aInputs)
				if (ST.valid(aInput)) {
					if (ST.invalid(tUSB) && OM.is_(OD_USB_STICKS[1], aInput)) tUSB = aInput;
					else tScanned = aInput;
					if (ST.valid(tUSB) && ST.valid(tScanned)) {
						if (tScanned.getItem() == mapWritten) {
							rRecipe = new Recipe(F, F, F, ST.array(ST.amount(1, tScanned), ST.amount(1, tUSB)), ST.array(ST.amount(1, tUSB), ST.amount(1, tScanned)), null, null, null, null, 64, 16, 0);
							if (!rRecipe.mOutputs[0].hasTagCompound())
								rRecipe.mOutputs[0].setTagCompound(UT.NBT.make());
							rRecipe.mOutputs[0].getTagCompound().setTag(NBT_USB_DATA, UT.NBT.make(ItemProspectMap.NBT_TAG, ST.meta_(tScanned)));
							rRecipe.mOutputs[0].getTagCompound().setByte(NBT_USB_TIER, (byte) 1);
						}
					}
				}
		}
		// to dynamically generate Recipes !
		// gregapi.recipes.maps.RecipeMapPrinter.findRecipe
		event.accept(rRecipe);
	}

	@Override
	public void onModPostInit2(FMLPostInitializationEvent aEvent) {
		proxy.postInit(aEvent);
	}

	@Override
	public void onModServerStarting2(FMLServerStartingEvent aEvent) {
		proxy.serverStarting(aEvent);
	}

	@Override
	public void onModServerStarted2(FMLServerStartedEvent aEvent) {

	}

	@Override
	public void onModServerStopping2(FMLServerStoppingEvent aEvent) {

	}

	@Override
	public void onModServerStopped2(FMLServerStoppedEvent aEvent) {

	}
}
