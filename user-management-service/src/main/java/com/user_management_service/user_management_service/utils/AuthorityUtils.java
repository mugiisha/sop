package com.user_management_service.user_management_service.utils;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.*;

public class AuthorityUtils {

    public static Collection<? extends GrantedAuthority> getAuthorities(String roleName) {
        if (roleName != null) {
            return Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + roleName)
            );
        }
        return Collections.emptyList();
    }
}