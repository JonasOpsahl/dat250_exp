package com.exp2.api.model;

import java.time.Instant;

public class Vote {
    
    private Instant publishedAt;
    private Integer voteId;
    private User voter;
    private VoteOption chosenOption;
    private Integer userId;

    
    public Vote() {

    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getPublishedAt() {
        return this.publishedAt;
    }

    public void setVoteId(Integer voteId) {
        this.voteId = voteId;
    }

    public Integer getVoteId() {
        return this.voteId;
    }
    
    public void setVoterId(Integer userId) {
        this.userId = userId;
    }

    public Integer getVoterId() {
        return this.userId;
    }

    public void setVoter(User voter) {
        this.voter = voter;
    }

    public User getVoter() {
        return this.voter;
    }

    public void setChosenOption(VoteOption chosenOption) {
        this.chosenOption = chosenOption;
    }

    public VoteOption getChosenOption() {
        return this.chosenOption;
    }

}
