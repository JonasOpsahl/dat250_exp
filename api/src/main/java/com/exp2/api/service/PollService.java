package com.exp2.api.service;

import com.exp2.api.model.Poll;
import com.exp2.api.model.User;
import com.exp2.api.model.VoteOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface PollService {
    User createUser(String username, String email, String password);
    List<User> getUsers();
    User getUser(Integer userId);
    User updateUser(Integer userId, Optional<String> newUsername, Optional<String> newEmail, Optional<String> newPassword);
    boolean deleteUser(Integer id);
    Poll createPoll(String question, Integer durationDays, Integer creatorId, Poll.Visibility visibility, 
                    Optional<Integer> maxVotesPerUser, 
                    List<Integer> invitedUsers, List<VoteOption> pollOptions);
    List<Poll> getPolls(Optional<Integer> userId);
    Poll getPoll(Integer pollId, Integer userId);
    Poll updatePoll(Optional<Integer> durationDays, Integer pollId, Integer userId, List<Integer> newInvites);
    boolean deletePoll(Integer pollId);
    boolean castVote(Integer pollId, Optional<Integer> userId, Integer presentationOrder);
    Map<String, Integer> getPollResults(Integer pollId);
    void loginUser(Integer userId);
    void logoutUser(Integer userId);
    boolean isUserLoggedIn(Integer userId);
    Set<String> getLoggedInUsers();

    
}