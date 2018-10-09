package ssh_wrapper;

/*
 * Class Command --
 * 		
 * 		Definition for a command object
 * 	
 */
public class Command {
	private String name;
	private String cmd;

	public Command(String name, String cmd) {
		this.SetName(name);
		this.SetCmd(cmd);
	}

	public String GetName() {
		return name;
	}

	public void SetName(String name) {
		this.name = name;
	}

	public String GetCmd() {
		return cmd;
	}

	public void SetCmd(String cmd) {
		this.cmd = cmd;
	}
}