package auto_deploy;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import ssh_wrapper.Command;
import ssh_wrapper.Credential;
import ssh_wrapper.RemoteCmd;

/**
 * All workloads are organized as a series of commands to be run on the remote
 * server
 * 
 * Workload: CmdList: --------- ---------- SpinUp() docker run -d nginx docker
 * run -d redis
 * 
 * Stats() docker ps top -b -n 1 vmstat -s df -h docker stats --no-stream
 * 
 * Logs() docker logs containerID1 docker logs containerID2
 * 
 * The AutoDeploy implements the following workloads The output will be logged
 * to the Console
 * 
 * 
 * @version 1.0
 *
 */
public class AutoDeploy {
	private Credential cred;
	private List<String> containerID;
	private List<String> statsList;
	private List<String> logList;
	private List<Command> cmdList;

	private final static Logger Log = Logger.getLogger(RemoteCmd.class.getName());

	public AutoDeploy(Credential cred) {
		this.cred = cred;
	}

	/*
	 * Worker --
	 * 
	 * This method implements a specific workload. It generates the list of
	 * commands, executes them on the remote server and prints it to stdout
	 * 
	 * @param workloadType String | The type of workload. Possible workloads are
	 * SpinUp, Stats and Logs
	 * 
	 * @return None
	 */
	public void Worker(String workloadType) {

		// Set the workload type to populate the correct cmdList
		Workload work = new Workload(workloadType);
		if (workloadType.equals("Logs")) {
			this.cmdList = work.GenerateWorkload(this.containerID);
		} else {
			this.cmdList = work.GenerateWorkload();
		}

		// Execute list of commands for the workload
		HashMap<Integer, Object> responseMap = ExecuteCommandStream(this.cmdList);

		// Output the results to stdout
		if (workloadType.equals("SpinUp")) {
			this.containerID = ProcessResponses(responseMap, workloadType, this.cmdList);
			System.out.println("=============== SPUN UP CONTAINER IDs =================");
			for (String item : this.containerID) {
				System.out.println(item);
			}
		} else if (workloadType.equals("Stats")) {
			this.statsList = ProcessResponses(responseMap, workloadType, this.cmdList);
			System.out.println("=============== STATS OUTPUT =================");
			for (String item : this.statsList) {
				System.out.println(item + "\n");

			}
		} else if (workloadType.equals("Logs")) {
			this.logList = ProcessResponses(responseMap, workloadType, this.cmdList);
			System.out.println("=============== LOG OUTPUT =================");
			for (String item : this.logList) {
				System.out.println(item + "\n");
			}
		}

	}

	/*
	 * ExecuteCommandStream --
	 * 
	 * This method executes the list of commands. It uses ssh exec provided by
	 * ExecRemoteCmd. The response for each command is stored in a separate
	 * HashMap indexed by hashing the command object.
	 * 
	 * @param cmdList List<Command> | List of command objects
	 * 
	 * @return response HashMap<Integer, Object> | HashMap of command objects
	 * whose value is HashMap<String, String>
	 */
	private HashMap<Integer, Object> ExecuteCommandStream(List<Command> cmdList) {
		HashMap<Integer, Object> responseMap = new HashMap<Integer, Object>();

		// Iterate over each command. As an optimization we could use a single
		// session for a given workload
		for (Command cmd : cmdList) {
			RemoteCmd.ConnectToRemoteHost(this.cred);
			HashMap<String, String> resp = RemoteCmd.ExecRemoteCmd(cmd.GetCmd());

			// Shout out to Evan for teaching me this!
			responseMap.put(cmd.hashCode(), resp);
			RemoteCmd.CleanUp();
			Log.log(Level.INFO, "Command: " + cmd.GetCmd() + " Success!");
		}

		return responseMap;
	}

	/*
	 * ProcessResponses --
	 * 
	 * This method processes the response for each command depending on the
	 * workload type.
	 * 
	 * @param responseMap HashMap<Integer, Object> | HashMap of all the
	 * responses for the given workload
	 * 
	 * @param workloadType String | Type of the workload
	 * 
	 * @param cmdList List<Command> | List of command objects
	 * 
	 * @return List<String> | List of processed responses
	 */
	@SuppressWarnings("unchecked")
	private List<String> ProcessResponses(HashMap<Integer, Object> responseMap, String workloadType,
			List<Command> cmdList) {
		List<String> processedResp = new ArrayList<String>();

		if (workloadType.equals("SpinUp")) {
			for (Command cmd : cmdList) {
				HashMap<String, String> res = new HashMap<String, String>();
				res = (HashMap<String, String>) responseMap.get(cmd.hashCode());
				processedResp.add(res.get("out"));
			}
			// For other workloads we also need the command to make it easier
			// for the user to consume the output
		} else if (workloadType.equals("Stats") || workloadType.equals("Logs")) {
			for (Command cmd : cmdList) {
				HashMap<String, String> res = new HashMap<String, String>();
				res = (HashMap<String, String>) responseMap.get(cmd.hashCode());
				processedResp.add(cmd.GetCmd());
				processedResp.add(res.get("out"));
			}
		}

		return processedResp;

	}

}

class Workload {
	/*
	 * Workload Class: Implements the generateCommand method for different type
	 * of workloads
	 */
	private List<Command> cmdList;
	private String workloadType;
	private final static Logger Log = Logger.getLogger(RemoteCmd.class.getName());

	public Workload(String workloadType) {
		this.cmdList = new ArrayList<Command>();
		this.workloadType = workloadType;
	}

	/*
	 * GenerateWorkload --
	 * 
	 * This method generates the list of commands for SpinUp and Stats workloads
	 */
	public List<Command> GenerateWorkload() {
		if (this.workloadType.equals("SpinUp")) {
			this.cmdList.clear();
			this.cmdList.add(new Command("docker", "docker run -d nginx"));
			this.cmdList.add(new Command("redis", "docker run -d redis"));

		} else if (this.workloadType.equals("Stats")) {
			this.cmdList.clear();
			this.cmdList.add(new Command("docker", "docker ps"));
			this.cmdList.add(new Command("cpu", "top -b -n 1"));
			this.cmdList.add(new Command("mem", "vmstat -s"));
			this.cmdList.add(new Command("hdd", "df -h"));
			this.cmdList.add(new Command("dockerStat", "docker stats --no-stream"));

		} else if (this.workloadType.equals("Logs")) {

		}
		return this.cmdList;

	}

	/*
	 * GenerateWorkload --
	 * 
	 * Overloaded function to generate workload for Logs.
	 * 
	 */
	public List<Command> GenerateWorkload(List<String> containerID) {
		this.cmdList.clear();
		Log.log(Level.INFO, "Generating Commands for Log Phase");
		for (String id : containerID) {
			String cmd = "docker logs " + id;
			this.cmdList.add(new Command("dockerLogs", cmd));
		}
		return this.cmdList;
	}
}
