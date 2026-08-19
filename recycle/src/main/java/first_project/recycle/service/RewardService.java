package first_project.recycle.service;

import first_project.recycle.domain.Reward;
import first_project.recycle.repository.RewardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService {
    private final RewardMapper rewardMapper;

    public List<Reward> findAll(){
        return rewardMapper.findAll();
    }

}
