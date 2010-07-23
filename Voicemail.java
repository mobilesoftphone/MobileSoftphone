package features;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.sip.SipServerConnection;


public class Voicemail extends Alert {
	private Player p2;
	public Voicemail(String title, byte[] vm) {
		super(title);
		
		try {
			p2 = Manager.createPlayer(new ByteArrayInputStream(vm),"audio/x-wav");
        	p2.realize();
        	p2.prefetch();
        	p2.start();
			Thread.currentThread().sleep(2000);
			p2.close();
		} catch (MediaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
