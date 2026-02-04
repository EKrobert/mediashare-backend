package ezian.robert.mediasharebackend.controller.api;

import ezian.robert.mediasharebackend.model.User;
import ezian.robert.mediasharebackend.service.UserServiceImpl;
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
public class UserController {
    private UserServiceImpl userService;
    private PasswordEncoder passwordEncoder;

    public UserController(UserServiceImpl userService, PasswordEncoder passwordEncoder){
        this.userService=userService;
        this.passwordEncoder=passwordEncoder;
    }

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

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication);
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Non authentifié"));
        }

        User user = (User) authentication.getPrincipal();

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("username", user.getUsername());
        profile.put("role", user.getRole());
        profile.put("avatarUrl", user.getAvatarUrl());

        return ResponseEntity.ok(profile);
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}