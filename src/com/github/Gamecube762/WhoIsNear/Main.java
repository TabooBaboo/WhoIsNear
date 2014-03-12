package com.github.Gamecube762.WhoIsNear;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{
	
	YamlConfiguration config;
	File configFile;
	
	int defaultRange;
	HashMap<String, Integer> groupRanges = new HashMap<String, Integer>();
	{//presets
		groupRanges.put("Helper", 100);
		groupRanges.put("VIP", 300);
		groupRanges.put("Moderator", 500);
		groupRanges.put("Admin", 1000);
	}
	
	
	@Override
	public void onEnable() {
		
		getDataFolder().mkdirs();
		configFile = new File(getDataFolder(), "config.yml");
		
		config = new YamlConfiguration();
		if (configFile.exists()) config = YamlConfiguration.loadConfiguration(configFile);
		
		updateConfig();
		loadConfig();
	}
	
	@Override
	public void onDisable() {
		groupRanges.clear();
	}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if ( !(sender instanceof Player) ) {sender.sendMessage("Needs to be a player sendong the command!"); return true;}
    	
    	Player p = (Player) sender;
    	List<Player> players = new ArrayList<Player>(); 
    	int range = getRange(p);
    	
    	for (Entity e : p.getNearbyEntities(range, range, range)) if (e instanceof Player)  players.add( (Player) e );
    		
    	if (players.size() < 1) { sender.sendMessage("No players within " + range + " blocks"); return true; } 
    	
    	sender.sendMessage("Players within " + defaultRange + "blocks of you:");
    	for (Player a : players) {
    		boolean hidden = a.hasPermission("WhoIsNear.hidden"),
    				bypass = p.hasPermission("WhoIsNear.hidden.bypass");
    		
    		
    		if (!hidden | bypass) {
    			int i = (int) a.getLocation().distance( p.getLocation() );
    			sender.sendMessage(ChatColor.YELLOW + a.getDisplayName() + ChatColor.BLUE +" (" + ChatColor.RESET + i + ChatColor.BLUE + ")" );
    			
    			if (a.hasPermission("WhoIsNear.found.warn") ) a.playSound(a.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
    			if (a.hasPermission("WhoIsNear.found.tell") ) a.sendMessage("Someone Found you with a radar!");
    				
    		}
    	}
    	
    	return true;
    }
    
    public int getRange(Player p){
    	for (String s : groupRanges.keySet())
    		if ( p.hasPermission("WhoIsNear.group." + s) )
    			return groupRanges.get(s);
    	
    	return defaultRange;
    }

    private void updateConfig() {
    	config.options().header("WhoIsNear config.yml | Plugin by Gamecube762\n"+
    			"DO NOT use TAB when editing this file, use SPACES instead, otherwise the plugin will hate you...\n" +
    			"Settings.DefaultRange: 500 is the default range for the radar\n" +
    			"Settings.GroupRanges: <GroupName : Range> is the ranges for the groups\n" +
    			"\nGroups are checked by \"If the player has permission WhoIsNear.group.<group>\"\n" +
    			"Default groups are examples for how to format the config and can be replaced."
    			);
    	
    	if (config.contains("Settings.Range")) {config.set("Settings.Range", null);}
    	if (!config.contains("Settings.DefaultRange")) config.set("Settings.DefaultRange", 500);
    	if (!config.contains("Settings.GroupRanges")) config.createSection("Settings.GroupRanges", groupRanges);
    	
    	
    	try {config.save(configFile);} catch (IOException e) {getLogger().severe("Could not save config.yml!!");}
    }
    
    private void loadConfig() {
    	defaultRange = config.getInt("Settings.DefaultRange");
    	
    	
    	//Getting list of groups
    	groupRanges.clear();//clean it so we don't get unneeded groups
    	
    	Map<String, Object> a = config.getConfigurationSection("Settings.GroupRanges").getValues(false);
    	String b = "config.yml | Settings.GroupRanges \n Values needs to be numbers:";
    	
    	for (String s : a.keySet())
    		if (a.get(s) instanceof Integer) addgroup(s, (Integer) a.get(s) );
    		else b = b + "\n" + s + ": " + a.get(s).toString();
    	
    	if (!b.equals("config.yml | Settings.GroupRanges \n Values needs to be numbers:")) getLogger().severe(b);
    }
    
    private void addgroup(String s, int i){
    	groupRanges.put(s, i);
    	
    	Permission groupPerm = new Permission("WhoIsNear.group." + s, PermissionDefault.FALSE);
    	getServer().getPluginManager().addPermission(groupPerm);//adds permission node defaulting to false so we can correctly check groups later
    }
}
