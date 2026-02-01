package ezian.robert.mediasharebackend.service;

import ezian.robert.mediasharebackend.model.User;

import java.util.List;

public interface UserService {

    public List<User> findAll();
    public User findByUsername(String username);
    public User findByEmail(String email);
    public User save(User user);
    public boolean delete(User user);
    public User authenticate(String username, String password);


}
