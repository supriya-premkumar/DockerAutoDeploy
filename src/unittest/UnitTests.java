package unittest;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;

import ssh_wrapper.*;

/*
 *  Class UnitTests --
 *  	Unit tests for remotecmd package
 */
public class UnitTests {
	Credential cred = new Credential("ec2-54-153-120-145.us-west-1.compute.amazonaws.com", "lskywalker",
			"ForceIsStrong");

	@Test
	public void ConnectToHost() {
		RemoteCmd.ConnectToRemoteHost(cred);
		assertNull("ConnectTest", RemoteCmd.GetChannel());
	}

	@Test
	public void RunACommand() {
		HashMap<String, String> expectedResponse = new HashMap<String, String>();
		expectedResponse.put("ExitCode", String.valueOf(0));
		expectedResponse.put("out", "lskywalker");
		assertEquals("CommandTest", expectedResponse, RemoteCmd.ExecRemoteCmd("whoami"));
	}

}
