package ssh_wrapper;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.*;
import java.util.logging.Logger;
import com.jcraft.jsch.*;

public class RemoteCmd {
	/*
	 * RemoteCmd Class --
	 * 
	 * This class implements the stand alone model to run a command on a remote
	 * server through ssh exec. This uses JSch library by Atsuhiko Yamanaka,
	 * JCraft,Inc.
	 * 
	 */
	private static Channel channel;
	private static Session session;
	private final static Logger Log = Logger.getLogger(RemoteCmd.class.getName());

	/*
	 * ConnectToRemoteHost --
	 * 
	 * This method connects to the remote host specified in credentials object
	 * via ssh. We set the Channel in this method which will be reused later.
	 * 
	 * @param cred Credential | Crediential Object
	 * 
	 * @return None
	 * 
	 */
	public static void ConnectToRemoteHost(Credential cred) {
		try {
			JSch jsch = new JSch();
			RemoteCmd.session = jsch.getSession(cred.getUser(), cred.url, 22);
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			RemoteCmd.session.setConfig(config);
			RemoteCmd.session.setPassword(cred.getPass());
			Log.log(Level.INFO, "Connecting to the server...");
			RemoteCmd.session.connect(30000);
			Log.log(Level.INFO, "Connection Successful!");
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Failed to connect to host: " + cred.toString(), e.toString());
		}

	}

	/*
	 * ExecRemoteCmd --
	 * 
	 * This method executes a single ssh command and returns the response as a
	 * HashMap with exit code and stdout as values
	 * 
	 * @param cmd String | A single command to run
	 * 
	 * @return response HashMap<String, String> | Returns a HashMap
	 * {"ExitCode":<exitCode>, "out":<stdout>}
	 */
	public static HashMap<String, String> ExecRemoteCmd(String cmd) {
		HashMap<String, String> response = new HashMap<String, String>();
		StringWriter writer = new StringWriter();

		try {
			RemoteCmd.channel = RemoteCmd.session.openChannel("exec");
			((ChannelExec) RemoteCmd.channel).setCommand(cmd);
			RemoteCmd.channel.setInputStream(null);
			((ChannelExec) RemoteCmd.channel).setErrStream(System.err);
			InputStream input = RemoteCmd.channel.getInputStream();
			RemoteCmd.channel.connect();

			byte[] temp = new byte[1024];

			// Code to process stdout in chunks
			while (true) {
				while (input.available() > 0) {
					int i = input.read(temp, 0, 1024);
					if (i < 0) {
						break;
					}
					writer.append(new String(temp, 0, i));
				}
				if (RemoteCmd.channel.isClosed()) {
					if (input.available() > 0) {
						continue;
					}
					response.put("out", writer.toString().trim());
					response.put("ExitCode", String.valueOf(RemoteCmd.channel.getExitStatus()));
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					Log.log(Level.SEVERE, e.toString());
				}
			}
		} catch (Exception e) {
			Log.log(Level.SEVERE, "Failed to run the commmand: " + cmd, e);
		}
		return response;

	}

	/*
	 * CleanUp --
	 * 
	 * Tidies up the resources
	 */
	public static void CleanUp() {
		Log.log(Level.FINE, "Cleaning up session and channel handlers");
		RemoteCmd.channel.disconnect();
		RemoteCmd.session.disconnect();
	}

	/*
	 * getChannel -- Getter function for getChannel
	 */
	public static Channel GetChannel() {
		return RemoteCmd.channel;
	}

}