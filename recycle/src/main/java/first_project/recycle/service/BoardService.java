package first_project.recycle.service;


import first_project.recycle.domain.Board;
import first_project.recycle.domain.BoardImage;
import first_project.recycle.domain.BoardType;
import first_project.recycle.domain.Paging;
import first_project.recycle.dto.BoardDetailResponse;
import first_project.recycle.dto.BoardListResponse;
import first_project.recycle.dto.BoardPageResponse;
import first_project.recycle.dto.BoardUpdateRequest;
import first_project.recycle.dto.BoardCreateRequest;
import first_project.recycle.repository.BoardImageMapper;
import first_project.recycle.repository.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BoardService {

    private static final int PAGE_SIZE = 10;

    private final BoardMapper boardMapper;
    private final BoardImageMapper boardImageMapper;
    private final FileStorageService fileStorageService;
    private final CommentService commentService;
    private final EcoPointHistoryService ecoPointHistoryService;

    // 메인페이지 최신 게시글 조회
    public List<BoardListResponse> getRecentBoards() {
        return boardMapper.findRecentBoards();
    }

    // 게시글 목록 + 검색 + 타입 + 페이징
    public BoardPageResponse getBoards(
            int page,
            String keyword,
            String searchType,
            BoardType boardType) {

        int totalCount =
                boardMapper.countBoards(
                        keyword,
                        searchType,
                        boardType
                );

        Paging paging = new Paging(page, PAGE_SIZE, totalCount);

        List<BoardListResponse> boards = boardMapper.findBoards(
                keyword,
                searchType,
                boardType,
                paging.getOffset(),
                paging.getSize()
        );
        return new BoardPageResponse(boards, paging);
    }

    /** 게시글 저장 후 생성된 boarId를 이용해 첨부 이미지 저장
     @Transactional : 글 저장 후 사진 저장에서 실패하면 글 INSERT도
     함께 취소(롤백)되어 반쪽짜리 데이터가 남지 않음
      */

    @Transactional
    public Long write(
                      Long memberId,
                      BoardCreateRequest request,
                      List<MultipartFile> images){

        //게시글 정보 생성
        Board board = new Board();

        board.setMemberId(memberId);
        board.setBoardType(request.getBoardType());
        board.setTitle(request.getTitle());
        board.setContent(request.getContent());

        // 게시글 저장 자동 생성 boarID 가져오기
        // useGeneratedkeys로 생성된 boardId
        boardMapper.insertBoard(board);

        Long boardId = board.getBoardId();

        // 2. 첨부 이미지가 있으면 순서대로 저장
        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                String imageUrl = fileStorageService.store(images.get(i));

                if (imageUrl != null) {
                    BoardImage boardImage = new BoardImage();
                    boardImage.setBoardId(boardId);
                    boardImage.setImageUrl(imageUrl);
                    boardImage.setSortOrder(i);

                    boardImageMapper.insertBoardImage(boardImage);
                }
            }

        }
        //게시글 작성시 100p 지급
        ecoPointHistoryService.earnPoint(memberId,100,"BOARD",boardId);
        return boardId;
    }

    /**
     * 게시글 상세 조회
     */
    public BoardDetailResponse getBoard(Long boardId) {

        // 게시글 조회
        BoardDetailResponse board =
                boardMapper.findById(boardId);

        if (board == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 게시글입니다.");
        }

        // 게시글 이미지 조회
        List<BoardImage> images =
                boardImageMapper.findByBoardId(boardId);

        board.setImages(images);

        return board;
    }

    /**
     * 게시글 수정
     * 작성자 본인인 경우에만 수정
     */
    public boolean updateBoard(
            Long boardId,
            Long memberId,
            BoardUpdateRequest request) {

        int result = boardMapper.updateBoard(
                boardId,
                memberId,
                request.getBoardType(),
                request.getTitle(),
                request.getContent()
        );

        return  result > 0;
    }

    /**
     * 게시글 삭제
     * 작성자 본인의 글만 삭제하며 이미지 파일도 함께 정리한다
     */
    @Transactional
    public boolean deleteBoard(
            Long boardId,
            Long memberId) {

        // 게시글 및 작성자 확인
        BoardDetailResponse board =
                boardMapper.findById(boardId);

        if (board == null
                || !Objects.equals(
                        board.getMemberId(),
                        memberId)) {
            return false;
        }

        // 실제 파일 삭제를 위해 이미지 목록을 먼저 조회
        List<BoardImage> images =
                boardImageMapper.findByBoardId(boardId);

        // 댓글 삭제
        commentService.deleteByBoardId(boardId);

        // 이미지 DB 데이터 먼저 삭제
        boardImageMapper.deleteByBoardId(boardId);

        // 게시글 삭제
        int result =
                boardMapper.deleteBoard(boardId, memberId);

        if (result == 0) {
            return false;
        }

        // 서버에 저장된 실제 이미지 파일 삭제
        for (BoardImage image : images) {
            fileStorageService.delete(image.getImageUrl());
        }

        return true;
    }





}
