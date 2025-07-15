package com.ecom.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

@Service
public class UserServiceImpl implements UserService{
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDtls saveUser(UserDtls user) {
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		user.setLockTime(null);
		user.setRole("ROLE_USER");
		user.setEnable(true);
		String encodePassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassword);
		return userRepository.save(user);
	}

	@Override
	public UserDtls findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public List<UserDtls> getUsers(String role) {
		return userRepository.findByRole(role);
	}

	@Override
	public Boolean updateAccountStatus(Integer id, Boolean status) {
		
		Optional<UserDtls> findByUser = userRepository.findById(id);
		if(findByUser.isPresent()) {
			UserDtls user = findByUser.get();
			user.setEnable(status);
			userRepository.save(user);
			return true;
		}
		return false;
	}

	@Override
	public void increaseFailedAttempt(UserDtls user) {
		int attempt = user.getFailedAttempt()+1;
		user.setFailedAttempt(attempt);
		userRepository.save(user);
		
	}

	@Override
	public void userAccountLock(UserDtls user) {
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepository.save(user);
	}

	@Override
	public Boolean unlockAccountTimeExpired(UserDtls user) {
		long lockTime = user.getLockTime().getTime();
		long unlockTime = lockTime+ AppConstant.UNLOCK_DURATION_TIME;
		long currentTime = System.currentTimeMillis();
		if(unlockTime<=currentTime) {
			user.setAccountNonLocked(true);
			user.setFailedAttempt(0);
			user.setLockTime(null);
			userRepository.save(user);
			return true;
		}
		return false;
	}

	@Override
	public void resetAttempt(int userId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateUserResetToken(String email, String token) {
		UserDtls user = userRepository.findByEmail(email);
		user.setResetToken(token);
		userRepository.save(user);
		
	}

	@Override
	public UserDtls getUserByToken(String token) {
		return userRepository.findByResetToken(token);
	}

	@Override
	public void updateUser(UserDtls user) {
		userRepository.save(user);
	}

	@Override
	public UserDtls updateUserProfile(UserDtls user, MultipartFile img) throws IOException {
		UserDtls dbUser = userRepository.findById(user.getId()).get();
		
		if(!ObjectUtils.isEmpty(dbUser)) {
			if(!img.isEmpty()) {
				dbUser.setProfileImage(img.getOriginalFilename());
			}
			dbUser.setEmail(user.getEmail()); 
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser.setMobileNumber(user.getMobileNumber());
			dbUser.setName(user.getName());
			dbUser.setPincode(user.getPincode());
			dbUser = userRepository.save(dbUser);
		}
		if(!img.isEmpty()) {
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
					+ img.getOriginalFilename());
			Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		}
		return dbUser;
	}

	@Override
	public UserDtls saveAdmin(UserDtls user) {
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		user.setLockTime(null);
		user.setRole("ROLE_ADMIN");
		user.setEnable(true);
		String encodePassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassword);
		return userRepository.save(user);
	}

	@Override
	public Boolean existsEmail(String email) {
		return userRepository.existsByEmail(email);
	}

}
