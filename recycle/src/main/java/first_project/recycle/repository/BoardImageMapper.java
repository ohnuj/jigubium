package first_project.recycle.repository;


import first_project.recycle.domain.BoardImage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BoardImageMapper {
//    사진등록
    int insertBoardImage(BoardImage boardImage);
}
