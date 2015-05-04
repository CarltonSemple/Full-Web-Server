package helpers;

import dataModel.User;

public class Authenticator {
	/*
	 * Return true if the user credentials are correct.
	 * False otherwise
	 */
	public static boolean verifyCredentials(User user){
		for(User u : User.userList){
			if(user.name().equals(u.name())){
				if(user.password().equals(u.password())){
					return true;
				}
			}
		}
		return false;
	}
	
	public static int key = 1;	// just a basis for a more complex way of logging in, to be implemented later
}
