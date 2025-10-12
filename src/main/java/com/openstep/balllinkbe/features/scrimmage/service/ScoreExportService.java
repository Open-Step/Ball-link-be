package com.openstep.balllinkbe.features.scrimmage.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameEvent;

import java.util.List;

public interface ScoreExportService {

    // PDF로 변환하기
    byte[] generatePdfScoreSheet(Game game, List<GameEvent> events, String exportFormat);
}