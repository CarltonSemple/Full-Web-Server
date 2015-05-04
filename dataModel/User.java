package dataModel;
import java.io.Serializable;
import java.util.logging.Level;

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
	private String authority;	// none, or admin
	
	public User(String u, String p, String auth){
		id = usernum++;
		username = u;
		password = p;
		authority = auth;
	}
	
	public String name(){
		return username;
	}
	
	public String password(){
		return password;
	}
	
	public String Authority(){
		return authority;
	}
	
	public boolean changeName(String newname, String pass){
		if(password.equals(pass)){
			username = newname;
			return true;
		}
		
		return false;
	}
	
	/**
	 * Add a user if a user with that name doesn't already exist
	 * @param user
	 * @return
	 */
	public static int AddUser(User user){
		for(User u : User.userList){
			if(u.name().equals(user.name())){
				ServerMain.logger.log(Level.WARNING, "user already exists");
				System.out.println("user already exists");
				return 0;
			}
		}
		
		User.userList.add(user);	// add the new unique user
		ServerMain.logger.log(Level.FINE, user.toString() + " added to user database");
		System.out.println(user.toString() + " added to user database");
		return 1;
	}
	
	public static String getAuthority(User user){
		for(User u : User.userList){
			if(u.name().equals(user.name())){
				user.authority = u.Authority();
				return user.authority;
			}
		}
		return "";
	}
	
	/*
	 * Iterate through the public static user list, printing them out
	 */
	public static void printUsers(){
		for(User u : User.userList){
			ServerMain.logger.log(Level.FINE, u.toString());
			System.out.println(u.toString());
		}
	}
	
	@Override
	public String toString(){
		return "Username: " + username + " Password: " + password + " Authorization: " + authority;
	}
}
