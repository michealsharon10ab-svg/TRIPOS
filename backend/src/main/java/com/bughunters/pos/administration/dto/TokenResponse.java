package com.bughunters.pos.administration.dto;

public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private boolean requires2FA = false;
    private String tempToken;
    private UserResponseDTO user;
    private BranchSummaryDTO activeBranch;

    public TokenResponse() {}

    public static TokenResponse for2FA(String tempToken) {
        TokenResponse response = new TokenResponse();
        response.setRequires2FA(true);
        response.setTempToken(tempToken);
        return response;
    }

    public static TokenResponse success(String accessToken, String refreshToken, long expiresIn,
                                        UserResponseDTO user, BranchSummaryDTO activeBranch) {
        TokenResponse response = new TokenResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(expiresIn);
        response.setUser(user);
        response.setActiveBranch(activeBranch);
        response.setRequires2FA(false);
        return response;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public boolean isRequires2FA() { return requires2FA; }
    public void setRequires2FA(boolean requires2FA) { this.requires2FA = requires2FA; }

    public String getTempToken() { return tempToken; }
    public void setTempToken(String tempToken) { this.tempToken = tempToken; }

    public UserResponseDTO getUser() { return user; }
    public void setUser(UserResponseDTO user) { this.user = user; }

    public BranchSummaryDTO getActiveBranch() { return activeBranch; }
    public void setActiveBranch(BranchSummaryDTO activeBranch) { this.activeBranch = activeBranch; }
}
