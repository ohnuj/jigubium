package first_project.recycle.dto;

import first_project.recycle.domain.Paging;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BoardPageResponse {

    private List<BoardListResponse> boards;
    private Paging paging;
}
