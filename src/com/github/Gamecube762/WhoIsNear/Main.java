package com.github.Gamecube762.WhoIsNear;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{
	
	YamlConfiguration config;
	File configFile;
	
	int Range;
	
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if ( !(sender instanceof Player) ) {sender.sendMessage("Needs to be a player sendong the command!"); return true;}
    	
    	Player p = (Player) sender;
    	List<Player> players = new ArrayList<Player>(); 
    	
    	for (Entity e : p.getNearbyEntities(Range, Range, Range)) if (e instanceof Player)  players.add( (Player) e );
    		
    	if (players.size() < 1) {sender.sendMessage("No players within " + Range + " blocks"); return true;} 
    	
    	sender.sendMessage("Players within " + Range + "blocks of you:");
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

    private void updateConfig() {
    	if (!config.contains("Settings.Range")) config.set("Settings.Range", 500);
    	
    	try {config.save(configFile);} catch (IOException e) {getLogger().severe("Could not save config.yml!!");}
    }
    
    private void loadConfig() {
    	Range = config.getInt("Settings.Range");
    }
}
