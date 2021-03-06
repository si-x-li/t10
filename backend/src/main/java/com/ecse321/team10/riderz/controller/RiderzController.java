package com.ecse321.team10.riderz.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecse321.team10.riderz.dto.AuthenticationDto;
import com.ecse321.team10.riderz.model.Authentication;
import com.ecse321.team10.riderz.sql.MySQLJDBC;
/**
 * Index controller of the application
 *
 */
@RestController
public class RiderzController {
	private static final Logger logger = LogManager.getLogger(RiderzController.class);
	@Autowired
	MySQLJDBC sql;
	
	@RequestMapping("/")
	public String mainPage() {
		logger.info("An info message");
		return "Index page, web frontend is to be added later.\n";
	}

	@Autowired
	private ModelMapper modelMapper;
	
	private AuthenticationDto convertToDto(Authentication auth) { return modelMapper.map(auth, AuthenticationDto.class); }
	
	/**
	 * Verifies the user. If they are valid, delete the old session and insert the new session.
	 * @param username	-	A String representing the User's username.
	 * @param password	-	A String representing the User's password.
	 * @return String indicating the verification status.
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String login( @RequestParam String username,
						 @RequestParam String password) {
		try {
			sql.connect();
			if (sql.verifyLogin(username, password)) {
				sql.deleteAuthentication(username);
				sql.insertAuthentication(username);
				sql.closeConnection();
				return "User session has been established!";
			} else {
				sql.closeConnection();
				return "This combination does not exist!";
			}
		} catch (Exception e) {
			logger.error("Authentication error: " + e.getMessage());
			return "Server encountered errors please report this incidence to an administrator";
		}
	}
	
	/**
	 * Obtains all session time.
	 * @return A list of AuthenticationDto object(s). Otherwise null.
	 */
	@RequestMapping(value = "/session", method = RequestMethod.GET)
	public List<AuthenticationDto> getSessionTime() {
		if (sql.connect()) {
			List<AuthenticationDto> sessions = new ArrayList<>();
			for(Authentication session: sql.getSession()) {
				sessions.add(convertToDto(session));
			}
			sql.closeConnection();
			logger.info("Successfully got a list of all sessions.");
			return sessions;
		} else {
			logger.info("There was no session value");
			return null;
		}
	}
}
