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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import wild.api.command.CommandFramework;
import wild.api.command.CommandFramework.Permission;

@Permission(QuickRegen.USE_PERM)
public class CommandHandler extends CommandFramework {

	public CommandHandler(JavaPlugin plugin, String label) {
		super(plugin, label);
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		PlayerInventory inventory = CommandValidate.getPlayerSender(sender).getInventory();
		
		ItemStack itemInHand = inventory.getItemInHand();
		
		if (itemInHand != null) {
			ItemStack newMode = null;
			
			if (itemInHand.isSimilar(QuickRegen.wandChunk)) {
				newMode = QuickRegen.wand5x5;
			} else if (itemInHand.isSimilar(QuickRegen.wand5x5)) {
				newMode = QuickRegen.wand9x9;
			} else if (itemInHand.isSimilar(QuickRegen.wand9x9)) {
				newMode = QuickRegen.wand15x15;
			} else if (itemInHand.isSimilar(QuickRegen.wand15x15)) {
				newMode = QuickRegen.wand25x25;
			} else if (itemInHand.isSimilar(QuickRegen.wand25x25)) {
				newMode = QuickRegen.wandChunk;
			}
			
			if (newMode != null) {
				inventory.setItemInHand(newMode);
				sender.sendMessage(ChatColor.GREEN + "Hai cambiato modalità! Digita di nuovo /" + label + " per cambiare ancora.");
				return;
			}
		}
		
		inventory.setItemInHand(QuickRegen.wandChunk);
		sender.sendMessage(ChatColor.GREEN + "Hai ricevuto la bacchetta! Digita /" + label + " per cambiare modalità.");
	}

}
