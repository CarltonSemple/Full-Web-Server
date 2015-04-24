package dataModel;
import java.io.Serializable;

import server.ServerMain;


// serializable example: http://crunchify.com/how-to-serialize-deserialize-list-of-objects-in-java-java-serialization-example/

/*
 * 
 */
public class User implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6356071710821951135L;

	private static int usernum = 0;	// NOT SERIALIZABLE . number that is given to a new user
	
	public static java.util.List<User> userList;	// publicly accessible list of users
	
	private int id;
	private String username;
	private String password;
	
	public User(String u, String p){
		id = usernum++;
		username = u;
		password = p;
	}
	
	public String name(){
		return username;
	}
	
	public boolean changeName(String newname, String pass){
		if(password.equals(pass)){
			username = newname;
			return true;
		}
		
		return false;
	}
	
	/*
	 * Iterate through the public static user list, printing them out
	 */
	public static void printUsers(){
		for(User u : User.userList){
			ServerMain.logger.info(u.toString());
		}
	}
	
	@Override
	public String toString(){
		return "Username: " + username + " Password: " + password;
	}
}
