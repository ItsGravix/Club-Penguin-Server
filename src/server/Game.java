package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import server.commands.AddInventoryCommand;
import server.commands.StopServerCommand;
import server.events.game.client.HeartbeatEvent;
import server.events.game.client.JoinRoomEvent;
import server.events.game.client.JoinServerEvent;
import server.events.game.friends.BuddyAcceptEvent;
import server.events.game.friends.BuddyFindEvent;
import server.events.game.friends.BuddyRequestEvent;
import server.events.game.friends.GetBuddiesEvent;
import server.events.game.friends.RemoveBuddyEvent;
import server.events.game.inventory.AddItemEvent;
import server.events.game.inventory.GetInventoryEvent;
import server.events.game.mail.MailStartEvent;
import server.events.game.message.SendEmoteEvent;
import server.events.game.message.SendJokeEvent;
import server.events.game.message.SendMessageEvent;
import server.events.game.message.SendSafeMessageEvent;
import server.events.game.player.GetPlayerEvent;
import server.events.game.player.SendActionEvent;
import server.events.game.player.SendFrameEvent;
import server.events.game.player.SetPositionEvent;
import server.events.game.player.SnowballEvent;
import server.events.game.player.UpdatePlayerEvent;
import server.player.Penguin;
import server.util.Logger;

public class Game extends Server
{
	protected List<Penguin> IglooMap;
	
	public Game(ServerInfo info)
	{
		super(info);
		
		this.IglooMap = new ArrayList<>();
		
		save();
	}
	
	public List<Penguin> getIglooMap()
	{
		return this.IglooMap;
	}

	public void init() throws Exception
	{
		Logger.info("Starting Club Penguin [Game] Server...", this);
		Logger.info("Written in Java by Fil_", this);
	
		this.ServerSocket = new ServerSocket(this.ServerInfo.Port);
		
		final Server scope = this;
		
		this.ServerThread = new Thread()
		{
			public void run()
			{
				Logger.info("Server Started - Waiting for Clients to connect!", scope);
				
				while(true)
				{
					try
					{
						//Freezes Thread until a Client has established a Connection!
						Socket socket = ServerSocket.accept();

						Threads.submit(new Runnable()
						{
							public void run()
							{
								onConnection(socket);
							}
						});
					}
					catch(Exception e)
					{
						Logger.error("There was an error while accepting a Client connection: " + e.getMessage(), scope);
					}
				}
			}
		};
		
		this.ServerThread.start();
	
		this.Database.updateServer(this.ServerInfo);
	}

	public void onDisconnect(Penguin client)
	{
		Logger.info("Client Disconnected - " + client.getSocket().getInetAddress().getHostAddress() + ":" + client.getSocket().getPort(), this);
		
		this.IglooMap.remove(client);
	
		client.handleBuddyOffline();
		client.removePlayerFromRoom();	
		
		this.Clients.remove(client);
		
		client = null;
		
		save();
	}
	
	@SuppressWarnings("deprecation")
	public void stop() throws Exception
	{
		clear();
		
		for(Penguin client : this.Clients)
		{
			client.kickStop();
		}
		
		this.Clients.clear();
		
		this.Threads.shutdownNow();
		
		if(this.ServerThread != null)
			this.ServerThread.stop();
	}
	
	public void registerCommands()
	{
		this.CommandManager.registerCommand(new StopServerCommand());
		this.CommandManager.registerCommand(new AddInventoryCommand());
	}

	public void registerEvents() 
	{
		this.EventManager.registerEvent(new JoinServerEvent());
		this.EventManager.registerEvent(new GetInventoryEvent());
		this.EventManager.registerEvent(new JoinRoomEvent());
		this.EventManager.registerEvent(new SetPositionEvent());
		this.EventManager.registerEvent(new HeartbeatEvent());
		this.EventManager.registerEvent(new UpdatePlayerEvent());
		this.EventManager.registerEvent(new GetPlayerEvent());
		this.EventManager.registerEvent(new AddItemEvent());
		this.EventManager.registerEvent(new SnowballEvent());
		this.EventManager.registerEvent(new SendFrameEvent());
		this.EventManager.registerEvent(new SendActionEvent());
		this.EventManager.registerEvent(new SendMessageEvent());
		this.EventManager.registerEvent(new SendJokeEvent());
		this.EventManager.registerEvent(new SendSafeMessageEvent());
		this.EventManager.registerEvent(new SendEmoteEvent());
		this.EventManager.registerEvent(new GetBuddiesEvent());
		this.EventManager.registerEvent(new BuddyRequestEvent());
		this.EventManager.registerEvent(new BuddyAcceptEvent());
		this.EventManager.registerEvent(new RemoveBuddyEvent());
		this.EventManager.registerEvent(new BuddyFindEvent());
		this.EventManager.registerEvent(new MailStartEvent());
	}
}