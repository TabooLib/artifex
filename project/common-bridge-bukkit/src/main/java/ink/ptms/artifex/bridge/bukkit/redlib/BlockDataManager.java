package ink.ptms.artifex.bridge.bukkit.redlib;

import ink.ptms.artifex.bridge.bukkit.redlib.backend.BlockDataBackend;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import taboolib.module.nms.MinecraftVersion;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Manages persistent data attached to blocks, backed by either SQLite or chunk PersistentDataContainers
 * @author Redempt
 */
public class BlockDataManager {
	
	/**
	 * Creates a BlockDataManager backed by chunk PersistentDataContainers
	 * @param autoLoad Whether to automatically load data for newly-loaded chunks asynchronously
	 * @param events Whether to listen for events to automatically move and remove DataBlocks in response to their owning blocks being moved and removed
	 * @return The created BlockDataManager
	 */
	public static BlockDataManager createPDC(boolean autoLoad, boolean events) {
		Plugin plugin = JavaPlugin.getProvidingPlugin(BlockDataManager.class);
		BlockDataBackend backend = BlockDataBackend.pdc(plugin);
		return new BlockDataManager(plugin, backend, autoLoad, events);
	}
	
	/**
	 * Creates a BlockDataManager backed by SQLite
	 * @param path The path to the SQLite database
	 * @param autoLoad Whether to automatically load data for newly-loaded chunks
	 * @param events Whether to listen for events to automatically move and remove DataBlocks in response to their owning blocks being moved and removed
	 * @return The created BlockDataManager
	 */
	public static BlockDataManager createSQLite(Path path, boolean autoLoad, boolean events) {
		Plugin plugin = JavaPlugin.getProvidingPlugin(BlockDataManager.class);
		BlockDataBackend backend = BlockDataBackend.sqlite(path);
		return new BlockDataManager(plugin, backend, autoLoad, events);
	}
	
	/**
	 * Creates a BlockDataManager backed by SQLite if the server is running a version lower than 1.14, and chunk PersistentDataContainers otherwise
	 * @param path The path to the SQLite database
	 * @param autoLoad Whether to automatically load data for newly-loaded chunks
	 * @param events Whether to listen for events to automatically move and remove DataBlocks in response to their owning blocks being moved and removed
	 * @return The created BlockDataManager
	 */
	public static BlockDataManager createAuto(Path path, boolean autoLoad, boolean events) {
		Plugin plugin = JavaPlugin.getProvidingPlugin(BlockDataManager.class);
		BlockDataBackend backend = MinecraftVersion.INSTANCE.getMajorLegacy() >= 11400 ? BlockDataBackend.pdc(plugin) : BlockDataBackend.sqlite(path);
		return new BlockDataManager(plugin, backend, autoLoad, events);
	}
	
	private final BlockDataBackend backend;
	private final Plugin plugin;
	private BlockDataListener listener;
	private final Map<ChunkPosition, Map<BlockPosition, DataBlock>> dataBlocks = new ConcurrentHashMap<>();
	private final Map<ChunkPosition, CompletableFuture<Void>> loading = new ConcurrentHashMap<>();
	private final Set<ChunkPosition> modified = Collections.synchronizedSet(new HashSet<>());
	
	/**
	 * Asynchronously retrieves a DataBlock
	 * @param block The Block the data is attached to
	 * @param create Whether to create a new DataBlock if one does not exist for the given Block
	 * @return A CompletableFuture with the DataBlock
	 */
	public CompletableFuture<DataBlock> getDataBlockAsync(Block block, boolean create) {
		ChunkPosition pos = new ChunkPosition(block.getChunk());
		return load(pos).thenApply(n -> {
			BlockPosition bPos = new BlockPosition(block);
			DataBlock db = dataBlocks.get(pos).get(bPos);
			if (db != null) {
				return db;
			}
			if (!create) {
				return null;
			}
			db = new DataBlock(new HashMap<>(), bPos, block.getWorld().getName(), this);
			dataBlocks.get(pos).put(bPos, db);
			setModified(pos);
			return db;
		});
	}
	
	private BlockDataManager(Plugin plugin, BlockDataBackend backend, boolean autoLoad, boolean events) {
		this.plugin = plugin;
		this.backend = backend;
		new EventListener<>(plugin, ChunkUnloadEvent.class, e -> unload(new ChunkPosition(e.getChunk())));
		if (autoLoad) {
			new EventListener<>(plugin, ChunkLoadEvent.class, e -> load(new ChunkPosition(e.getChunk())));
		}
		if (events) {
			new BlockDataListener(this, plugin);
		}
	}
	
	/**
	 * Attempts to migrate SQLite from an older version of the database from the previous BlockDataManager library
	 * @return Whether a migration was completed successfully
	 */
	public boolean migrate() {
		return backend.attemptMigration(this);
	}
	
	/**
	 * @return The plugin that owns this BlockDataManager
	 */
	public Plugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Saves all data loaded in this BlockDataManager
	 */
	public void save() {
		List<ChunkPosition> modified = new ArrayList<>(this.modified);
		modified.forEach(c -> save(c, true));
		this.modified.clear();
		unwrap(backend.saveAll());
	}
	
	/**
	 * Saves all data loaded in this BlockDataManager and closes connections where needed
	 */
	public void saveAndClose() {
		save();
		unwrap(backend.close());
	}
	
	protected void setModified(ChunkPosition pos) {
		modified.add(pos);
	}
	
	/**
	 * Gets a DataBlock, creating one if it doesn't exist
	 * @param block The Block data will be attached to
	 * @return A DataBlock
	 */
	public DataBlock getDataBlock(Block block) {
		return getDataBlock(block, true);
	}
	
	private CompletableFuture<Void> save(ChunkPosition pos, boolean force) {
		if (!force && !modified.contains(pos)) {
			return CompletableFuture.completedFuture(null);
		}
		modified.remove(pos);
		Map<String, Object> map = new HashMap<>();
		Map<BlockPosition, DataBlock> blocks = dataBlocks.get(pos);
		if (blocks == null) {
			return CompletableFuture.completedFuture(null);
		}
		if (blocks.size() == 0) {
			dataBlocks.remove(pos);
			return backend.remove(pos);
		}
		blocks.forEach((k, v) -> {
			map.put(k.toString(), v.data);
		});
		return backend.save(pos, BlockDataContainerKt.serializeToByteArray(map, true));
	}
	
	private CompletableFuture<Void> unload(ChunkPosition pos) {
		CompletableFuture<Void> load = loading.remove(pos);
		if (load != null) {
			load.cancel(true);
			dataBlocks.remove(pos);
			return CompletableFuture.completedFuture(null);
		}
		return save(pos, false).thenRun(() -> dataBlocks.remove(pos));
	}
	
	/**
	 * Removes a DataBlock and its data from this BlockDataManager
	 * @param db The DataBlock to remove
	 */
	public void remove(DataBlock db) {
		ChunkPosition chunkPosition = db.getChunkPosition();
		setModified(chunkPosition);
		Optional.ofNullable(dataBlocks.get(chunkPosition)).ifPresent(m -> m.remove(db.getBlockPosition()));
	}
	
	/**
	 * Moves a DataBlock to a new location asynchronously
	 * @param db The DataBlock whose data should be moved
	 * @param location The Block to move the data to
	 * @return A CompletableFuture for the moving task
	 */
	public CompletableFuture<DataBlock> moveAsync(DataBlock db, Block location) {
		remove(db);
		ChunkPosition chunkPosition = new ChunkPosition(location);
		modified.add(chunkPosition);
		return getDataBlockAsync(location, true).thenApply(b -> {
			b.data = db.data;
			return b;
		});
	}
	
	/**
	 * Moves a DataBlock to a new location
	 * @param db The DataBlock whose data should be moved
	 * @param block The Block to move the data to
	 * @return The new DataBlock
	 */
	public DataBlock move(DataBlock db, Block block) {
		return unwrap(moveAsync(db, block));
	}
	
	/**
	 * Loads the data for a chunk asynchronously
	 * @param world The world the data is in
	 * @param cx The chunk X of the chunk the data is in
	 * @param cz The chunk Z of the chunk the data is in
	 * @return A CompletableFuture for the loading task
	 */
	public CompletableFuture<Void> loadAsync(World world, int cx, int cz) {
		return load(new ChunkPosition(cx, cz, world.getName()));
	}
	
	/**
	 * Loads the data for a chunk synchronously
	 * @param world The world the data is in
	 * @param cx The chunk X of the chunk the data is in
	 * @param cz The chunk Z of the chunk the data is in
	 */
	public void load(World world, int cx, int cz) {
		unwrap(loadAsync(world, cx, cz));
	}
	
	/**
	 * Unloads the data for a chunk asynchronously
	 * @param world The world the data is in
	 * @param cx The chunk X of the chunk the data is in
	 * @param cz The chunk Z of the chunk the data is in
	 * @return A CompletableFuture for the unloading task
	 */
	public CompletableFuture<Void> unloadAsync(World world, int cx, int cz) {
		return unload(new ChunkPosition(cx, cz, world.getName()));
	}
	
	/**
	 * Unloads the data for a chunk synchronously
	 * @param world The world the data is in
	 * @param cx The chunk X of the chunk the data is in
	 * @param cz The chunk Z of the chunk the data is in
	 */
	public void unload(World world, int cx, int cz) {
		unwrap(unloadAsync(world, cx, cz));
	}
	
	/**
	 * Gets the DataBlocks for a given chunk, if it is loaded already
	 * @param world The world the data is in
	 * @param cx The chunk X of the chunk the data is in
	 * @param cz The chunk Z of the chunk the data is in
	 * @return The DataBlocks if they are loaded, otherwise an empty collection
	 */
	public Collection<DataBlock> getLoaded(World world, int cx, int cz) {
		ChunkPosition pos = new ChunkPosition(cx, cz, world.getName());
		return Optional.ofNullable(dataBlocks.get(pos)).map(Map::values).orElseGet(ArrayList::new);
	}
	
	/**
	 * Checks whether the DataBlocks for a given chunk are loaded
	 * @param world The world the data is in
	 * @param cx The chunk X of the chunk the data is in
	 * @param cz The chunk Z of the chunk the data is in
	 * @return Whether the DataBlocks for the given chunk are loaded
	 */
	public boolean isLoaded(World world, int cx, int cz) {
		ChunkPosition pos = new ChunkPosition(cx, cz, world.getName());
		return dataBlocks.containsKey(pos);
	}
	
	@SuppressWarnings("unchecked")
	private CompletableFuture<Void> load(ChunkPosition pos) {
		if (dataBlocks.containsKey(pos)) {
			return CompletableFuture.completedFuture(null);
		}
		CompletableFuture<Void> load = loading.get(pos);
		if (load != null && !load.isDone()) {
			return load;
		}
		dataBlocks.put(pos, new HashMap<>());
		load = backend.load(pos).thenApply(s -> {
			if (s == null) {
				loading.remove(pos);
				return null;
			}
			Map<String, Object> map = BlockDataContainerKt.deserializeToMap(s, true);
			map.keySet().forEach(k -> load(k, (Map<String, Object>) map.get(k), pos));
			loading.remove(pos);
			return null;
		});
		loading.put(pos, load);
		return load;
	}
	
	private void load(String key, Map<String, Object> map, ChunkPosition pos) {
		String[] split = key.split(" ");
		int x = Integer.parseInt(split[0]);
		int y = Integer.parseInt(split[1]);
		int z = Integer.parseInt(split[2]);
		BlockPosition bPos = new BlockPosition(x, y, z);
		DataBlock db = new DataBlock(map, bPos, pos.getWorldName(), this);
		dataBlocks.get(pos).put(bPos, db);
	}
	
	/**
	 * Gets a DataBlock for the given Block
	 * @param block The Block data will be attached to
	 * @param create Whether to create a new DataBlock if one does not exist already
	 * @return The DataBlock, or null
	 */
	public DataBlock getDataBlock(Block block, boolean create) {
		return unwrap(getDataBlockAsync(block, create));
	}
	
	/**
	 * Loads all DataBlocks stored by this BlockDataManager. Not supported for PDC.
	 * @return A CompletableFuture for the loading task.
	 */
	@SuppressWarnings("unchecked")
	public CompletableFuture<Void> loadAll() {
		loading.values().forEach(f -> f.cancel(true));
		loading.clear();
		dataBlocks.clear();
		return backend.loadAll().thenApply(chunkMap -> {
			chunkMap.forEach((cPos, data) -> {
				Map<String, Object> chunkData = BlockDataContainerKt.deserializeToMap(data, true);
				chunkData.keySet().forEach(bPos -> load(bPos, (Map<String, Object>) chunkData.get(bPos), cPos));
			});
			return null;
		});
	}
	
	/**
	 * @return All DataBlocks currently loaded in this BlockDataManager
	 */
	public Set<DataBlock> getAllLoaded() {
		return dataBlocks.values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toSet());
	}
	
	private <T> T unwrap(CompletableFuture<T> future) {
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
