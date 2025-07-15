package com.ecom.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.UserDtls;

public interface UserService {
	public UserDtls saveUser(UserDtls user);

	public UserDtls findByEmail(String email);

	public List<UserDtls> getUsers(String role);

	public Boolean updateAccountStatus(Integer id, Boolean status);
	
	public void increaseFailedAttempt(UserDtls user);
	
	public void userAccountLock(UserDtls user);
	
	public Boolean unlockAccountTimeExpired(UserDtls user);
	
	public void resetAttempt(int userId);

	public void updateUserResetToken(String email, String token);

	public UserDtls getUserByToken(String token);

	public void updateUser(UserDtls user);
	
	public UserDtls updateUserProfile(UserDtls user, MultipartFile img) throws IOException;
	
	public UserDtls saveAdmin(UserDtls user);
	
	public Boolean existsEmail(String email);
	
}
