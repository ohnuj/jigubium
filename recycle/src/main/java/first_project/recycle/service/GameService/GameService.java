package first_project.recycle.service.GameService;

import first_project.recycle.repository.GameMapper; // mapper -> repository로 변경
import first_project.recycle.service.EcoPointHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class GameService {

    private final GameMapper gameMapper;
    private final EcoPointHistoryService ecoPointHistoryService;

    public GameService(GameMapper gameMapper,
                       EcoPointHistoryService ecoPointHistoryService) {

        this.gameMapper = gameMapper;
        this.ecoPointHistoryService = ecoPointHistoryService;
    }

    public int getTodayPlayCount(Long memberId) {
        return gameMapper.countTodayGamesByMemberId(memberId);
    }

    public List<Map<String, String>> getRandomItems(int count) {
        return gameMapper.findRandomItems(count);
    }

    @Transactional
    public int saveGameResult(Map<String, Object> resultData) {
        Long memberId = ((Number) resultData.get("memberId")).longValue();

        int todayPlayCount = gameMapper.countTodayGamesByMemberId(memberId);
        if(todayPlayCount >= 3){
            throw new IllegalStateException("오늘 플레이 가능한 횟수(3회)를 모두 소모했습니다.");
        }

        Object correctCountValue = resultData.get("correctCount");
        if(!(correctCountValue instanceof Number)){
            throw new IllegalArgumentException("정답 개수 정보가 없습니다");
        }

        int correctCount = ((Number) correctCountValue).intValue();

        if(correctCount < 0 || correctCount > 3){
            throw new IllegalArgumentException("잘못된 정답 개수입니다");
        }
        gameMapper.insertGameHistory(resultData);

        Long gameHistoryId = ((Number) resultData.get("gameHistoryId")).longValue();

        int earnedPoint = correctCount * 10;

        if(earnedPoint > 0){
            ecoPointHistoryService.earnPoint(memberId,earnedPoint,"GAME",gameHistoryId);

        }
        return earnedPoint;
    }
}