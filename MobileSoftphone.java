import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.sip.SipConnectionNotifier;
import javax.microedition.sip.SipException;
import javax.microedition.sip.SipServerConnection;

import features.Call;
import features.CallLogs;
import features.Settings;
import features.Voicemail;

public class MobileSoftphone extends MIDlet implements CommandListener {

	private Display display;
	
	/* Login Menu Elements */
	private Form loginMenu;
	private TextField username, password;
	private Command signIn, forgot;
	
	/* Main Menu Elements */
	private List mainMenu;
	private Command select;
	
	/* Feature's objects */
	private Call call = null;
	private CallLogs logs = null;
	private Voicemail voiceMail = null;
	private Settings settings = null;
	
	/* Voicemail feature objects */
    private SipConnectionNotifier scn = null;
    private SipServerConnection ssc = null;
    private final String port = "3000";
    private boolean vmReceived = false;
    private byte[] voicemail;
	
	public void generateLoginMenu() {
		/* Generate Login Menu */
		loginMenu = new Form("Welcome to Mobile Softphone!");
		
		username = new TextField("Username: ", "", 7, TextField.ANY);
		password = new TextField("Password: ", "", 7, TextField.PASSWORD);		
		signIn = new Command("Sign In", Command.OK, 1);
		forgot = new Command("Forgot?", Command.HELP, 2);
		
		loginMenu.append(username);
		loginMenu.append(password);
		loginMenu.addCommand(signIn);
		loginMenu.addCommand(forgot);
		loginMenu.setCommandListener(this);
		
		setDisplay(loginMenu);
	}
	
	public void generateMainMenu() {
		/* Generate Main Menu */
		mainMenu = new List("Hi " + username.getString() + "!", List.IMPLICIT);
		
		select = new Command("Select", Command.OK, 1);
		
		mainMenu.append("Call", null);
		mainMenu.append("Voicemail", null);
		mainMenu.append("Call Logs", null);
		mainMenu.append("Settings", null);
		
		mainMenu.addCommand(select);
		mainMenu.setCommandListener(this);
		
		setDisplay(mainMenu);
	}
	
	public void setDisplay(Displayable d) {
		display.setCurrent(d);
	}
	
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// TODO Auto-generated method stub
		
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub
		
	}

	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);		
		generateLoginMenu();
		new ReceiveThread().start();
	}

	public void commandAction(Command arg0, Displayable arg1) {
		/* Login Menu commands */
		if (arg0 == signIn) {
			/* Show Main Menu and delete login menu */
			generateMainMenu();
			loginMenu.deleteAll();
			loginMenu = null;
		} else if(arg0 == forgot) {
			Alert alert = new Alert("Forgot Passowrd?");
			alert.setString("Think again...");
			alert.setType(AlertType.INFO);
			setDisplay(alert);
		} 
		
		/* Main Menu commands */
		else if(arg0 == select) {
			if(mainMenu.getSelectedIndex() == 0) { /* Call feature */
				if(call == null) {
					call = new Call("Make a call...", List.IMPLICIT, display, mainMenu);
				}
				setDisplay(call);
			} else if(mainMenu.getSelectedIndex() == 1) { /* Voicemail feature */
				if(vmReceived == false) {
					Alert alert = new Alert("Voicemail");
					alert.setString("0 new voicemail!");
					alert.setType(AlertType.CONFIRMATION);
					setDisplay(alert);
				} else {
					voiceMail = new Voicemail("Playing new voicemail", voicemail);
					setDisplay(voiceMail);
				}
			} else if(mainMenu.getSelectedIndex() == 2) { /* Call Logs feature */
				if(logs == null) {
					logs = new CallLogs("Call Logs", List.IMPLICIT, display, mainMenu);
				}
				setDisplay(logs);
			} else if(mainMenu.getSelectedIndex() == 3) { /* Settings feature */
				if(settings == null) {
					settings = new Settings("Coming soon!");
				}
				setDisplay(settings);
			}
				
		}
	}
	
	public class ReceiveThread extends Thread {
		private byte[] buffer = new byte[0xFF];
        public void run() {
            try {
                scn = (SipConnectionNotifier)Connector.open("sip:" + port);

                // block and wait for incoming request.
                // SipServerConnection is established and returned
                // when a new request is received.
                ssc = scn.acceptAndOpen();

                if (ssc.getMethod().equals("MESSAGE")) {
                    String contentType = ssc.getHeader("Content-Type");

                    if ((contentType != null) && contentType.equals("audio/x-wav")) {
                    	InputStream is = ssc.openContentInputStream();
                        int bytesRead;
                        String msg = new String("");
                        
                        while ((bytesRead = is.read(buffer)) != -1) {
                            msg += new String(buffer, 0, bytesRead);
                        }

                        System.out.println("Body: \"" + msg + "\"\n\n");

                        while ((bytesRead = is.read(buffer)) != -1) {
                            msg += new String(buffer, 0, bytesRead);
                        }
                        
                        voicemail = new byte[msg.length()];
                        System.out.println(is.read(voicemail));
                        
                        vmReceived = true;
                        System.out.println(msg.length() + " " + voicemail.length);
                    }

                    // initialize SIP 200 OK and send it back
                    ssc.initResponse(200);
                    ssc.send();
                }

                ssc.close();
            } catch (Exception ex) {
                // IOException
                // InterruptedIOException
                // SecurityException
                // SipException
                ex.printStackTrace();
            }
        }
    }

}
