package ink.ptms.artifex.bridge.bukkit.redlib;

import ink.ptms.artifex.bridge.bukkit.redlib.event.DataBlockDestroyEvent;
import ink.ptms.artifex.bridge.bukkit.redlib.event.DataBlockMoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import taboolib.module.nms.MinecraftVersion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BlockDataListener implements Listener {

    private final BlockDataManager manager;

    public BlockDataListener(BlockDataManager manager, Plugin plugin) {
        this.manager = manager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void fireDestroy(DataBlock db, Event parent, DataBlockDestroyEvent.DestroyCause cause) {
        if (db == null) {
            return;
        }
        DataBlockDestroyEvent ev = new DataBlockDestroyEvent(db, parent, cause);
        Bukkit.getPluginManager().callEvent(ev);
        if (!ev.isCancelled()) {
            manager.remove(db);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        DataBlock db = manager.getDataBlock(e.getBlock(), false);
        fireDestroy(db, e, DataBlockDestroyEvent.DestroyCause.PLAYER_BREAK);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplode(BlockExplodeEvent e) {
        handleExplosion(e.blockList(), e, DataBlockDestroyEvent.DestroyCause.BLOCK_EXPLOSION);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        handleExplosion(e.blockList(), e, DataBlockDestroyEvent.DestroyCause.ENTITY_EXPLOSION);
    }

    private void handleExplosion(List<Block> blocks, Cancellable e, DataBlockDestroyEvent.DestroyCause cause) {
        List<DataBlock> toRemove = new ArrayList<>();
        blocks.forEach(b -> {
            DataBlock db = manager.getDataBlock(b, false);
            if (db == null) {
                return;
            }
            DataBlockDestroyEvent ev = new DataBlockDestroyEvent(db, (Event) e, cause);
            Bukkit.getPluginManager().callEvent(ev);
            // 如果 DataBlockDestroyEvent 被取消
            if (ev.isCancelled()) {
                // 在破坏事件下不会阻止事件的发生，而是阻止方块被破坏
                if (e instanceof EntityExplodeEvent) {
                    ((EntityExplodeEvent) e).blockList().remove(b);
                } else if (e instanceof BlockExplodeEvent) {
                    ((BlockExplodeEvent) e).blockList().remove(b);
                } else {
                    e.setCancelled(true);
                }
            } else {
                toRemove.add(db);
            }
        });
        if (e.isCancelled()) {
            return;
        }
        toRemove.forEach(manager::remove);
    }

    /**
     * 方块被烧毁
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombust(BlockBurnEvent e) {
        DataBlock db = manager.getDataBlock(e.getBlock(), false);
        fireDestroy(db, e, DataBlockDestroyEvent.DestroyCause.COMBUST);
    }

    /**
     * 方块自然消退（冰、雪融化）
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFade(BlockFadeEvent e) {
        DataBlock db = manager.getDataBlock(e.getBlock(), false);
        fireDestroy(db, e, DataBlockDestroyEvent.DestroyCause.FADE);
    }

    /**
     * 树叶消散
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent e) {
        DataBlock db = manager.getDataBlock(e.getBlock(), false);
        fireDestroy(db, e, DataBlockDestroyEvent.DestroyCause.LEAVES_DECAY);
    }

    /**
     * 活塞推出方块
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        handlePiston(e.getBlocks(), e);
    }

    /**
     * 活塞收回方块
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        handlePiston(e.getBlocks(), e);
    }

    private void handlePiston(List<Block> blocks, BlockPistonEvent e) {
        List<DataBlock> toMove = new ArrayList<>();
        blocks.forEach(b -> {
            DataBlock db = manager.getDataBlock(b, false);
            if (db == null) {
                return;
            }
            Block destination = db.getBlock().getRelative(e.getDirection());
            DataBlockMoveEvent ev = new DataBlockMoveEvent(db, destination, e);
            Bukkit.getPluginManager().callEvent(ev);
            if (!ev.isCancelled()) {
                toMove.add(db);
            } else {
                // 当 DataBlockMoveEvent 被阻止时，活塞事件也将被阻止
                e.setCancelled(true);
            }
        });
        if (e.isCancelled()) {
            return;
        }
        Map<Block, Map<String, Object>> moved = new HashMap<>();
        toMove.forEach(db -> {
            Block destination = db.getBlock().getRelative(e.getDirection());
            moved.put(destination, db.data);
        });
        toMove.forEach(manager::remove);
        moved.forEach((block, data) -> {
            manager.getDataBlock(block, false).data = data;
        });
    }

    /**
     * 液体流动
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        if (MinecraftVersion.INSTANCE.getMajorLegacy() < 11300 || !(e.getToBlock().getBlockData() instanceof Waterlogged)) {
            DataBlock db = manager.getDataBlock(e.getToBlock(), false);
            fireDestroy(db, e, DataBlockDestroyEvent.DestroyCause.LIQUID);
        }
    }

    /**
     * 玩家吃蛋糕
     */
    @SuppressWarnings("ConstantConditions")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCake(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.CAKE && e.getClickedBlock().getData() == 5) {
            DataBlock db = manager.getDataBlock(e.getClickedBlock(), false);
            fireDestroy(db, e, DataBlockDestroyEvent.DestroyCause.CAKE);
        }
    }

    private boolean isLegacyAir(Material material) {
        return MinecraftVersion.INSTANCE.getMajorLegacy() >= 11500 ? material.isAir() : material == Material.AIR;
    }

    /**
     * 实体修改方块
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (isLegacyAir(e.getTo())) {
            DataBlockDestroyEvent.DestroyCause destroyCause;
            if (e.getEntity() instanceof Zombie) {
                destroyCause = DataBlockDestroyEvent.DestroyCause.ZOMBIE_BREAK_DOOR;
            } else if (e.getEntity() instanceof Silverfish) {
                destroyCause = DataBlockDestroyEvent.DestroyCause.SILVERFISH;
            } else if (e.getEntity() instanceof Wither) {
                destroyCause = DataBlockDestroyEvent.DestroyCause.WITHER;
            } else if (e.getEntity() instanceof EnderDragon) {
                destroyCause = DataBlockDestroyEvent.DestroyCause.ENDER_DRAGON;
            } else if (e.getEntity() instanceof Enderman || e.getEntity() instanceof FallingBlock) {
                // TODO 末影人与方块坠落将进行数据转移
//                e.setCancelled(true);
                return;
            } else {
                destroyCause = DataBlockDestroyEvent.DestroyCause.ENTITY;
            }
            DataBlock db = manager.getDataBlock(e.getBlock(), false);
            fireDestroy(db, e, destroyCause);
        }
    }
}
