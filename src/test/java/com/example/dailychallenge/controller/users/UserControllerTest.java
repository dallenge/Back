package com.example.dailychallenge.controller.users;

import static com.example.dailychallenge.util.fixture.TokenFixture.AUTHORIZATION;
import static com.example.dailychallenge.util.fixture.TokenFixture.EMAIL;
import static com.example.dailychallenge.util.fixture.TokenFixture.PASSWORD;
import static com.example.dailychallenge.util.fixture.TokenFixture.TOKEN_PREFIX;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dailychallenge.dto.ChallengeDto;
import com.example.dailychallenge.dto.UserDto;
import com.example.dailychallenge.entity.challenge.Challenge;
import com.example.dailychallenge.entity.challenge.ChallengeCategory;
import com.example.dailychallenge.entity.challenge.ChallengeDuration;
import com.example.dailychallenge.entity.challenge.ChallengeLocation;
import com.example.dailychallenge.entity.challenge.UserChallenge;
import com.example.dailychallenge.entity.users.User;
import com.example.dailychallenge.exception.users.UserDuplicateCheck;
import com.example.dailychallenge.exception.users.UserDuplicateNotCheck;
import com.example.dailychallenge.exception.users.UserPasswordCheck;
import com.example.dailychallenge.repository.UserRepository;
import com.example.dailychallenge.service.challenge.ChallengeService;
import com.example.dailychallenge.service.challenge.UserChallengeService;
import com.example.dailychallenge.service.users.UserService;
import com.example.dailychallenge.utils.JwtTokenUtil;
import com.example.dailychallenge.vo.RequestUpdateUser;
import com.example.dailychallenge.vo.RequestUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class UserControllerTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private ChallengeService challengeService;
    @Autowired
    private UserChallengeService userChallengeService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public UserDto createUser() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test1234@test.com");
        userDto.setUserName("홍길동");
        userDto.setInfo("testInfo");
        userDto.setPassword("1234");
        return userDto;
    }

    MockMultipartFile createMultipartFiles() {
        String path = "userImgFile";
        String imageName = "editImage.jpg";
        MockMultipartFile multipartFile = new MockMultipartFile(path, imageName,
                "image/jpg", new byte[]{1, 2, 3, 4});
        return multipartFile;
    }

    public Challenge createChallenge() throws Exception {
        User savedUser = userService.saveUser(createUser(), passwordEncoder);
        ChallengeDto challengeDto = ChallengeDto.builder()
                .title("제목입니다.")
                .content("내용입니다.")
                .challengeCategory(ChallengeCategory.STUDY.getDescription())
                .challengeLocation(ChallengeLocation.INDOOR.getDescription())
                .challengeDuration(ChallengeDuration.WITHIN_TEN_MINUTES.getDescription())
                .build();
        MultipartFile challengeImg = createMultipartFiles();
        List<MultipartFile> challengeImgFiles = List.of(challengeImg);
        return challengeService.saveChallenge(challengeDto, challengeImgFiles, savedUser);
    }
    public Challenge createChallenge2() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test12345@test.com");
        userDto.setUserName("홍길동");
        userDto.setInfo("testInfo");
        userDto.setPassword("1234");

        User savedUser = userService.saveUser(userDto, passwordEncoder);
        ChallengeDto challengeDto = ChallengeDto.builder()
                .title("제목입니다.")
                .content("내용입니다.")
                .challengeCategory(ChallengeCategory.STUDY.getDescription())
                .challengeLocation(ChallengeLocation.INDOOR.getDescription())
                .challengeDuration(ChallengeDuration.WITHIN_TEN_MINUTES.getDescription())
                .build();
        MultipartFile challengeImg = createMultipartFiles();
        List<MultipartFile> challengeImgFiles = List.of(challengeImg);
        return challengeService.saveChallenge(challengeDto, challengeImgFiles, savedUser);
    }

    @Test
    @DisplayName("회원가입 테스트")
    public void newUserTest() throws Exception {
        RequestUser requestUser = RequestUser.builder()
                .userName("GilDong")
                .email("test@test.com")
                .password("1234")
                .build();

        mockMvc.perform(post("/user/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUser))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 테스트")
    public void loginUserTest() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "test1234@test.com");
        loginData.put("password", "1234");

        userService.saveUser(createUser(), passwordEncoder);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 정보 수정 api 테스트")
    public void updateUserTest() throws Exception {
        User savedUser = userService.saveUser(createUser(), passwordEncoder);
        Long userId = savedUser.getId();
        RequestUpdateUser requestUpdateUserBuild = RequestUpdateUser.builder()
                .userName("editName")
                .info("editInfo")
                .build();

        MockMultipartFile userImgFile = createMultipartFiles();
        String data = objectMapper.writeValueAsString(requestUpdateUserBuild);
        MockMultipartFile requestUpdateUser = new MockMultipartFile("requestUpdateUser", "requestUpdateUser",
                "application/json", data.getBytes(
                StandardCharsets.UTF_8));

        String token = generateToken();
        mockMvc.perform(multipart("/user/{userId}", userId)
                        .file(userImgFile)
                        .file(requestUpdateUser)
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 삭제 api 테스트")
    public void deleteUserTest() throws Exception {
        User savedUser = userService.saveUser(createUser(), passwordEncoder);
        Long userId = savedUser.getId();

        String token = generateToken();
        mockMvc.perform(delete("/user/{userId}", userId)
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 아이디 중복 에러 테스트 - 검증 url")
    public void duplicateUserIdTest() throws Exception {
        User user = userService.saveUser(createUser(), passwordEncoder);
        String token = generateToken();

        mockMvc.perform(post("/user/check")
                        .param("email","test1234@test.com")
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result ->
                        assertTrue(result.getResolvedException().getClass().isAssignableFrom(UserDuplicateCheck.class)))
                .andDo(print());
    }

    @Test // 중복일 때만 예외 발생
    @DisplayName("회원가입 중복 에러 테스트 - 아이디 중복 체크를 안하고 회원가입하는 경우")
    public void duplicateUserTest() throws Exception {
        userService.saveUser(createUser(), passwordEncoder);
        RequestUser requestUser = RequestUser.builder()
                .userName("GilDong")
                .email("test1234@test.com")
                .password("1234")
                .build();

        mockMvc.perform(post("/user/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUser))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result ->
                        assertTrue(result.getResolvedException().getClass().isAssignableFrom(UserDuplicateNotCheck.class)))
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 검증 테스트")
    public void checkUserPassword() throws Exception {
        User user = userService.saveUser(createUser(), passwordEncoder);
        mockMvc.perform(post("/user/{userId}/check",user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("password","1234")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 검증 에러 테스트")
    public void checkUserPasswordError() throws Exception {
        User user = userService.saveUser(createUser(), passwordEncoder);

        mockMvc.perform(post("/user/{userId}/check",user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("password","12345")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(result ->
                        assertTrue(result.getResolvedException().getClass().isAssignableFrom(UserPasswordCheck.class)))
                .andDo(print());
    }

    @Test
    @DisplayName("비밀번호 변경 테스트")
    public void changeUserPassword() throws Exception {
        User user = userService.saveUser(createUser(), passwordEncoder);
        mockMvc.perform(post("/user/{userId}/change",user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("oldPassword","1234")
                        .param("newPassword","12345")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("회원정보 조회 테스트")
    public void getUserInfo() throws Exception {
        User user = userService.saveUser(createUser(), passwordEncoder);
        String token = generateToken();
        mockMvc.perform(get("/user/{userId}", user.getId())
                        .header(AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("내가 작성한 챌린지 조회 테스트")
    public void getUserChallenge() throws Exception {
        createChallenge();
        String token = generateToken();

        mockMvc.perform(get("/user/challenge")
                        .header(AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    @DisplayName("내가 참가한 챌린지 조회 테스트")
    public void getParticipateChallenge() throws Exception {
        Challenge challenge = createChallenge2();

        User savedUser = userService.saveUser(createUser(), passwordEncoder);

        UserChallenge savedUserChallenge = userChallengeService.saveUserChallenge(challenge, savedUser);
        userChallengeService.challengeParticipate(savedUserChallenge);

        String token = generateToken();
        mockMvc.perform(get("/user/participate")
                        .header(AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    private String generateToken() {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD));
        if (auth.isAuthenticated()) {
            UserDetails userDetails = userService.loadUserByUsername(EMAIL);
            return TOKEN_PREFIX + jwtTokenUtil.generateToken(userDetails);
        }

        throw new IllegalArgumentException("token 생성 오류");
    }
}