package server.event.events.client.game.ignore;

import server.event.XTEvent;
import server.player.Penguin;

public class AddIgnoredEvent extends XTEvent
{
	public AddIgnoredEvent() 
	{
		super("n#an");
	}

	public void process(Penguin penguin, String[] args) 
	{
		penguin.ignorePlayer(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
	}
}