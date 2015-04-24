package helpers;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import server.ServerMain;
import dataModel.*;

public class FileAccess {
	/*
	 * Load the users from the specified file via deserialization.
	 * Throws IOException if loading fails.
	 */
	@SuppressWarnings("unchecked")
	public static List<User> loadUsers(String filePath){
		List<User> returnedList = new ArrayList<User>();
		FileInputStream fin = null;
		ObjectInputStream oin = null;
		try{
			fin = new FileInputStream(filePath);
			oin = new ObjectInputStream(fin);
			returnedList = (ArrayList<User>)oin.readObject();	// deserialize
		}catch(FileNotFoundException e){
			ServerMain.logger.info("user file not found");
		}catch(IOException e){
			ServerMain.logger.info("IO exception reading user file");
		}catch(Exception e){
			ServerMain.logger.info("error deserializing user file");
		}finally{
			// Always close streams
			try{
			if(oin != null)
				oin.close();
			if(fin != null)
				fin.close();
			}catch(IOException ee){
				ServerMain.logger.info("error closing user list input streams");
			}
		}
		
		return returnedList;
	}
	
	public static void saveUsers(String filePath, List<User> userList){
		FileOutputStream fout = null;
		ObjectOutputStream oout = null;
		try{
			fout = new FileOutputStream(filePath);
			oout = new ObjectOutputStream(fout);
			oout.writeObject(userList);	// serialize & write list
			oout.close();
			fout.close();
		}catch(FileNotFoundException e){
			ServerMain.logger.info("user file not found");
		}catch(IOException e){
			ServerMain.logger.info("IO exception reading user file");
		}catch(Exception e){
			ServerMain.logger.info("error serializing");
		}finally{
			// Always close streams
			try{
			if(fout != null)
				fout.close();
			if(oout != null)
				oout.close();
			}catch(IOException ee){
				ServerMain.logger.info("error closing user list input streams");
			}
		}	
		ServerMain.logger.info("user list saved");
	}
}
