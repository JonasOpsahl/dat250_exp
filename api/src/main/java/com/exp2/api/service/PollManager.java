package com.exp2.api.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.exp2.api.model.Poll;
import com.exp2.api.model.User;
import com.exp2.api.model.Vote;
import com.exp2.api.model.VoteOption;

@Component
public class PollManager {
    
    private Map<Integer, User> users = new HashMap<>();
    private Map<Integer, Poll> polls = new HashMap<>();
    private Map<Integer, Vote> allVotes = new HashMap<>();

    public PollManager() {

    }

    private Integer userIdCreator() {
        return users.size()+1;
    }

    private Integer pollIdCreator() {
        return polls.size()+1;
    }

    private Integer voteIdCreator() {
        return allVotes.size()+1;
    }



    // Users

    // Create
    public User createUser(String username, String email, String password) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password);
        Integer userId = userIdCreator();
        newUser.setUserId(userId);
        users.put(userId, newUser);
        return newUser;
    }

    // Read
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    // Read
    public User getUser(Integer userId) {
        return users.get(userId);
    }

    // Update
    public User updateUser(Integer userId, Optional<String> newUsername, Optional<String> newEmail, Optional<String> newPassword) {
        User toChange = getUser(userId);
        if (newUsername.isPresent()) {
            toChange.setUsername(newUsername.get());
        }
        if (newEmail.isPresent()) {
            toChange.setEmail(newEmail.get());
        }
        if (newPassword.isPresent()) {
            toChange.setPassword(newPassword.get());
        }
        users.put(userId, toChange);
        return toChange;
    }

    // Delete
    public boolean deleteUser(Integer id) {
        users.remove(id);
        return true;
    }

    // Polls

    // Create
    public Poll createPoll(String question, Integer durationDays, Integer creatorId, Poll.Visibility visibility, 
                           Optional<Integer> maxVotesPerUser, 
                           List<Integer> invitedUsers, List<VoteOption> pollOptions) {
        Poll newPoll = new Poll();
        if (users.get(creatorId) == null) {
            return null;
        }

        newPoll.setVisibility(visibility);
        if (visibility == Poll.Visibility.PRIVATE) {
            invitedUsers.add(creatorId);
            newPoll.setInvitedUsers(invitedUsers.stream().distinct().toList());
            newPoll.setMaxVotesPerUser(maxVotesPerUser.orElse(1));
        }
        else {
            newPoll.setInvitedUsers(new ArrayList<>(users.keySet()));
        }
        newPoll.setQuestion(question);
        newPoll.setPublishedAt(Instant.now());
        Instant validUntil = Instant.now().plus(Duration.ofDays(durationDays));
        newPoll.setValidUntil(validUntil);
        newPoll.setCreatorId(creatorId);

        newPoll.setPollOptions(pollOptions);

        Integer pollId = pollIdCreator();
        newPoll.setDurationDays(durationDays);
        newPoll.setPollId(pollId);
        polls.put(pollId, newPoll);

        return newPoll;
    }

    // Read
    public List<Poll> getPolls(Optional<Integer> userId) {
        List<Poll> returnList = new ArrayList<>();
        
        if (userId.isEmpty()) {
            for (Poll poll : polls.values()) {
                if (poll.getVisibility() == Poll.Visibility.PUBLIC) {
                    returnList.add(poll);
                }
            }
        } else {
            Integer id = userId.get();
            
            for (Poll poll : polls.values()) {
                if (poll.getVisibility() == Poll.Visibility.PUBLIC || 
                    (poll.getVisibility() == Poll.Visibility.PRIVATE && poll.getInvitedUsers().contains(id))) {
                    returnList.add(poll);
                }
            }
        }
        return returnList;
    }

    // Read
    public Poll getPoll(Integer pollId, Integer userId) {
        Poll poll = polls.get(pollId);
        if (poll.getVisibility() == Poll.Visibility.PUBLIC) {
            return polls.get(pollId);
        }
        if (poll.getVisibility() == Poll.Visibility.PRIVATE && poll.getInvitedUsers().contains(userId)) {
            return polls.get(pollId);
        }
        else {
            return null;
        }
    }

    // Update
    public Poll updatePoll(Optional<Integer> durationDays, Integer pollId, Integer userId, List<Integer> newInvites) {
        Poll toUpdate = getPoll(pollId, userId);
        
        if (toUpdate == null || !toUpdate.getCreatorId().equals(userId)) {
             return null;
        }


        List<Integer> currentInvites = toUpdate.getInvitedUsers();
        List<Integer> allInvites = Stream.concat(currentInvites.stream(), newInvites.stream())
                                     .distinct()
                                     .collect(Collectors.toList());
        toUpdate.setInvitedUsers(allInvites);


        if (durationDays.isPresent()) {
            Integer durationDaysInt = durationDays.get();
            Instant currentDeadline = toUpdate.getValidUntil();
            Instant newDeadline = currentDeadline.plus(Duration.ofDays(durationDaysInt));
            toUpdate.setValidUntil(newDeadline);

        }
        toUpdate.setPublishedAt(Instant.now());
        polls.put(pollId, toUpdate);

        return toUpdate;
    }

    // Delete
    public boolean deletePoll(Integer pollId) {
        polls.remove(pollId);
        return true;
    }

    // Votes AND VoteOptions

    // Create vote

    public boolean castVote(Integer pollId, Optional<Integer> userId, Integer presentationOrder) {
        Poll poll = polls.get(pollId);

        if (poll == null || Instant.now().isAfter(poll.getValidUntil())) {
            return false;
        }

        VoteOption chosenOption = null;
        for (VoteOption option : poll.getPollOptions()) {
            if (option.getPresentationOrder() == presentationOrder) {
                chosenOption = option;
                break;
            }
        }

        if (chosenOption == null) {
            return false; 
        }
        User voter = null; 
        if (userId.isPresent()) {
            voter = users.get(userId.get());
            if(voter == null) return false; 
        }


        // VOTING CHECKS HERE
        if (poll.getVisibility() == Poll.Visibility.PRIVATE) {
            if (voter == null || !poll.getInvitedUsers().contains(voter.getUserId())) {
                return false;
        }

            int userVoteCount = 0;
            for (Vote vote : allVotes.values()) {
                if (vote.getVoter().equals(voter) && poll.getPollOptions().contains(vote.getChosenOption())) {
                    userVoteCount++;
                }
            }

            if (userVoteCount >= poll.getMaxVotesPerUser()) {
                return false;
            }
        }

        Vote newVote = new Vote();
        newVote.setVoteId(voteIdCreator());
        newVote.setPublishedAt(Instant.now());
        newVote.setVoter(voter);

        newVote.setChosenOption(chosenOption);
        
        allVotes.put(newVote.getVoteId(), newVote);
        return true;
    }

    public Map<String, Integer> getPollResults(Integer pollId) {
        Poll poll = polls.get(pollId);
        if (poll == null) {
            return null;
        }
        Map<String, Integer> results = new HashMap<>();
        for (VoteOption option : poll.getPollOptions()) {
            results.put(option.getCaption(), 0);
        }
        for (Vote vote : allVotes.values()) {
            if (poll.getPollOptions().contains(vote.getChosenOption())) {
                String caption = vote.getChosenOption().getCaption();
                results.put(caption, results.get(caption) + 1);
            }
        }
        return results;
    }

    // For testing
    public void reset() {
        users.clear();
        polls.clear();
        allVotes.clear();
    }

}
