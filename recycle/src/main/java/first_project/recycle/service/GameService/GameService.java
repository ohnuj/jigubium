package first_project.recycle.service.GameService;

import first_project.recycle.repository.GameMapper; // mapper -> repository로 변경
import first_project.recycle.service.EcoPointHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service // 비즈니스 로직을 처리하는 서비스 계층 빈(Bean) 등록
public class GameService {

    // DB 접근을 담당하는 MyBatis 매퍼 주입
    private final GameMapper gameMapper;
    private final EcoPointHistoryService ecoPointHistoryService;

    // 생성자 주입을 통한 GameMapper 의존성 연결
    public GameService(GameMapper gameMapper,
                       EcoPointHistoryService ecoPointHistoryService) {

        this.gameMapper = gameMapper;
        this.ecoPointHistoryService = ecoPointHistoryService;
    }

    // [플레이 횟수 체크] 특정 회원의 오늘 게임 플레이 횟수를 DB에서 조회
    public int getTodayPlayCount(Long memberId) {
        return gameMapper.countTodayGamesByMemberId(memberId);
    }

    // [문제 출제] 게임에 사용할 쓰레기 아이템을 지정된 개수(count)만큼 랜덤 조회
    public List<Map<String, String>> getRandomItems(int count) {
        return gameMapper.findRandomItems(count);
    }

    // [결과 저장] 게임 점수 및 결과 데이터를 DB에 기록 (트랜잭션 보장)
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