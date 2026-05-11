package com.trafficfine.utils;

import com.trafficfine.model.User;

public class SessionManager {
    private static User currentUser = null;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    public static boolean isOwner() {
        return currentUser != null && "OWNER".equals(currentUser.getRole());
    }

    public static void logout() {
        currentUser = null;
    }
}
