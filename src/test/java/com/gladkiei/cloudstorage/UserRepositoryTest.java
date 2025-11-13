package com.gladkiei.cloudstorage;


import com.gladkiei.cloudstorage.models.User;
import com.gladkiei.cloudstorage.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
public class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private static final String NAME = "Test";
    private static final String NEW_NAME = "New name";
    private static final String PASSWORD = "12345";
    private final User user = new User(NAME, PASSWORD);

    @Test
    void saveAndFindUserByName() {
        userRepository.save(user);
        Optional<User> optional = userRepository.findByUsername(NAME);
        assertThat(optional.isPresent());
        assertEquals(NAME, optional.get().getUsername());
    }

    @Test
    void shouldUpdateUserName() {
        userRepository.save(user);

        assertEquals(NAME, user.getUsername());

        user.setUsername(NEW_NAME);
        userRepository.save(user);

        assertTrue(userRepository.findByUsername(NEW_NAME).isPresent());
        assertFalse(userRepository.findByUsername(NAME).isPresent());
    }

    @Test
    void shouldGetAllUsers() {
        userRepository.save(user);
        List<User> all = userRepository.findAll();
        assertEquals(1, all.size());
    }

    @Test
    void shouldDeleteUser() {
        assertTrue(userRepository.findAll().isEmpty());

        User saved = userRepository.save(user);
        assertFalse(userRepository.findAll().isEmpty());

        userRepository.delete(saved);
        assertTrue(userRepository.findAll().isEmpty());
    }

}
