package net.fexcraft.mod.states;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TreeMap;
import java.util.UUID;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.capabilities.sign.SignCapabilitySerializer;
import net.fexcraft.lib.mc.network.handlers.NBTTagCompoundPacketHandler;
import net.fexcraft.lib.mc.registry.FCLRegistry;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.fsmm.util.Print;
import net.fexcraft.mod.lib.fcl.PacketHandler;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkPos;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.capabilities.ChunkCapability;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.fexcraft.mod.states.api.capabilities.SignTileEntityCapability;
import net.fexcraft.mod.states.api.capabilities.WorldCapability;
import net.fexcraft.mod.states.cmds.*;
import net.fexcraft.mod.states.events.ChunkEvents;
import net.fexcraft.mod.states.events.ConfigEvents;
import net.fexcraft.mod.states.events.PlayerEvents;
import net.fexcraft.mod.states.events.WorldEvents;
import net.fexcraft.mod.states.guis.GuiHandler;
import net.fexcraft.mod.states.guis.Listener;
import net.fexcraft.mod.states.guis.Receiver;
import net.fexcraft.mod.states.impl.SignMailbox;
import net.fexcraft.mod.states.impl.SignShop;
import net.fexcraft.mod.states.impl.capabilities.ChunkCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.PlayerCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.SignTileEntityCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.WorldCapabilityUtil;
import net.fexcraft.mod.states.packets.ImagePacket;
import net.fexcraft.mod.states.packets.ImagePacketHandler;
import net.fexcraft.mod.states.util.Config;
import net.fexcraft.mod.states.util.ForcedChunksManager;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.ImageUtil;
import net.fexcraft.mod.states.util.MessageSender;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.StatesPermissions;
import net.fexcraft.mod.states.util.TaxSystem;
import net.fexcraft.mod.states.util.UpdateHandler;
import net.minecraft.command.CommandHandler;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import static net.minecraft.server.MinecraftServer.getServer;

@Mod(modid = States.MODID, name = "States", version = States.VERSION, dependencies = "required-after:fcl", /*serverSideOnly = true,*/ guiFactory = "net.fexcraft.mod.states.util.GuiFactory", acceptedMinecraftVersions = "*", acceptableRemoteVersions = "*")
public class States {
	
	public static final String VERSION = "1.3.3";
	public static final String MODID = "states";
	public static final String ADMIN_PERM = "states.external.admin";
	public static final String PREFIX = "&0[&2States&0]";
	//
	public static final TreeMap<ChunkPos, Chunk> CHUNKS = new TreeMap<>();
	public static final TreeMap<Integer, District> DISTRICTS = new TreeMap<Integer, District>();
	public static final TreeMap<Integer, Municipality> MUNICIPALITIES = new TreeMap<Integer, Municipality>();
	public static final TreeMap<Integer, State> STATES = new TreeMap<Integer, State>();
	public static final TreeMap<UUID, PlayerCapability> PLAYERS = new TreeMap<UUID, PlayerCapability>();
	public static final TreeMap<Integer, List<ChunkPos>> LOADED_CHUNKS = new TreeMap<>();
	//
	public static final String DEF_UUID = "66e70cb7-1d96-487c-8255-5c2d7a2b6a0e";
	public static final String CONSOLE_UUID = "f78a4d8d-d51b-4b39-98a3-230f2de0c670";
	public static final String DEFAULT_ICON = "https://i.imgur.com/LwuKE0b.png";
	public static Account SERVERACCOUNT;
	//
	@Mod.Instance(MODID)
	public static States INSTANCE;
	public static Timer TAX_TIMER, DATA_MANAGER, IMG_TIMER;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event){
		Config.initialize(event);
		StatesPermissions.init();
		SignCapabilitySerializer.addListener(SignShop.class);
		SignCapabilitySerializer.addListener(SignMailbox.class);
		CapabilityManager.INSTANCE.register(SignTileEntityCapability.class, new SignTileEntityCapabilityUtil.Storage(), new SignTileEntityCapabilityUtil.Callable());
		CapabilityManager.INSTANCE.register(ChunkCapability.class, new ChunkCapabilityUtil.Storage(), new ChunkCapabilityUtil.Callable());
		CapabilityManager.INSTANCE.register(WorldCapability.class, new WorldCapabilityUtil.Storage(), new WorldCapabilityUtil.Callable());
		CapabilityManager.INSTANCE.register(PlayerCapability.class, new PlayerCapabilityUtil.Storage(), new PlayerCapabilityUtil.Callable());
		ForgeChunkManager.setForcedChunkLoadingCallback(this, new ForcedChunksManager());
		FCLRegistry.newAutoRegistry(MODID);
	}

	private ChunkEvents chunkEvents = new ChunkEvents();
	private ConfigEvents configEvents = new ConfigEvents();
	private PlayerEvents playerEvents = new PlayerEvents();
	private WorldEvents worldEvents = new WorldEvents();

	@Mod.EventHandler
	public void properInit(FMLInitializationEvent event){
		MinecraftForge.EVENT_BUS.register(chunkEvents);

		if(event.getSide().isServer()) {
			CommandHandler ch = (CommandHandler) getServer().getCommandManager();
			ch.registerCommand(new AdminCmd());
			ch.registerCommand(new ChunkCmd());
			ch.registerCommand(new DebugCmd());
			ch.registerCommand(new DistrictCmd());
			ch.registerCommand(new GuiCmd());
			ch.registerCommand(new MailCmd());
			ch.registerCommand(new MunicipalityCmd());
			ch.registerCommand(new NickCmd());
			ch.registerCommand(new StateCmd());
		}

		MinecraftForge.EVENT_BUS.register(configEvents);
		FMLCommonHandler.instance().bus().register(configEvents);

		MinecraftForge.EVENT_BUS.register(playerEvents);
		FMLCommonHandler.instance().bus().register(playerEvents);

		MinecraftForge.EVENT_BUS.register(worldEvents);
		FMLCommonHandler.instance().bus().register(worldEvents);
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
		if(event.getSide().isClient()){
			MinecraftForge.EVENT_BUS.register(new net.fexcraft.mod.states.guis.LocationUpdate());
		}
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event){
		NBTTagCompoundPacketHandler.addListener(Side.SERVER, new Listener());
		NBTTagCompoundPacketHandler.addListener(Side.CLIENT, new Receiver());
		//
		PermissionAPI.registerNode(ADMIN_PERM, DefaultPermissionLevel.OP, "States Admin Permission");
		SERVERACCOUNT = DataManager.getAccount("server:states", false, true);
		//
		PacketHandler.getInstance().registerMessage(ImagePacketHandler.Client.class, ImagePacket.class, 29910, Side.CLIENT);
		PacketHandler.getInstance().registerMessage(ImagePacketHandler.Server.class, ImagePacket.class, 29911, Side.SERVER);
		UpdateHandler.initialize();
	}
	
	private static File statesdir;
	
	public static final File updateSaveDirectory(World world){
		return statesdir = new File(world.getSaveHandler().getWorldDirectory(), "states/");
	}
	
	public static final File getSaveDirectory(){
		return statesdir;
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event){
		if(MessageSender.RECEIVER == null) Config.updateWebHook();
	}
	
	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event){
		ForcedChunksManager.check();
		//
		LocalDateTime midnight = LocalDateTime.of(LocalDate.now(ZoneOffset.systemDefault()), LocalTime.MIDNIGHT);
		long mid = midnight.toInstant(ZoneOffset.UTC).toEpochMilli(); long date = Time.getDate();
		while((mid += Config.TAX_INTERVAL) < date);
		if(TAX_TIMER == null){
			(TAX_TIMER = new Timer()).schedule(new TaxSystem(), new Date(mid), Config.TAX_INTERVAL);
		}
		if(DATA_MANAGER == null){
			(DATA_MANAGER = new Timer()).schedule(new StateUtil(), new Date(mid), Print.dev() ? 60000 : Time.MIN_MS * 15);
		}
		//
		if(IMG_TIMER == null){
			(IMG_TIMER = new Timer()).schedule(new ImageCache(), new Date(mid), 1000);
			if(event.getSide().isClient()){
				IMG_TIMER.schedule(new ImageUtil(), new Date(mid), 1000);
			}
		}
	}
	
	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event){
		if(MessageSender.RECEIVER != null) MessageSender.RECEIVER.halt();
	}

}
