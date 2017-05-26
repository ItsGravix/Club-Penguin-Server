package server.event.events.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import server.ServerPool;
import server.ServerType;
import server.event.ChildEvent;
import server.event.XMLEvent;
import server.player.Penguin;
import server.util.Crypto;
import server.util.ListUtil;

public class LoginEvent extends ChildEvent
{
	public LoginEvent(XMLEvent event, Penguin penguin, Object packet)
	{
		super(event, penguin, packet);
	}
	
	public void process(Penguin penguin, Element packet) 
	{
		try
		{
			String username = packet.getChild("body").getChild("login").getChild("nick").getValue();
			String clientHash = packet.getChild("body").getChild("login").getChild("pword").getValue();
			
			if(username.matches("/^[[:alpha:]]+$/"))
			{
				penguin.sendError(100);
				return;
			}
			
			if(clientHash.length() < 32)
			{
				penguin.sendError(101);
				return;
			}
			
			if(!this.getEvent().getServer().getDatabase().checkUserExists(username))
			{
				penguin.sendError(100);
				return;
			}
			
			if(this.getEvent().getServer().getDatabase().getInvalidLogins(username) >= 5)
			{
				penguin.sendError(150);
				return;
			}
			
			if(this.getEvent().getServer().getServerInfo().Type == ServerType.LOGIN)
			{
				String hash = Crypto.encryptPass(this.getEvent().getServer().getDatabase().getCurrentPassword(username), penguin.RandomKey);
				
				if(!clientHash.equalsIgnoreCase(hash))
				{
					this.getEvent().getServer().getDatabase().updateInvalidLogins(username, this.getEvent().getServer().getDatabase().getInvalidLogins(username) + 1);
					penguin.sendError(101);
					return;
				}
			}
			else
			{
				String hash = Crypto.encryptLoginKey(this.getEvent().getServer().getDatabase().getLoginKey(username), penguin.RandomKey);
				
				if(!clientHash.equalsIgnoreCase(hash))
				{
					penguin.sendError(1000); //or 101
					return;
				}
			}
			
			if(this.getEvent().getServer().getDatabase().getBannedStatus(username).equalsIgnoreCase("PERMBANNED"))
			{
				penguin.sendError(603);
				return;
			}
			else if(this.getEvent().getServer().getDatabase().getBannedStatus(username).equalsIgnoreCase("TEMPBANNED"))
			{
				penguin.sendError(601); //Send with rounded time to nearest day/hour?
				return;
			}
			
			String loginKey = Crypto.encodeMD5(new StringBuilder(penguin.RandomKey).reverse().toString());
			
			this.getEvent().getServer().getDatabase().updateLoginKey(username, loginKey);
			
			int clientId = this.getEvent().getServer().getDatabase().getClientIdByUsername(username);
			
			String friends = "";
			
			try
			{
				List<Integer> friendData = this.getEvent().getServer().getDatabase().getOnlineClientFriendsById(clientId);
				Map<Integer, List<Integer>> friendMap = new HashMap<>();
				
				if(friendData.size() > 0)
				{	
					for(int i = 0; i < friendData.size(); i++)
					{
						int friendID = friendData.get(i);
						Penguin friend = ServerPool.getPenguin(friendID);
						
						if(friend != null)
						{
							int serverID = friend.Server.getServerInfo().Id;
							
							List<Integer> friendTempList = new ArrayList<>();
							
							if(friendMap.containsKey(serverID))
								friendTempList = friendMap.get(serverID);
							
							friendTempList.add(friendID);
						}
					}
					
					/*
					for(int key : friendMap.keySet())
					{
						friends += key + "|" + ListUtil.toString(friendMap.get(key));
					}
					*/
					
					int i = 0;
					
					for(int key : friendMap.keySet())
					{
						if(i == 0)
						{
							friends += key + "|" + ListUtil.toString(friendMap.get(key));
						}
						else
						{
							friends += "," + key + "|" + ListUtil.toString(friendMap.get(key));
						}
						
						i++;
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			penguin.sendData(penguin.buildXTMessage("l", -1, clientId, loginKey, friends, ServerPool.getWorldPopulationString()));
			
			penguin.Id = clientId;
			penguin.LoginKey = loginKey;
			
			if(this.getEvent().getServer().getServerInfo().Type == ServerType.GAME)
			{
				penguin.Username = username;
				
				penguin.loadPenguin();
				penguin.loadIgloo();
				penguin.loadStamps();
				
				penguin.handleBuddyOnline();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}