package ezian.robert.mediasharebackend.controller.api;

import ezian.robert.mediasharebackend.model.User;
import ezian.robert.mediasharebackend.service.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/user")
@Tag(name = "Utilisateurs", description = "Inscription et profil des utilisateurs")
public class UserController {
    private UserServiceImpl userService;
    private PasswordEncoder passwordEncoder;

    public UserController(UserServiceImpl userService, PasswordEncoder passwordEncoder){
        this.userService=userService;
        this.passwordEncoder=passwordEncoder;
    }

    @Operation(
            summary = "Créer un compte",
            description = "Crée un nouvel utilisateur. Les champs email, username et password sont obligatoires. "
                    + "L'email et le nom d'utilisateur doivent être uniques. Route publique (pas de token)."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Informations du compte à créer",
            required = true,
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "email": "robert@mediashare.com",
                      "username": "robert_ezian",
                      "password": "MotDePasse#2026"
                    }
                    """))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Compte créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Email, username ou mot de passe manquant"),
            @ApiResponse(responseCode = "409", description = "Email ou nom d'utilisateur déjà utilisé"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la création de l'utilisateur")
    })
    @PostMapping("/inscription")
    public ResponseEntity<?> inscription(@RequestBody Map<String, String> request){
        try {
            // Validation des champs
            String email = request.get("email");
            String username = request.get("username");
            String password = request.get("password");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "L'email est obligatoire"));
            }

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Le nom d'utilisateur est obligatoire"));
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Le mot de passe est obligatoire"));
            }

            // Vérifier si l'email existe déjà
            if (userService.findByEmail(email) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Cet email est déjà utilisé"));
            }

            // Vérifier si le username existe déjà
            if (userService.findByUsername(username) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Ce nom d'utilisateur est déjà utilisé"));
            }

            User user = new User();
            user.setEmail(email.trim());
            user.setUsername(username.trim());
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("user");

            User usersaved = userService.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Utilisateur créé avec succès");
            response.put("userId", usersaved.getId());
            response.put("email", usersaved.getEmail());
            response.put("username", usersaved.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (DataIntegrityViolationException e) {
            // Erreur de contrainte unique (email ou username déjà existant)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Cet email ou nom d'utilisateur est déjà utilisé"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la création de l'utilisateur"));
        }
    }

    @Operation(
            summary = "Voir mon profil",
            description = "Retourne l'id, l'email, le username, le rôle et l'avatar de l'utilisateur connecté. "
                    + "Nécessite un token valide."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil de l'utilisateur connecté"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication);
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Non authentifié"));
        }

        User user = (User) authentication.getPrincipal();
        User user1 = userService.findById(user.getId());
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("username", user1.getUsername());
        profile.put("role", user.getRole());
        profile.put("avatarUrl", user.getAvatarUrl());

        return ResponseEntity.ok(profile);
    }
}