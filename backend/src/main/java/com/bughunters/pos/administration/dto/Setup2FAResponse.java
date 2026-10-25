package com.bughunters.pos.administration.dto;

public class Setup2FAResponse {

    private String secret;
    private String qrCodeUri;

    public Setup2FAResponse() {}

    public Setup2FAResponse(String secret, String qrCodeUri) {
        this.secret = secret;
        this.qrCodeUri = qrCodeUri;
    }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public String getQrCodeUri() { return qrCodeUri; }
    public void setQrCodeUri(String qrCodeUri) { this.qrCodeUri = qrCodeUri; }
}
