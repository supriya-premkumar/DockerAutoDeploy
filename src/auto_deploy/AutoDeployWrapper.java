package auto_deploy;

import ssh_wrapper.Credential;

/**
 * The AutoDeployWrapper implements an application that spins up docker
 * containers, collects stats and the logs from the containers on a remote
 * machine. There will be one AutoDeploy instance per machine.
 * 
 * Dependencies: JSch Junit hamcrest
 * 
 * @version 1.0
 *
 */
public class AutoDeployWrapper {

	public static void main(String[] args) {
		// Static credentials for an AWS EC2 Instance
		Credential cred = new Credential("ec2-54-193-97-53.us-west-1.compute.amazonaws.com", "lskywalker",
				"ForceIsStrong");
		AutoDeploy autoDep = new AutoDeploy(cred);

		// Call stack
		autoDep.Worker("SpinUp");
		autoDep.Worker("Stats");
		autoDep.Worker("Logs");

	}

}