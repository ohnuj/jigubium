package first_project.recycle.service;

import first_project.recycle.dto.AdminDashBoardResponse;
import first_project.recycle.repository.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminMapper adminMapper;

    public AdminDashBoardResponse findDashBoardData(){
        return adminMapper.findDashBoardData();
    }
}
