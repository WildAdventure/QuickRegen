/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.quickregen;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;

import wild.api.WildCommons;
import wild.api.item.ItemBuilder;

public class QuickRegen extends JavaPlugin implements Listener {

	public static final String USE_PERM = "quickregen.use";
	
	public static ItemStack wandChunk;
	public static ItemStack wand5x5;
	public static ItemStack wand9x9;
	public static ItemStack wand15x15;
	public static ItemStack wand25x25;
	
	
	@Override
	public void onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("WildCommons")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] Richiesto WildCommons!");
			setEnabled(false);
			return;
		}
		
		wandChunk = makeWand("modalità chunk");
		wand5x5 = makeWand("modalità 5x5");
		wand9x9 = makeWand("modalità 9x9");
		wand15x15 = makeWand("modalità 15x15");
		wand25x25 = makeWand("modalità 25x25");
		
		Bukkit.getPluginManager().registerEvents(this, this);
		new CommandHandler(this, "quickregen");
	}
	
	private ItemStack makeWand(String action) {
		return ItemBuilder
				.of(Material.STICK)
				.name(ChatColor.LIGHT_PURPLE + "Regen Wand " + ChatColor.GRAY + "(" + ChatColor.GOLD + action + ChatColor.GRAY + ")")
				.lore(ChatColor.GRAY + "Punta e clicca per rigenerare un chunk o una regione.")
				.enchant(Enchantment.DURABILITY)
				.build();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (
				(event.hasItem()) &&
				(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
				(event.getItem().getType() == Material.STICK) &&
				(event.getItem().containsEnchantment(Enchantment.DURABILITY)) &&
				(event.getPlayer().hasPermission(USE_PERM))) {
			
			Block target = event.hasBlock() ? event.getClickedBlock() : event.getPlayer().getTargetBlock((Set<Material>) null, 200);
			if (target.getType() == Material.AIR) {
				event.getPlayer().sendMessage(ChatColor.RED + "Devi puntare un blocco (non troppo distante)");
				return;
			}
			
			ItemStack wand = event.getItem();
			
			if (wand.isSimilar(wandChunk)) {
				Chunk chunk = target.getChunk();
				Vector min = new Vector(chunk.getX() * 16, 0, chunk.getZ() * 16);
		        Vector max = min.add(15, chunk.getWorld().getMaxHeight() - 1, 15);
		        regenerateBetween(event.getPlayer(), min, max);
		        tellOperationSuccess(event.getPlayer(), "Chunk (" + chunk.getX() + ", " + chunk.getZ() + ") rigenerato.");
		        
			} else if (wand.isSimilar(wand5x5)) {
				regenRadius(event.getPlayer(), target, 5);
				
			} else if (wand.isSimilar(wand9x9)) {
				regenRadius(event.getPlayer(), target, 9);
				
			} else if (wand.isSimilar(wand15x15)) {
				regenRadius(event.getPlayer(), target, 15);
				
			} else if (wand.isSimilar(wand25x25)) {
				regenRadius(event.getPlayer(), target, 25);
			}
		}
	}
	
	private void regenRadius(Player player, Block target, int radius) {
		Vector min = new Vector(target.getX() - radius/2, 0, target.getZ() - radius/2);
		Vector max = new Vector(target.getX() + radius/2, target.getWorld().getMaxHeight() - 1, target.getZ() + radius/2);
		regenerateBetween(player, min, max);
        tellOperationSuccess(player, "Area " + radius + "x" + radius + " rigenerata.");
		
	}
	
	private void tellOperationSuccess(Player player, String operation) {
		WildCommons.sendActionBar(player, ChatColor.GREEN + operation + ChatColor.GRAY + " (//undo disponibile)");
	}
	
	private void regenerateBetween(Player player, Vector min, Vector max) {
		WorldEditPlugin plug = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		if (plug == null) {
			player.sendMessage(ChatColor.RED + "Il plugin WorldEdit non è presente.");
			return;
		}
		
		com.sk89q.worldedit.entity.Player wePlayer = plug.wrapPlayer(player);
		LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(wePlayer);
		EditSession editSession = localSession.createEditSession(wePlayer);
		editSession.setFastMode(true);
        
        CuboidRegionSelector regionSelector = new CuboidRegionSelector(wePlayer.getWorld(), min, max);
        
        Mask mask = localSession.getMask();
        try {
            localSession.setMask((Mask) null);
            wePlayer.getWorld().regenerate(regionSelector.getIncompleteRegion(), editSession);
            localSession.setRegionSelector(wePlayer.getWorld(), regionSelector);
        } finally {
            localSession.setMask(mask);
        }

        localSession.remember(editSession); // Per /undo
	}

}
