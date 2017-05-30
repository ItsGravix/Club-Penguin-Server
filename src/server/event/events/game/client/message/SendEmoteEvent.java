package server.event.events.game.client.message;

import server.event.XTEvent;
import server.player.Penguin;

public class SendEmoteEvent extends XTEvent
{
	public SendEmoteEvent()
	{
		super("u#se");
	}
	
	public void process(Penguin penguin, String[] args) 
	{
		penguin.sendEmote(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
	}
}