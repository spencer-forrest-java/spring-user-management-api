package com.usermanagement.enumeration;

public enum RoleEnum {
	ADMIN("ROLE_ADMIN"),
	SUPER_ADMIN("ROLE_SUPER_ADMIN");

	public final String label;

	RoleEnum(String label) {
		this.label = label;
	}
}
