package helpers;

import dataModel.User;

public class Authenticator {
	/*
	 * Return true if the user credentials are correct.
	 * An admin == 2, regular user == 1
	 * False otherwise
	 */
	public static int verifyCredentials(User user){
		for(User u : User.userList){
			if(user.name().equals(u.name())){
				if(user.password().equals(u.password())){
					if(user.Authority().equals("admin"))
						return 2;
					else
						return 1;
				}
			}
		}
		return 0;
	}	
}
