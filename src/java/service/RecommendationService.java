package service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Session;
import model.SocialNetwork;
import model.User;

public class RecommendationService {

    private SocialNetwork network;
    private Session session;

    public RecommendationService(SocialNetwork network, Session session) {
        this.network = network;
        this.session = session;
    }

    public List<User> recommendFriends() {
        Map<User, Integer> scoredRecommendations = recommendFriendsWithScores();

        List<User> recommendations = new ArrayList<>(scoredRecommendations.keySet());
        recommendations.sort((a, b) -> Integer.compare(scoredRecommendations.get(b), scoredRecommendations.get(a)));

        return recommendations;
    }

    public Map<User, Integer> recommendFriendsWithScores() {
        String currentUserId = session.getCurrentUserId();
        if (currentUserId == null) {
            return new HashMap<>();
        }

        User currentUser = network.getUser(currentUserId);
        if (currentUser == null) {
            return new HashMap<>();
        }

        Map<User, Integer> scores = new HashMap<>();
        Set<String> candidateIds = new HashSet<>();
        Set<String> currentFriendIds = currentUser.getFriendIds();

        for (String friendId : currentFriendIds) {
            User friend = network.getUser(friendId);
            if (friend == null) {
                continue;
            }

            for (String friendOfFriendId : friend.getFriendIds()) {
                if (friendOfFriendId.equals(currentUserId)) {
                    continue;
                }

                if (currentFriendIds.contains(friendOfFriendId)) {
                    continue;
                }

                User candidate = network.getUser(friendOfFriendId);
                if (candidate == null) {
                    continue;
                }

                candidateIds.add(friendOfFriendId);
            }
        }

        for (String candidateId : candidateIds) {
            User candidate = network.getUser(candidateId);
            if (candidate != null) {
                scores.put(candidate, calculateRecommendationScore(currentUser, candidate));
            }
        }

        return scores;
    }

    private int calculateRecommendationScore(User currentUser, User candidate) {
        int score = 0;

        String currentWorkplace = currentUser.getWorkplace();
        String candidateWorkplace = candidate.getWorkplace();
        if (currentWorkplace != null && currentWorkplace.equals(candidateWorkplace)) {
            score += 2;
        }

        String currentHometown = currentUser.getHometown();
        String candidateHometown = candidate.getHometown();
        if (currentHometown != null && currentHometown.equals(candidateHometown)) {
            score += 2;
        }

        Set<String> currentFriendIds = currentUser.getFriendIds();
        Set<String> candidateFriendIds = candidate.getFriendIds();

        int mutualFriends = 0;
        for (String friendId : currentFriendIds) {
            if (candidateFriendIds.contains(friendId)) {
                mutualFriends++;
            }
        }
        score += mutualFriends;

        return score;
    }
}
