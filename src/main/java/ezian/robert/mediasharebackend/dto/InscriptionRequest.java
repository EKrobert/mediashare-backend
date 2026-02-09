package ezian.robert.mediasharebackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Données pour l'inscription")
public class InscriptionRequest {

    @Schema(description = "Adresse email", example = "user@example.com", required = true)
    private String email;

    @Schema(description = "Nom d'utilisateur", example = "john_doe", required = true)
    private String username;

    @Schema(description = "Mot de passe", example = "Password123!", required = true)
    private String password;

    // Getters et Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}