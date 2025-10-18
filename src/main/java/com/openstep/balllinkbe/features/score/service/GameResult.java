package com.openstep.balllinkbe.features.score.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 명령 처리 후 반환되는 결과 객체
 * - events(): 브로드캐스트할 이벤트 리스트
 * - stateSync(): 전체 동기화 상태
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameResult {
    private List<Map<String, Object>> events = new ArrayList<>();
    private Map<String, Object> stateSync;

    public void addEvent(Map<String, Object> ev) {
        events.add(ev);
    }
}
