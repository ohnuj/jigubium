package first_project.recycle.repository;


import first_project.recycle.domain.BoardImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardImageMapper {
//    사진등록
    int insertBoardImage(BoardImage boardImage);

    // 상세 게시글 이미지 가져오기
    List<BoardImage> findByBoardId(Long boardId);

    // 이미지 삭제
    int deleteByBoardId(Long boardId);

    int deleteImage(
            @Param("imageId") Long imageId,
            @Param("boardId") Long boardId
    );

    BoardImage findById(Long imageId);

    Integer findMaxSortOrder(Long boardId);
}
