package com.darkblade12.simplealias.command.alias;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.Setting;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.hook.types.VaultHook;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "add", params = "<name> <setting> <value>", description = "Adds a value to a setting of an alias", permission = Permission.ADD_COMMAND, infiniteParams = true)
public final class AddCommand implements ICommand {
	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		String name = StringUtils.removeStart(params[0], "/");
		Alias a = SimpleAlias.getAliasManager().getAlias(name);
		if (a == null) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name doesn't exist!");
		} else {
			Setting s = Setting.fromName(params[1]);
			if (s == null) {
				sender.sendMessage(SimpleAlias.PREFIX + "§cA setting with this name/path doesn't exist!");
			} else {
				String path = s.getPath();
				String[] valueArray = (String[]) Arrays.copyOfRange(params, 2, params.length);
				String value = StringUtils.join(valueArray, " ");
				if (s == Setting.ENABLED_WORLDS) {
					String[] worlds = value.split(", ");
					Set<String> enabledWorlds = new HashSet<String>();
					Set<String> aliasEnabledWorlds = a.getEnabledWorlds();
					for (String world : worlds) {
						World w = Bukkit.getWorld(world);
						if (w == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because the world §4" + world + " §cdoesn't exist!");
							return;
						}
						String exactWorld = w.getName();
						if (aliasEnabledWorlds.contains(exactWorld)) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because the world §4" + exactWorld + " §cis already present!");
							return;
						}
						enabledWorlds.add(exactWorld);
					}
					aliasEnabledWorlds.addAll(enabledWorlds);
					value = StringUtils.join(enabledWorlds, ", ");
				} else if (s == Setting.EXECUTION_ORDER) {
					String[] actions = value.split(", ");
					List<String> executionOrder = new ArrayList<String>();
					for (String action : actions) {
						Action aliasAction = a.getAction(action);
						if (aliasAction == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because the action §4" + action + " §cdoesn't exist!");
							return;
						}
						executionOrder.add(aliasAction.getName());
					}
					a.getExecutionOrder().addAll(executionOrder);
					value = StringUtils.join(executionOrder, ", ");
				} else if (s == Setting.PERMISSION_GROUPS) {
					String[] groups = value.split(", ");
					Set<String> permissionGroups = new HashSet<String>();
					Set<String> aliasPermissionGroups = a.getPermissionGroups();
					VaultHook v = SimpleAlias.getVaultHook();
					boolean enabled = v.isEnabled() && v.hasPermissionGroupSupport();
					for (String group : groups) {
						String exactGroup = v.getExactGroupName(group);
						if (enabled && exactGroup == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because the group §4" + group + " §cdoesn't exist!");
							return;
						}
						String groupName = enabled ? exactGroup : group;
						if (aliasPermissionGroups.contains(groupName)) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because the group §4" + groupName + " §cis already present!");
							return;
						}
						permissionGroups.add(groupName);
					}
					aliasPermissionGroups.addAll(permissionGroups);
					value = StringUtils.join(permissionGroups, ", ");
				} else {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting doesn't support the addition of values!");
					return;
				}
				try {
					a.save();
					sender.sendMessage(SimpleAlias.PREFIX + "§aThe value §2" + value + " §awas added to the setting §e" + path + " §aof the alias §6" + name + "§a.");
				} catch (Exception e) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cFailed to save the alias! Cause: " + e.getMessage());
					if (Settings.isDebugEnabled()) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}