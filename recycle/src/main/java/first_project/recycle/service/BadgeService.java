package first_project.recycle.service;

import first_project.recycle.domain.Badge;
import first_project.recycle.repository.BadgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BadgeService {
    private final BadgeMapper badgeMapper;


    public Badge findCurrentBadge(int totalPoint) {
        return badgeMapper.findCurrentBadge(totalPoint);
    }


}
