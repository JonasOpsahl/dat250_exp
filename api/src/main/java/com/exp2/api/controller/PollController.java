package com.exp2.api.controller;

import org.springframework.web.bind.annotation.RestController;

import com.exp2.api.model.Poll;
import com.exp2.api.model.VoteOption;
import com.exp2.api.service.PollManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@CrossOrigin
@RequestMapping("/api/polls")
public class PollController {
    
    private PollManager pollManager;

    public PollController(PollManager pollManager) {
        this.pollManager = pollManager;
    }

    @RequestMapping
    public List<Poll> getPolls(@RequestParam(required = false) Optional<Integer> userId) {
        return pollManager.getPolls(userId);
    }
    @RequestMapping("/{id}")
    public Poll getPoll(@PathVariable Integer id, @RequestParam Integer userId) {
        return pollManager.getPoll(id, userId);
    }

    @PostMapping
    public Poll createPoll(@RequestParam String question,
                           @RequestParam Integer durationDays,
                           @RequestParam Integer creatorId,
                           @RequestParam Poll.Visibility visibility,
                           @RequestParam Optional<Integer> maxVotesPerUser,
                           @RequestParam(required = false) List<Integer> invitedUsers,
                           @RequestParam List<String> optionCaptions,
                           @RequestParam List<Integer> optionOrders) {

        List<VoteOption> pollOptions = new ArrayList<>();
        for (int i = 0; i < optionCaptions.size(); i++) {
            VoteOption option = new VoteOption();
            option.setCaption(optionCaptions.get(i));
            option.setPresentationOrder(optionOrders.get(i));
            pollOptions.add(option);
        }

        return pollManager.createPoll(
            question,
            durationDays,
            creatorId,
            visibility,
            maxVotesPerUser,
            invitedUsers == null ? new ArrayList<>() : invitedUsers,
            pollOptions
        );
    }

    @PostMapping("/{id}/vote")
    public boolean castVote(
            @PathVariable("id") Integer pollId,
            @RequestParam Integer presentationOrder,
            @RequestParam(required = false) Optional<Integer> userId) {

        return pollManager.castVote(pollId, userId, presentationOrder);
    }

    @GetMapping("/{id}/results")
    public Map<String, Integer> getPollResults(@PathVariable("id") Integer pollId) {
        return pollManager.getPollResults(pollId);
    }

    @PutMapping("/{id}")
    public Poll updatePoll(@PathVariable Integer id, @RequestParam Optional<Integer> durationDays, @RequestParam Integer userId, @RequestParam(required = false) List<Integer> newInvites) {
        if (newInvites == null) {
            List<Integer> newInvitesEmpty = new ArrayList<>();
            return pollManager.updatePoll(durationDays, id ,userId, newInvitesEmpty);
        }
        return pollManager.updatePoll(durationDays, id ,userId, newInvites);
    }

    @DeleteMapping("/{id}")
    public boolean deletePoll(@PathVariable Integer id) {
        return pollManager.deletePoll(id);
    }

}
