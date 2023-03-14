package com.example.dailychallenge.service.badge;

import static com.example.dailychallenge.util.fixture.TokenFixture.EMAIL;
import static com.example.dailychallenge.util.fixture.TokenFixture.PASSWORD;
import static com.example.dailychallenge.util.fixture.challenge.ChallengeFixture.createChallengeDto;
import static com.example.dailychallenge.util.fixture.user.UserFixture.OTHER_EMAIL;
import static com.example.dailychallenge.util.fixture.user.UserFixture.OTHER_USERNAME;
import static com.example.dailychallenge.util.fixture.user.UserFixture.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.dailychallenge.entity.badge.UserBadgeEvaluation;
import com.example.dailychallenge.entity.challenge.Challenge;
import com.example.dailychallenge.entity.challenge.UserChallenge;
import com.example.dailychallenge.entity.users.User;
import com.example.dailychallenge.repository.badge.BadgeRepository;
import com.example.dailychallenge.repository.badge.UserBadgeRepository;
import com.example.dailychallenge.util.ServiceTest;
import com.example.dailychallenge.util.fixture.TestDataSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserBadgeEvaluationServiceTest extends ServiceTest {
    @Autowired
    private TestDataSetup testDataSetup;
    @Autowired
    private BadgeRepository badgeRepository;
    @Autowired
    private UserBadgeRepository userBadgeRepository;
    @Autowired
    private UserBadgeEvaluationService userBadgeEvaluationService;

    private User user;

    @BeforeEach
    void beforeEach() {
        user = testDataSetup.saveUser(USERNAME, EMAIL, PASSWORD);
        UserBadgeEvaluation userBadgeEvaluation = testDataSetup.saveUserBadgeEvaluation(user);

        for (int i = 0; i < 9; i++) {
            챌린지를_생성하고_참여하고_달성한다();

            userBadgeEvaluation.addNumberOfAchievement();
            userBadgeEvaluation.addNumberOfChallengeCreate();
        }
    }

    @Nested
    @DisplayName("N개 달성 뱃지")
    class AchievementBadge {
        @Test
        @DisplayName("생성 테스트")
        void canCreateTest() {
            챌린지를_생성하고_참여하고_달성한다();

            userBadgeEvaluationService.createAchievementBadgeIfFollowStandard(user);

            assertEquals("챌린지 10개 달성", badgeRepository.findAll().get(0).getName());
        }

        @Test
        @DisplayName("생성하지 못하는 테스트")
        void canNotCreateTest() {
            for (int i = 0; i < 2; i++) {
                챌린지를_생성하고_참여하고_달성한다();
                UserBadgeEvaluation userBadgeEvaluation = user.getUserBadgeEvaluation();
                userBadgeEvaluation.addNumberOfAchievement();
            }

            userBadgeEvaluationService.createAchievementBadgeIfFollowStandard(user);

            assertTrue(badgeRepository.findAll().isEmpty());
        }

        @Test
        @DisplayName("삭제 테스트")
        void deleteTest() {
            챌린지를_생성하고_참여하고_달성한다();
            userBadgeEvaluationService.createAchievementBadgeIfFollowStandard(user);

            userBadgeEvaluationService.deleteAchievementBadgeIfFollowStandard(user);

            // User의 userBadge에 cascade가 걸려 있어서 삭제 되지 않았었음
            UserBadgeEvaluation userBadgeEvaluation = user.getUserBadgeEvaluation();
            assertEquals(9, userBadgeEvaluation.getNumberOfAchievement());
            assertEquals(0, badgeRepository.findAll().size());
            assertEquals(0, userBadgeRepository.findAll().size());
        }
    }

    @Nested
    @DisplayName("챌린지 N개 생성 뱃지")
    class ChallengeCreateBadge {
        @Test
        @DisplayName("생성 테스트")
        void canCreateTest() {
            Challenge challenge = testDataSetup.챌린지를_생성한다(createChallengeDto(), user);
            testDataSetup.챌린지에_참가한다(challenge, user);

            userBadgeEvaluationService.createChallengeCreateBadgeIfFollowStandard(user);

            assertEquals("챌린지 10개 생성", badgeRepository.findAll().get(0).getName());
        }

        @Test
        @DisplayName("생성하지 못하는 테스트")
        void canNotCreateTest() {
            for (int i = 0; i < 2; i++) {
                Challenge challenge = testDataSetup.챌린지를_생성한다(createChallengeDto(), user);
                testDataSetup.챌린지에_참가한다(challenge, user);
                UserBadgeEvaluation userBadgeEvaluation = user.getUserBadgeEvaluation();
                userBadgeEvaluation.addNumberOfChallengeCreate();
            }

            userBadgeEvaluationService.createChallengeCreateBadgeIfFollowStandard(user);

            assertTrue(badgeRepository.findAll().isEmpty());
        }

        @Test
        @DisplayName("삭제 테스트")
        void deleteTest() {
            Challenge challenge = testDataSetup.챌린지를_생성한다(createChallengeDto(), user);
            testDataSetup.챌린지에_참가한다(challenge, user);
            userBadgeEvaluationService.createChallengeCreateBadgeIfFollowStandard(user);

            userBadgeEvaluationService.deleteChallengeCreateBadgeIfFollowStandard(user);

            // User의 userBadge에 cascade가 걸려 있어서 삭제 되지 않았었음
            UserBadgeEvaluation userBadgeEvaluation = user.getUserBadgeEvaluation();
            assertEquals(9, userBadgeEvaluation.getNumberOfChallengeCreate());
            assertEquals(0, badgeRepository.findAll().size());
            assertEquals(0, userBadgeRepository.findAll().size());
        }
    }

    @Nested
    @DisplayName("후기 N개 작성 뱃지")
    class CommentWriteBadge {
        @Test
        @DisplayName("생성 테스트")
        void canCreateTest() {
            UserBadgeEvaluation userBadgeEvaluation = user.getUserBadgeEvaluation();
            User otherUser = testDataSetup.saveUser(OTHER_USERNAME, OTHER_EMAIL, PASSWORD);
            for (int i = 0; i < 9; i++) {
                Challenge challenge = testDataSetup.챌린지를_생성한다(createChallengeDto(), otherUser);
                testDataSetup.챌린지에_참가한다(challenge, otherUser);
                testDataSetup.챌린지에_참가한다(challenge, user);

                testDataSetup.챌린지에_댓글을_단다(challenge, user, "content" + i);
                userBadgeEvaluation.addNumberOfCommentWrite();
            }
            Challenge challenge = testDataSetup.챌린지를_생성한다(createChallengeDto(), otherUser);
            testDataSetup.챌린지에_댓글을_단다(challenge, user, "content" + 9);

            userBadgeEvaluationService.createCommentWriteBadgeIfFollowStandard(user);

            assertEquals("후기 10개 작성", badgeRepository.findAll().get(0).getName());
        }

        @Test
        @DisplayName("생성하지 못하는 테스트")
        void canNotCreateTest() {
            UserBadgeEvaluation userBadgeEvaluation = user.getUserBadgeEvaluation();
            User otherUser = testDataSetup.saveUser(OTHER_USERNAME, OTHER_EMAIL, PASSWORD);
            for (int i = 0; i < 2; i++) {
                Challenge challenge = testDataSetup.챌린지를_생성한다(createChallengeDto(), otherUser);
                testDataSetup.챌린지에_참가한다(challenge, otherUser);
                testDataSetup.챌린지에_참가한다(challenge, user);

                testDataSetup.챌린지에_댓글을_단다(challenge, user, "content" + i);
                userBadgeEvaluation.addNumberOfCommentWrite();
            }
            Challenge challenge = testDataSetup.챌린지를_생성한다(createChallengeDto(), otherUser);
            testDataSetup.챌린지에_댓글을_단다(challenge, user, "content" + 3);

            userBadgeEvaluationService.createCommentWriteBadgeIfFollowStandard(user);

            assertTrue(badgeRepository.findAll().isEmpty());
        }
    }

    private void 챌린지를_생성하고_참여하고_달성한다() {
        Challenge challenge = testDataSetup.챌린지를_생성한다(createChallengeDto(), user);
        UserChallenge userChallenge = testDataSetup.챌린지에_참가한다(challenge, user);
        testDataSetup.챌린지를_달성한다(userChallenge);
    }
}