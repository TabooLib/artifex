package ink.ptms.artifex.bridge.bukkit.redlib.backend;

import ink.ptms.artifex.bridge.bukkit.redlib.*;
import ink.ptms.artifex.bridge.bukkit.redlib.sql.SQLHelper;
import org.bukkit.block.Block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class SQLiteBackend implements BlockDataBackend {

    private final SQLHelper helper;
    private final Executor exec = Executors.newSingleThreadExecutor();
    private final Path path;

    public SQLiteBackend(Path path) {
        this.path = path;
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        helper = new SQLHelper(SQLHelper.openSQLite(path));
        helper.execute("PRAGMA synchronous = OFF;");
        helper.executeUpdate("CREATE TABLE IF NOT EXISTS data (x INT, z INT, world STRING, data BLOB, PRIMARY KEY (x, z, world));");
        helper.setCommitInterval(5 * 20 * 60);
    }

    @Override
    public boolean attemptMigration(BlockDataManager manager) {
        try {
            DatabaseMetaData metadata = helper.getConnection().getMetaData();
            ResultSet results = metadata.getTables(null, null, "blocks", null);
            if (!results.next()) {
                return false;
            }
            results.close();
            Files.copy(path, path.getParent().resolve(path.getFileName() + "_old"), StandardCopyOption.REPLACE_EXISTING);
            helper.queryResults("SELECT x, y, z, world, data FROM blocks;").forEach(r -> {
                int x = r.get(1);
                int y = r.get(2);
                int z = r.get(3);
                String worldName = r.getString(4);
                byte[] data = r.getBytes(5);
                Locations.waitForWorld(worldName, world -> {
                    Block block = world.getBlockAt(x, y, z);
                    DataBlock db = manager.getDataBlock(block);
                    Map<String, Object> map = BlockDataContainerKt.deserializeToMap(data, true);
                    map.keySet().forEach(k -> db.set(k, map.get(k)));
                });
            });
            helper.executeUpdate("DROP TABLE blocks;");
            manager.save();
            return true;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public CompletableFuture<byte[]> load(ChunkPosition pos) {
        return CompletableFuture.supplyAsync(() -> helper.querySingleResultBytes("SELECT data FROM data WHERE x=? AND z=? AND world=?", pos.getX(), pos.getZ(), pos.getWorld().getName()), exec);
    }

    @Override
    public CompletableFuture<Void> save(ChunkPosition pos, byte[] data) {
        return CompletableFuture.runAsync(() -> helper.executeUpdate("REPLACE INTO data VALUES (?, ?, ?, ?);", pos.getX(), pos.getZ(), pos.getWorld().getName(), data), exec);
    }

    @Override
    public CompletableFuture<Void> remove(ChunkPosition pos) {
        return CompletableFuture.runAsync(() -> helper.executeUpdate("DELETE FROM data WHERE x=? AND z=? AND world=?;", pos.getX(), pos.getZ(), pos.getWorld().getName()), exec);
    }

    @Override
    public CompletableFuture<Void> saveAll() {
        return CompletableFuture.runAsync(helper::commit, exec);
    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            saveAll();
            helper.close();
        }, exec);
    }

    @Override
    public CompletableFuture<Map<ChunkPosition, byte[]>> loadAll() {
        return CompletableFuture.supplyAsync(() -> {
            SQLHelper.Results results = helper.queryResults("SELECT * FROM data;");
            Map<ChunkPosition, byte[]> map = new HashMap<>();
            results.forEach(r -> {
                int x = r.get(1);
                int z = r.get(2);
                String world = r.getString(3);
                ChunkPosition pos = new ChunkPosition(x, z, world);
                byte[] data = r.getBytes(4);
                map.put(pos, data);
            });
            return map;
        }, exec);
    }

}
