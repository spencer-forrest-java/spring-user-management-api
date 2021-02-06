package dto;

import com.usermanagement.domain.Authority;
import com.usermanagement.domain.User;
import lombok.Data;
import lombok.ToString;

import javax.persistence.ManyToOne;
import java.util.Date;

@Data
@ToString
public class UserDto {
	private String userId;
	private String username;
	private String email;
	private String password;
	private String firstName;
	private String lastName;
	private String profileImageUrl;
	private Date joinDate;
	private Date lastLoginDate;
	private Date lastLoginDisplay;
	private boolean isActive;
	private boolean isNotLocked;
	@ManyToOne
	private String role;
	private String[] authorities;

	// Jackson library needs it
	public UserDto() {
	}

	public UserDto(User user) {
		this.userId = user.getUserId();
		this.username = user.getUsername();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.profileImageUrl = user.getProfileImageUrl();
		this.joinDate = user.getJoinDate();
		this.lastLoginDate = user.getLastLoginDate();
		this.lastLoginDisplay = user.getLastLoginDisplay();
		this.isActive = user.isActive();
		this.isNotLocked = user.isNotLocked();
		this.role = user.getRole().getName();
		this.authorities = user.getRole().getAuthorities().stream()
													 .map(Authority::getName).toArray(String[]::new);
	}
}
