package first_project.recycle.domain;


import lombok.Getter;

@Getter
public class Paging {
    private final int page; // 현재페이지
    private final int size; // 한 페이지 게시글 수
    private final int totalCount; // 전체 데이터 수
    private final int totalPages; // 전체 데이터 수
    private final int offset; // DB 조회 시작 위치

    public Paging(int page, int size, int totalCount) {

        this.size = size;
        this.totalCount = totalCount;

        // 전체 페이지 수 계산
        this.totalPages = (int) Math.ceil((double) totalCount / size);

        // 페이지 번호 보정
        if (page < 1) {
            page = 1;
        }

        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }
        this.page = page;

        // SQL LIMIT에서 사용할 시작 위치
        this.offset = (page - 1) * size;
    }

}
