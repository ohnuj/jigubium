package first_project.recycle.service;


import first_project.recycle.dto.BoardDetailResponse;
import first_project.recycle.exception.ForbiddenException;
import first_project.recycle.exception.NotFoundException;
import first_project.recycle.repository.BoardLikeMapper;
import first_project.recycle.repository.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 좋아요
 */
@Service
@RequiredArgsConstructor
public class BoardLikeService {

    private final BoardLikeMapper boardLikeMapper;
    private final BoardMapper boardMapper;

    /**
     * 좋아요 등록 / 취소
     */
    @Transactional
    public void toggleLike(
            Long boardId,
            Long memberId) {

        BoardDetailResponse board =
                boardMapper.findById(boardId);

        if (board == null) {
            throw new NotFoundException(
                    "존재하지 않는 게시글입니다."
            );
        }

        // 본인 글 좋아요 금지
        if (board.getMemberId().equals(memberId)) {
            throw new ForbiddenException(
                    "본인의 게시글에는 좋아요를 누를 수 없습니다."
            );
        }

        int exists =
                boardLikeMapper.existsLike(
                        boardId,
                        memberId
                );

        // 이미 좋아요 했으면 취소
        if (exists > 0) {
            boardLikeMapper.deleteLike(
                    boardId,
                    memberId
            );
        }

        // 아직 안했으면 등록
        else {
            boardLikeMapper.insertLike(
                    boardId,
                    memberId
            );
        }
    }

    /**
     * 좋아요 개수
     */
    public int getLikeCount(Long boardId) {
        return boardLikeMapper.countByBoardId(boardId);
    }

    /**
     * 현재 사용자의 좋아요 여부
     */
    public boolean isLiked(
            Long boardId,
            Long memberId) {

        return boardLikeMapper.existsLike(
                boardId,
                memberId
        ) > 0;
    }

}
