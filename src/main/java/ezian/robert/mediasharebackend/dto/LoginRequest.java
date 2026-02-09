package ezian.robert.mediasharebackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Données pour la connexion")
public class LoginRequest {

    @Schema(description = "Adresse email", example = "user@example.com", required = true)
    private String email;

    @Schema(description = "Mot de passe", example = "Password123!", required = true)
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}