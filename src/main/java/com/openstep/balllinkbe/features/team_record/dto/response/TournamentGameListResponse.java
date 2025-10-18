package com.openstep.balllinkbe.features.team_record.dto.response;

import com.openstep.balllinkbe.features.team_record.repository.projection.TournamentGameProjection;
import lombok.*;

import java.time.LocalDateTime;

public final class TournamentGameListResponse {
    private TournamentGameListResponse(){}

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Item {
        private Long gameId;
        private LocalDateTime date;
        private String venueName;
        private String opponentName;
        private String state;    // SCHEDULED / ONGOING / FINISHED / CANCELED
        private Integer myScore;
        private Integer oppScore;
        private String result;   // WIN / LOSE / DRAW

        public static Item from(TournamentGameProjection p) {
            return Item.builder()
                    .gameId(p.getGameId())
                    .date(p.getDate() != null ? p.getDate().toLocalDateTime() : null)
                    .venueName(p.getVenueName())
                    .opponentName(p.getOpponentName())
                    .state(p.getState())
                    .myScore(p.getMyScore())
                    .oppScore(p.getOppScore())
                    .result(p.getResult())
                    .build();
        }
    }
}
