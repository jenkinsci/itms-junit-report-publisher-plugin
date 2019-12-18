package io.jenkins.plugins.model;

import hudson.util.Secret;

public class AuthenticationInfo {
    private Secret username;
    private Secret token;

    public String getUsername() {
        return Secret.toString(username);
    }

    public void setUsername(Secret username) {
        this.username = username;
    }

    public String getToken() {
        return Secret.toString(token);
    }

    public void setToken(Secret token) {
        this.token = token;
    }

}
