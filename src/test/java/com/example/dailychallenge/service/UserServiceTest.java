package com.example.dailychallenge.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dailychallenge.dto.UserDto;
import com.example.dailychallenge.entity.User;
import com.example.dailychallenge.exception.UserNotFound;
import com.example.dailychallenge.repository.UserRepository;
import com.example.dailychallenge.vo.RequestUpdateUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ObjectMapper objectMapper;

    public UserDto createUser(){
        UserDto userDto = new UserDto();
        userDto.setEmail("test1234@test.com");
        userDto.setUserName("홍길동");
        userDto.setInfo("testInfo");
        userDto.setPassword("1234");
        return userDto;
    }

    @Test
    @DisplayName("회원가입 테스트")
    public void saveUserTest(){
        UserDto userDto = createUser();
        User savedUser = userService.saveUser(userDto,passwordEncoder);

        assertEquals(savedUser.getEmail(),userDto.getEmail());
        assertEquals(savedUser.getUserName(),userDto.getUserName());
        assertEquals(savedUser.getInfo(),userDto.getInfo());

        assertAll(
                () -> assertNotEquals(savedUser.getPassword(), userDto.getPassword()),
                () -> assertTrue(passwordEncoder.matches(userDto.getPassword(), savedUser.getPassword()))
        );
    }

    @Test
    @DisplayName("로그인 테스트")
    public void loginUserTest() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "test1234@test.com");
        loginData.put("password", "1234");

        userService.saveUser(createUser(),passwordEncoder);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 정보 수정 테스트")
    public void updateUserTest(){
        User savedUser = userService.saveUser(createUser(), passwordEncoder);
        RequestUpdateUser requestUpdateUser = RequestUpdateUser.builder()
                .email("edit@edit.com")
                .userName("editName")
                .info("editInfo")
                .password("789")
                .build();

        userService.updateUser(savedUser.getId(), requestUpdateUser, passwordEncoder);

        assertEquals(savedUser.getEmail(), requestUpdateUser.getEmail());
        assertEquals(savedUser.getUserName(), requestUpdateUser.getUserName());
        assertEquals(savedUser.getInfo(), requestUpdateUser.getInfo());
        assertAll(
                () -> assertNotEquals(savedUser.getPassword(), requestUpdateUser.getPassword()),
                () -> assertTrue(passwordEncoder.matches(requestUpdateUser.getPassword(), savedUser.getPassword()))
        );
    }

    @Test
    @DisplayName("존재하지 않는 회원 정보 수정 테스트")
    public void updateNotExistUser(){
        User savedUser = userService.saveUser(createUser(), passwordEncoder);
        RequestUpdateUser requestUpdateUser = RequestUpdateUser.builder()
                .email("edit@edit.com")
                .userName("editName")
                .info("editInfo")
                .password("789")
                .build();
        Long userId = savedUser.getId() + 1;

        assertThatThrownBy(() -> userService.updateUser(userId, requestUpdateUser, passwordEncoder))
                .isInstanceOf(UserNotFound.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("회원 삭제 테스트")
    public void deleteUserTest(){
        User savedUser = userService.saveUser(createUser(), passwordEncoder);
        Long userId = savedUser.getId();

        userService.delete(userId);

        assertEquals(0, userRepository.count());
    }
}