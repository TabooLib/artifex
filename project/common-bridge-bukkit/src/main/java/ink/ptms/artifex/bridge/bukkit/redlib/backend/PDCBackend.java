package ink.ptms.artifex.bridge.bukkit.redlib.backend;

import ink.ptms.artifex.bridge.bukkit.redlib.BlockDataManager;
import ink.ptms.artifex.bridge.bukkit.redlib.ChunkPosition;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

class PDCBackend implements BlockDataBackend {
	
	private final NamespacedKey key;
	
	public PDCBackend(Plugin plugin) {
		key = new NamespacedKey(plugin, "blockData");
	}
	
	@Override
	public CompletableFuture<byte[]> load(ChunkPosition pos) {
		PersistentDataContainer pdc = pos.getWorld().getChunkAt(pos.getX(), pos.getZ()).getPersistentDataContainer();
		return CompletableFuture.completedFuture(pdc.get(key, PersistentDataType.BYTE_ARRAY));
	}
	
	@Override
	public CompletableFuture<Void> save(ChunkPosition pos, byte[] data) {
		PersistentDataContainer pdc = pos.getWorld().getChunkAt(pos.getX(), pos.getZ()).getPersistentDataContainer();
		pdc.set(key, PersistentDataType.BYTE_ARRAY, data);
		return CompletableFuture.completedFuture(null);
	}
	
	@Override
	public CompletableFuture<Void> remove(ChunkPosition pos) {
		PersistentDataContainer pdc = pos.getWorld().getChunkAt(pos.getX(), pos.getZ()).getPersistentDataContainer();
		pdc.remove(key);
		return CompletableFuture.completedFuture(null);
	}
	
	@Override
	public CompletableFuture<Void> saveAll() {
		return CompletableFuture.completedFuture(null);
	}
	
	@Override
	public CompletableFuture<Void> close() {
		return CompletableFuture.completedFuture(null);
	}
	
	@Override
	public CompletableFuture<Map<ChunkPosition, byte[]>> loadAll() {
		throw new UnsupportedOperationException("PDC backend cannot access all data blocks");
	}
	
	@Override
	public boolean attemptMigration(BlockDataManager manager) {
		return false;
	}
	
}
