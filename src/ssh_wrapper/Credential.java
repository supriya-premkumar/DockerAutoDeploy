package ssh_wrapper;

public class Credential {
	public String url;
	private String user;
	private String pass;
	
	public Credential(String url, String user, String pass){
		this.url = url;
		this.setUser(user);
		this.setPass(pass);
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public String getPass() {
		return this.pass;
	}

	public void setPass(String user) {
		this.pass = user;
	}
	
	public String toString() {
		return "Url: " + url + "\t User: " + this.getUser();
	}
	

}