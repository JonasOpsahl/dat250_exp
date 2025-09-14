package com.exp2.api.model;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Poll {
    
    private String question;
    private Instant publishedAt;
    private Integer durationDays;
    private Integer pollId;
    private Integer creatorId;
    private Instant validUntil;
    private Visibility visibility;
    private List<Integer> invitedUsers;
    private Integer maxVotesPerUser;
    private List<VoteOption> pollOptions;


    public enum Visibility {
        PUBLIC,
        PRIVATE
    }

    public Poll() {

    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return this.question;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getPublishedAt() {
        return this.publishedAt;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    @JsonIgnore
    public Integer getDurationDays() {
        return this.durationDays;
    }

    public void setPollId(Integer pollId) {
        this.pollId = pollId;
    }

    public Integer getPollId() {
        return this.pollId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public Integer getCreatorId() {
        return this.creatorId;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public Instant getValidUntil() {
        return this.validUntil;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    @JsonIgnore
    public Visibility getVisibility() {
        return this.visibility;
    }

    public void setInvitedUsers(List<Integer> userIds) {
        this.invitedUsers = userIds;
    }

    @JsonIgnore
    public List<Integer> getInvitedUsers() {
        return this.invitedUsers;
    }

    public Integer getMaxVotesPerUser() {
        return maxVotesPerUser;
    }

    public void setMaxVotesPerUser(Integer maxVotesPerUser) {
        this.maxVotesPerUser = maxVotesPerUser;
    }

    public void setPollOptions(List<VoteOption> pollOptions) { 
        this.pollOptions = pollOptions; 
    }

    public List<VoteOption> getPollOptions() { 
        return pollOptions; 
    }

}
