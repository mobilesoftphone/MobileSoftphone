package features;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

public class CallLogs extends List implements CommandListener {
	private Display display;
	private Command back;
	private List mainMenu;
	
	public CallLogs(String title, int listType, Display display, List mainMenu) {
		super(title, listType);
		
		this.display = display;
		this.mainMenu = mainMenu;
		
		back = new Command("Back", Command.BACK, 2);
		
		append("Missed Calls", null);
		append("Received Calls", null);
		append("Dialled Numbers", null);
		append("Call Duration", null);	
		
		addCommand(back);
		setCommandListener(this);
	}
	
	public void commandAction(Command arg0, Displayable arg1) {
		if(arg0 == back) {
			display.setCurrent(mainMenu);
		}
	}

}
