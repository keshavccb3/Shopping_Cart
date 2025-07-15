package com.ecom.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler{
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userService;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		String email = request.getParameter("username");
		UserDtls user = userRepository.findByEmail(email);
		if(user!=null) {
			if(user.isEnable()) {
				if(user.getAccountNonLocked()) {
					if(user.getFailedAttempt()<AppConstant.ATTEMPT_TIME-1) {
						userService.increaseFailedAttempt(user);
					}else {
						userService.userAccountLock(user);
						exception = new LockedException("Your account is Locked !! 3 failed attempt");
					}
				}else {
					if(userService.unlockAccountTimeExpired(user)) {
						exception = new LockedException("Your account is Unlocked !! Please try to login");
					}else {
						exception = new LockedException("Your account is Locked !! PLease try after sometime");
					}

				}
			}else {
				exception = new LockedException("Your account is Inactive");
			}
		}else {
			exception = new LockedException("Bad Credential");
		}
		
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}
	
}
