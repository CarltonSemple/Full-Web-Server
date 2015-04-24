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
}
