package server.events.game;

import server.event.XTEvent;
import server.player.Penguin;
import server.player.StaffRank;
import server.util.Values;

public class JoinServerEvent extends XTEvent
{
	public void process(Penguin penguin, String type, String[] args) 
	{
		if(type.equalsIgnoreCase("j#js"))
		{
			penguin.sendData(penguin.buildXTMessage("js", args[0], penguin.Id, Values.getBool(penguin.IsEPF), Values.getBool(penguin.Ranking == StaffRank.MODERATOR), 0));
		}
	}
}