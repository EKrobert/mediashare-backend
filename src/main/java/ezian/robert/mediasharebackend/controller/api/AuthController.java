package ezian.robert.mediasharebackend.controller.api;

import ezian.robert.mediasharebackend.config.JwtUtil;
import ezian.robert.mediasharebackend.model.User;
import ezian.robert.mediasharebackend.service.UserServiceImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private UserServiceImpl userService;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;

    public AuthController(UserServiceImpl userService, PasswordEncoder passwordEncoder,  JwtUtil jwtUtil)
    {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        User user = userService.authenticate(email, password);

        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Email ou mot de passe incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("avatarUrl", user.getAvatarUrl());

        return ResponseEntity.ok(response);
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
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("USER");

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
}