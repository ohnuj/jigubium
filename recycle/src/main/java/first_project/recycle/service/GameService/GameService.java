package first_project.recycle.service.GameService;

import first_project.recycle.repository.GameMapper; // mapper -> repository로 변경
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class GameService {

    private final GameMapper gameMapper;

    public GameService(GameMapper gameMapper) {
        this.gameMapper = gameMapper;
    }

    public int getTodayPlayCount(Long memberId) {
        return gameMapper.countTodayGamesByMemberId(memberId);
    }

    public List<Map<String, String>> getRandomItems(int count) {
        return gameMapper.findRandomItems(count);
    }

    @Transactional
    public void saveGameResult(Map<String, Object> resultData) {
        gameMapper.insertGameHistory(resultData);
    }
}