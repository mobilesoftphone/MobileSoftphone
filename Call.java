package features;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;
import javax.microedition.sip.SipClientConnection;
import javax.microedition.sip.SipClientConnectionListener;
import javax.microedition.sip.SipConnectionNotifier;

public class Call extends List implements CommandListener {
	private Display display;
	private Command back, select;
	private List mainMenu;
	private Player player;
	private byte[] recordedSoundArray = null;
	private Alert alert = null;
    private SipClientConnection sc = null;
    private final String ip = "127.0.0.1";
    private final String port = "3000";
	
	public Call(String title, int listType, Display display, List mainMenu) {
		super(title, listType);
		
		this.display = display;
		this.mainMenu = mainMenu;
		
		select = new Command("Select", Command.OK, 1);
		back = new Command("Back", Command.BACK, 2);
		
		append("View Friends", null);
		append("Dial Number", null);
		
		addCommand(select);
		addCommand(back);
		setCommandListener(this);
	}

	public void commandAction(Command arg0, Displayable arg1) {
		if(arg0 == back) {
			display.setCurrent(mainMenu);
		} else if(arg0 == select) {
			if(getSelectedIndex() == 1) { /* Dial Number feature */
				if(alert == null) {
					alert = new Alert("Dial Number");
					alert.setType(AlertType.INFO);
				}
				alert.setString("Recording...");
				setDisplay(alert, this);
				new SendSIPThread().start();
				alert.setString("Done...");
				setDisplay(alert, this);
			} else if(getSelectedIndex() == 0) { /* View Friends */
				
			}
			
		}
	}
	
	public void setDisplay(Alert a, Displayable d) {
		if(a == null) {
			display.setCurrent(d);
		} else if(d == null) {
			display.setCurrent(a);
		} else {
			display.setCurrent(a, d);
		}
	}
	
	public class SendSIPThread extends Thread implements SipClientConnectionListener {
		public void run() {
			try {
				player = Manager.createPlayer("capture://audio");
				player.realize();
				RecordControl rc = (RecordControl)player.getControl("RecordControl");
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				rc.setRecordStream(output);   
				rc.startRecord();
				player.start();
				Thread.currentThread().sleep(2000);
				rc.commit();
				recordedSoundArray = output.toByteArray();
				player.close();
	                
				sc = (SipClientConnection)Connector.open("sip:" + ip + ":" + port);
				sc.setListener(this);

				sc.initRequest("MESSAGE", null);
				sc.setHeader("From", "sip:" + ip);
				sc.setHeader("Subject", "SIP Testing");
				sc.setHeader("Content-Type", "audio/x-wav");
				sc.setHeader("Content-Length", Integer.toString(recordedSoundArray.length));

				OutputStream os = sc.openContentOutputStream();
				os.write(recordedSoundArray);
				os.flush();
				os.close(); // close the stream and send the message
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MediaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException iae) {
				iae.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public void notifyResponse(SipClientConnection arg0) {
			 try {
	                sc.receive(0);
	                System.out.println("Response received: " + sc.getStatusCode() + " " +
	                    sc.getReasonPhrase());
	                sc.close();
	            } catch (Exception ex) {
	                ex.printStackTrace();
	            }
		}
	}

}
