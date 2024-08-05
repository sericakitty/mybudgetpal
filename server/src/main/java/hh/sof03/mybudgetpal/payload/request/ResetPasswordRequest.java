package hh.sof03.mybudgetpal.payload.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotEmpty
    @Size(min=8)
    private String password = "";
    
    @NotEmpty
    @Size(min=8)
    private String passwordCheck = "";

    @NotEmpty
    private String email = "";

    @NotEmpty
    private String token = "";

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordCheck() {
        return passwordCheck;
    }

    public void setPasswordCheck(String passwordCheck) {
        this.passwordCheck = passwordCheck;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
