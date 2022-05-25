package ink.ptms.artifex.bridge.bukkit.redlib.event;

import ink.ptms.artifex.bridge.bukkit.redlib.DataBlock;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a DataBlock is destroyed
 * @author Redempt
 */
public class DataBlockDestroyEvent extends Event implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private final Event parent;
	private boolean cancelled = false;
	private final DataBlock db;
	private final DestroyCause cause;
	
	/**
	 * Creates a new BlockDataDestroyEvent
	 * @param db The DataBlock that was destroyed
	 * @param parent The Event which caused this one
	 * @param cause The cause of the DataBlock being destroyed
	 */
	public DataBlockDestroyEvent(DataBlock db, Event parent, DestroyCause cause) {
		this.db = db;
		this.parent = parent;
		this.cause = cause;
	}
	
	/**
	 * Sets whether the data should be removed from the block
	 * @param cancelled True to cancel removal of data from the block, false otherwise
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	/**
	 * Cancels the event which caused this one - meaning the block will not be destroyed
	 */
	public void cancelParent() {
		setCancelled(true);
		if (parent instanceof Cancellable) {
			((Cancellable) parent).setCancelled(true);
		}
		if (parent instanceof BlockExplodeEvent) {
			BlockExplodeEvent e = (BlockExplodeEvent) parent;
			Block block = db.getBlock();
			e.blockList().remove(db.getBlock());
			if (!cancelled) {
				e.blockList().add(block);
			}
		}
		if (parent instanceof EntityExplodeEvent) {
			EntityExplodeEvent e = (EntityExplodeEvent) parent;
			Block block = db.getBlock();
			e.blockList().remove(db.getBlock());
			if (!cancelled) {
				e.blockList().add(block);
			}
		}
	}
	
	/**
	 * @return The reason the DataBlock was destroyed
	 */
	public DestroyCause getCause() {
		return cause;
	}
	
	/**
	 * @return The event which caused this one
	 */
	public Event getParent() {
		return parent;
	}
	
	/**
	 * @return Whether this event is cancelled
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@NotNull
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	/**
	 * @return The DataBlock being removed
	 */
	public DataBlock getDataBlock() {
		return db;
	}
	
	/**
	 * @return The Block being destroyed
	 */
	public Block getBlock() {
		return db.getBlock();
	}
	
	public enum DestroyCause {
		
		PLAYER_BREAK,
		COMBUST,
		FADE,
		LEAVES_DECAY,
		ENTITY_EXPLOSION,
		BLOCK_EXPLOSION,
		ENTITY,
		LIQUID,
		CAKE,
		ZOMBIE_BREAK_DOOR,
		SILVERFISH,
		WITHER,
		ENDER_DRAGON,
		FALLING_BLOCK,
		ENDERMAN;

		public boolean isTransferred() {
			return this == FALLING_BLOCK || this == ENDERMAN;
		}
	}
	
}
