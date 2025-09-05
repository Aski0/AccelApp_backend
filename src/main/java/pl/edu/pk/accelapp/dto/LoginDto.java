package pl.edu.pk.accelapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @NotBlank(message = "Email nie może być pusty")
    @Email(message = "Nieprawidłowy format email")
    private String email;

    @NotBlank(message = "Hasło nie może być puste")
    private String password;

    public LoginDto() {}
    public LoginDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
}
