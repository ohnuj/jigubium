package first_project.recycle.service;

import first_project.recycle.domain.Board;
import first_project.recycle.domain.BoardImage;
import first_project.recycle.domain.BoardType;
import first_project.recycle.domain.Paging;
import first_project.recycle.exception.ForbiddenException;
import first_project.recycle.exception.NotFoundException;
import first_project.recycle.dto.BoardDetailResponse;
import first_project.recycle.dto.BoardListResponse;
import first_project.recycle.dto.BoardPageResponse;
import first_project.recycle.dto.BoardUpdateRequest;
import first_project.recycle.dto.BoardCreateRequest;
import first_project.recycle.repository.BoardImageMapper;
import first_project.recycle.repository.BoardMapper;
import first_project.recycle.repository.MypageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private static final int PAGE_SIZE = 10;

    private final BoardMapper boardMapper;
    private final BoardImageMapper boardImageMapper;
    private final FileStorageService fileStorageService;
    private final CommentService commentService;
    private final EcoPointHistoryService ecoPointHistoryService;
    private final MypageMapper mypageMapper;
    private static final int MAX_IMAGE_COUNT = 5;

    // 메인페이지 최신 게시글 조회
    public List<BoardListResponse> getRecentBoards() {
        return boardMapper.findRecentBoards();
    }

    // 관리자용 최신 공지사항 3개
    public List<BoardListResponse> getRecentNotices(){
        return boardMapper.findRecentNotices();
    }

    // 관리자용 전체 공지사항
    public List<BoardListResponse> getAllNotices(){
        return boardMapper.findAllNotices();
    }

    // 관리자용 공지사항 상세조회
    public BoardDetailResponse getNotice(Long boardId){
        BoardDetailResponse notice = getBoardDetail(boardId);

        if (notice.getBoardType() != BoardType.NOTICE) {
            throw new NotFoundException("존재하지 않는 공지사항입니다.");
        }
        return notice;
    }

    // 관리자용 공지사항 작성
    @Transactional
    public Long writeNotice(Long memberId,BoardCreateRequest request){
        validateBoardRequest(request.getTitle(), request.getContent());

        Board board = new Board();

        board.setMemberId(memberId);

        // 서버에서 boardType NOTICE로 지정
        board.setBoardType(BoardType.NOTICE);

        board.setTitle(request.getTitle());
        board.setContent(request.getContent());

        boardMapper.insertBoard(board);
        return board.getBoardId();
    }
    // 관리자용 공지사항 수정
    @Transactional
    public boolean updateNotice(
            Long boardId,
            BoardUpdateRequest request) {
        // 공지사항 조회
        BoardDetailResponse board = boardMapper.findById(boardId);

        if (board == null) {
            throw new NotFoundException("존재하지 않는 공지사항입니다.");
        }

        // 공지사항이 아닌 글 차단
        if (board.getBoardType() != BoardType.NOTICE) {
            throw new ForbiddenException("공지사항만 수정 가능합니다.");
        }

        validateBoardRequest(request.getTitle(), request.getContent());

        int result = boardMapper.updateNotice(boardId, request.getTitle(), request.getContent());
        return result > 0;
        }

    // 관리자용 공지사항 삭제
    @Transactional
    public boolean deleteNotice(Long boardId) {

        // 공지사항 조회
        BoardDetailResponse board = boardMapper.findById(boardId);

        if (board == null) {
            throw new NotFoundException("존재하지 않는 공지사항입니다.");
        }

        // 공지사항이 아닌 글 차단
        if (board.getBoardType() != BoardType.NOTICE) {
            throw new ForbiddenException("공지사항만 삭제 가능합니다.");
        }

        // 이미지 첨부 시 조회
        List<BoardImage> images = boardImageMapper.findByBoardId(boardId);
        // 연결 댓글 삭제
        commentService.deleteByBoardId(boardId);
        // 이미지 DB데이터 삭제
        boardImageMapper.deleteByBoardId(boardId);
        // 공지사항 삭제
        int result = boardMapper.deleteNotice(boardId);
        if(result == 0){
            return false;
        }
        //트랜잭션 정상 커밋 후 실제 파일 삭제
        for (BoardImage image : images) {
            deleteFileAfterCommit(image.getImageUrl());
        }
        return true;
    }

    // 게시글 목록 + 검색 + 타입 + 페이징 + 내 글 보기
    public BoardPageResponse getBoards(
            int page,
            String keyword,
            String searchType,
            BoardType boardType,
            String sort,
            Long memberId) {

        int totalCount =
                boardMapper.countBoards(
                        keyword,
                        searchType,
                        boardType,
                        memberId
                );

        Paging paging =
                new Paging(
                        page,
                        PAGE_SIZE,
                        totalCount
                );

        List<BoardListResponse> boards =
                boardMapper.findBoards(
                        keyword,
                        searchType,
                        boardType,
                        sort,
                        memberId,
                        paging.getOffset(),
                        paging.getSize()
                );

        return new BoardPageResponse(
                boards,
                paging
        );
    }

    /** 게시글 저장 후 생성된 boarId를 이용해 첨부 이미지 저장
     @Transactional : 글 저장 후 사진 저장에서 실패하면 글 INSERT도
     함께 취소(롤백)되어 반쪽짜리 데이터가 남지 않음
      */

    @Transactional
    public Long write(
            Long memberId,
            BoardCreateRequest request,
            List<MultipartFile> images) {

        validateBoardRequest(
                request.getTitle(),
                request.getContent()
        );

        // 일반 사용자는 NOTICE 작성 금지
        if (request.getBoardType() == BoardType.NOTICE) {
            throw new ForbiddenException(
                    "공지사항은 관리자만 작성할 수 있습니다."
            );
        }

        // 이미지 최대 5장 검사
        if (images != null) {

            long imageCount =
                    images.stream()
                            .filter(image ->
                                    image != null
                                            && !image.isEmpty())
                            .count();

            if (imageCount > MAX_IMAGE_COUNT) {
                throw new IllegalArgumentException(
                        "이미지는 최대 5장까지 업로드할 수 있습니다."
                );
            }
        }

        // 게시글 정보 생성
        Board board = new Board();

        board.setMemberId(memberId);
        board.setBoardType(request.getBoardType());
        board.setTitle(request.getTitle());
        board.setContent(request.getContent());

        // 게시글 저장
        boardMapper.insertBoard(board);

        Long boardId = board.getBoardId();

        // 첨부 이미지 저장
        if (images != null) {

            for (int i = 0; i < images.size(); i++) {

                String imageUrl =
                        fileStorageService.store(images.get(i));

                if (imageUrl != null) {

                    deleteFileAfterRollback(imageUrl);

                    BoardImage boardImage =
                            new BoardImage();

                    boardImage.setBoardId(boardId);
                    boardImage.setImageUrl(imageUrl);
                    boardImage.setSortOrder(i);

                    boardImageMapper.insertBoardImage(
                            boardImage
                    );
                }
            }
        }

        // 게시글 작성 시 100p 지급
        ecoPointHistoryService.earnPoint(
                memberId,
                100,
                "BOARD",
                boardId
        );

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
            throw new NotFoundException(
                    "존재하지 않는 게시글입니다.");
        }

        // 게시글 이미지 조회
        List<BoardImage> images =
                boardImageMapper.findByBoardId(boardId);

        board.setImages(images);

        return board;
    }
    /**
     * 게시글 수정 화면 조회
     * 게시글 존재 여부 + 작성자 본인 여부 + 공지사항 여부 확인
     */
    public BoardDetailResponse getBoardForEdit(Long boardId, Long memberId) {

        // 기존 getBoard() 사용
        // 게시글이 없으면 여기서 NotFoundException 발생
        BoardDetailResponse board = getBoard(boardId);

        // 작성자 본인 여부 확인
        if (!Objects.equals(board.getMemberId(), memberId)) {
            throw new ForbiddenException("게시글을 수정할 권한이 없습니다.");
        }

        // 공지사항은 일반 게시글 수정 URL로 접근 차단
        if (board.getBoardType() == BoardType.NOTICE) {
            throw new ForbiddenException("공지사항은 관리자 페이지에서만 수정할 수 있습니다.");
        }

        return board;
    }
    public BoardDetailResponse getBoardDetail(Long boardId) {

        BoardDetailResponse board = boardMapper.findById(boardId);

        if (board == null) {
            throw new NotFoundException("존재하지 않는 게시글입니다.");
        }

        // 이미지 조회
        List<BoardImage> images = boardImageMapper.findByBoardId(boardId);

        board.setImages(images);

        return board;
    }

    public void increaseViewCount(Long boardId) {
        boardMapper.increaseViewCount(boardId);
    }

    /**
     * 게시글 수정
     * 작성자 본인인 경우에만 수정
     */

    @Transactional
    public void updateBoard(
            Long boardId,
            Long memberId,
            BoardUpdateRequest request,
            List<MultipartFile> images) {

        // 게시글 존재 여부 확인
        BoardDetailResponse board = boardMapper.findById(boardId);

        if (board == null) {
            throw new NotFoundException("존재하지 않는 게시글입니다.");
        }

        // 작성자 확인
        if (!Objects.equals(
                board.getMemberId(),
                memberId)) {
            throw new ForbiddenException("게시글을 수정할 권한이 없습니다.");
        }
        // 공지사항은 관리자 기능으로만 수정
        if (board.getBoardType() == BoardType.NOTICE) {
            throw new ForbiddenException("공지사항은 관리자 페이지에서만 수정할 수 있습니다.");
        }

        // 일반 사용자의 NOTICE 변경 요청 차단
        if (request.getBoardType() == BoardType.NOTICE) {
            throw new ForbiddenException("공지사항으로 변경할 수 없습니다.");
        }

        validateBoardRequest(
                request.getTitle(),
                request.getContent()
        );

        // 현재 저장된 이미지
        List<BoardImage> currentImages =
                boardImageMapper.findByBoardId(boardId);

// 새 이미지 개수
        long newImageCount = 0;

        if (images != null) {
            newImageCount =
                    images.stream()
                            .filter(image ->
                                    image != null
                                            && !image.isEmpty())
                            .count();
        }

// 기존 이미지 + 새 이미지 최대 5장 검사
        if (currentImages.size() + newImageCount
                > MAX_IMAGE_COUNT) {

            throw new IllegalArgumentException(
                    "이미지는 최대 5장까지 업로드할 수 있습니다."
            );
        }

// 검증이 끝난 후 게시글 수정
      boardMapper.updateBoard(
                boardId,
                memberId,
                request.getBoardType(),
                request.getTitle(),
                request.getContent());


       Integer maxSortOrder =
               boardImageMapper.findMaxSortOrder(boardId);

        int startOrder =
                maxSortOrder == null ? 0 : maxSortOrder + 1;



        // 새 이미지 추가
        if (images != null) {

            for (int i = 0; i < images.size(); i++) {

                String imageUrl =
                        fileStorageService.store(images.get(i));

                if (imageUrl != null) {

                    deleteFileAfterRollback(imageUrl);

                    BoardImage boardImage =
                            new BoardImage();

                    boardImage.setBoardId(boardId);
                    boardImage.setImageUrl(imageUrl);
                    boardImage.setSortOrder(
                            startOrder + i
                    );

                    boardImageMapper.insertBoardImage(
                            boardImage
                    );
                }
            }
        }
    }

    /**
     * 게시글 삭제
     * 작성자 본인의 글만 삭제하며 이미지 파일도 함께 정리한다
     */
    @Transactional
    public void deleteBoard(
            Long boardId,
            Long memberId) {

        // 게시글 및 작성자 확인
        BoardDetailResponse board =
                boardMapper.findById(boardId);

       // 게시글 없음
        if (board == null) {
            throw new NotFoundException(
                    "존재하지 않는 게시글입니다.");
        }

        // 작성자 아님
        if (!Objects.equals(
                board.getMemberId(),
                memberId)) {
            throw new ForbiddenException(
                    "게시글을 삭제할 권한이 없습니다.");
        }

        // 공지사항은 관리자 기능으로만 삭제
        if (board.getBoardType() == BoardType.NOTICE) {
            throw new ForbiddenException(
                    "공지사항은 관리자 페이지에서만 삭제할 수 있습니다.");
        }

        // 실제 파일 삭제를 위해 이미지 목록을 먼저 조회
        List<BoardImage> images =
                boardImageMapper.findByBoardId(boardId);

        // 댓글 삭제
        commentService.deleteByBoardId(boardId);

        // 이미지 DB 데이터 먼저 삭제
        boardImageMapper.deleteByBoardId(boardId);

        // 게시글 삭제
        boardMapper.deleteBoard(boardId, memberId);



        // DB 트랜잭션 커밋 후 실제 이미지 파일 삭제
        for (BoardImage image : images) {

            deleteFileAfterCommit(
                    image.getImageUrl()
            );
        }
    }

    /**
     * 게시글 기존 이미지 개별 삭제
     */
    @Transactional
    public void deleteBoardImage(
            Long boardId,
            Long imageId,
            Long memberId) {

        // 게시글 작성자 확인
        BoardDetailResponse board =
                boardMapper.findById(boardId);

        // 1. 게시글 존재 여부 확인
        if (board == null) {
            throw new NotFoundException(
                    "존재하지 않는 게시글입니다."
            );
        }

        // 2. 게시글 작성자 확인
        if (!Objects.equals(
                board.getMemberId(),
                memberId)) {

            throw new ForbiddenException(
                    "이미지를 삭제할 권한이 없습니다."
            );
        }
        // 공지사항 이미지 삭제 차단
        if (board.getBoardType() == BoardType.NOTICE) {
            throw new ForbiddenException(
                    "공지사항 이미지는 관리자 페이지에서만 삭제할 수 있습니다."
            );
        }

        // 이미지 존재 여부
        BoardImage image =
                boardImageMapper.findById(imageId);

        if (image == null) {
            throw new NotFoundException(
                    "존재하지 않는 이미지입니다."
            );
        }

        // 4. 해당 게시글에 속한 이미지인지 확인
        if (!Objects.equals(
                image.getBoardId(),
                boardId)) {

            throw new NotFoundException(
                    "해당 게시글의 이미지가 아닙니다."
            );
        }

        // DB 이미지 삭제
        boardImageMapper.deleteImage(imageId, boardId);



        // DB 트랜잭션이 정상 커밋된 뒤
        // 실제 이미지 파일 삭제
        deleteFileAfterCommit(
                image.getImageUrl()
        );

    }

    // 마이페이지 활동 조회 - 작성한 총 게시글 수
    public int getBoardCount(Long memberId) {
        return mypageMapper.countBoardsById(memberId);
    }
    public BoardListResponse getPreviousBoard(
            Long boardId,
            BoardType boardType) {

        return boardMapper.findPreviousBoard(
                boardId,
                boardType
        );
    }

    public BoardListResponse getNextBoard(
            Long boardId,
            BoardType boardType) {

        return boardMapper.findNextBoard(
                boardId,
                boardType
        );
    }

    /**
     * 새로 저장한 파일은 DB 트랜잭션이 롤백되면 삭제
     */
    private void deleteFileAfterRollback(String imageUrl) {

        if (imageUrl == null) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCompletion(int status) {

                        if (status == STATUS_ROLLED_BACK) {

                            boolean deleted =
                                    fileStorageService.delete(imageUrl);

                            if (!deleted) {
                                log.warn(
                                        "트랜잭션 롤백 후 이미지 파일 삭제 실패: {}",
                                        imageUrl
                                );
                            }
                        }
                    }
                }
        );
    }


    /**
     * DB 삭제가 정상 커밋된 후 실제 파일 삭제
     */
    private void deleteFileAfterCommit(String imageUrl) {

        if (imageUrl == null) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        boolean deleted =
                                fileStorageService.delete(imageUrl);

                        if (!deleted) {
                            log.warn(
                                    "DB 삭제 후 이미지 파일 삭제 실패: {}",
                                    imageUrl
                            );
                        }
                    }
                }
        );
    }
    private void validateBoardRequest(
            String title,
            String content) {

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "제목을 입력해주세요."
            );
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "내용을 입력해주세요."
            );
        }

        if (title.length() > 255) {
            throw new IllegalArgumentException(
                    "제목은 255자 이하로 입력해주세요."
            );
        }
    }

    public int getTotalBoardCount() {
        return boardMapper.countBoards(
                null,
                null,
                null,
                null
        );
    }





}
