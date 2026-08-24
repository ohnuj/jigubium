package first_project.recycle.domain.ecoLocationdto;

import lombok.Getter;

@Getter
public class Paging {

    private final int page;       // 현재 페이지
    private final int size;       // 한 페이지 게시글 수
    private final int totalCount; // 전체 게시글 수
    private final int totalPages; // 전체 페이지 수
    private final int offset;     // DB 조회 시작 위치

    public Paging(int page, int size, int totalCount) {

        this.size = size;
        this.totalCount = totalCount;

        // 전체 페이지 수 계산
        this.totalPages =
                (int) Math.ceil(
                        (double) totalCount / size
                );

        // 조회 결과가 없는 경우
        if (totalPages == 0) {
            this.page = 1;
            this.offset = 0;
            return;
        }

        // 1보다 작은 페이지 방지
        if (page < 1) {
            page = 1;
        }

        // 마지막 페이지보다 큰 페이지 방지
        if (page > totalPages) {
            page = totalPages;
        }

        this.page = page;

        // SQL LIMIT에서 사용할 시작 위치
        this.offset =
                (this.page - 1) * this.size;
    }
}