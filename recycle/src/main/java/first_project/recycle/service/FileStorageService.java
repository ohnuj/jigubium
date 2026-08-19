package first_project.recycle.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {

    // 업로드 가능한 이미지 확장자
    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");

    // DB에 저장할 이미지 접근 경로
    private static final String WEB_PATH_PREFIX = "/upload/";

    // 실제 이미지 파일이 저장될 서버 경로
    private final Path uploadDir;

    public FileStorageService(
            @Value("${recycle.upload.dir}") String uploadDir) {
        // 상대경로는 실행 위치에 따라 엉뚱한 곳에 저장될 수 있으므로 절대경로로 보관
        this.uploadDir = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    // 이미지 파일을 서버에 저장하고 이미지 경로를 반환
    public String store(MultipartFile file) {

        // 파일이 없으면 저장하지 않음
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null
                        ? ""
                        : file.getOriginalFilename());

        String extension = extractExtension(originalName);

        // 허용되지 않은 확장자 검사
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "이미지 파일만 업로드할 수 있습니다.");
        }


        // 파일명을 UUID로 변경
        //  - 같은 이름 파일을 올려도 덮어쓰지 않음
        //  - 원본 파일명이 그대로 노출되지 않음
        // 파일명 중복 방지
        String savedName =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        + "."
                        + extension;

        try {
            // 업로드 폴더가 없으면 생성
            Files.createDirectories(uploadDir);

            Path target = uploadDir.resolve(savedName);

            // 실제 파일 저장
            file.transferTo(target.toFile());

        } catch (IOException e) {
            throw new IllegalStateException(
                    "이미지를 저장하지 못했습니다.", e);
        }

        // DB에 저장할 이미지 경로 반환
        return WEB_PATH_PREFIX + savedName;
    }

//    서버에 저장된 사진 파일 삭제 (피드 삭제 시 파일이 쌓이지 않도록 정리)
    public boolean delete(String webPath) {

        if (webPath == null
                || !webPath.startsWith(WEB_PATH_PREFIX)) {
            return false;
        }

        String fileName =
                webPath.substring(WEB_PATH_PREFIX.length());

        // 잘못된 파일 경로 접근 방지
        if (fileName.isBlank()
                || fileName.contains("/")
                || fileName.contains("\\")
                || fileName.contains("..")) {
            return false;
        }

        try {
            Path target =
                    uploadDir.resolve(fileName).normalize();

            // 한 번 더 확인 : 계산된 경로가 정말 업로드 폴더 안인가?
            if (!target.startsWith(uploadDir)) {
                return false;
            }

            return Files.deleteIfExists(target);


        } catch (IOException e) {
            // 파일이 사용 중이라 못 지우는 경우 등 -> 글 삭제 자체는 성공해야 하므로 예외 X
            return false;
        }
    }

    /** 파일명에서 확장자만 소문자로 추출 (없으면 빈 문자열) */
    private String extractExtension(String fileName) {

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex < 0
                || dotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName
                .substring(dotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }
}