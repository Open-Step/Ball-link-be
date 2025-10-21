package com.openstep.balllinkbe.features.user.service;

import com.openstep.balllinkbe.features.user.dto.response.MyCareerResponse;
import com.openstep.balllinkbe.features.user.repository.MyCareerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyCareerService {

    private final MyCareerRepository myCareerRepository;

    public MyCareerResponse getMyCareer(Long userId) {
        var recentGames = myCareerRepository.findRecentGames(userId)
                .stream()
                .map(MyCareerResponse.RecentGame::from)
                .toList();

        var seasonStats = myCareerRepository.findSeasonStats(userId)
                .stream()
                .map(MyCareerResponse.SeasonStat::from)
                .toList();

        return new MyCareerResponse(recentGames, seasonStats);
    }
}
