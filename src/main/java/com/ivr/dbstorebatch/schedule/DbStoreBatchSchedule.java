package com.ivr.dbstorebatch.schedule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ivr.dbstorebatch.datasource.DatabaseUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DbStoreBatchSchedule {

    @Value("${dbstore.batch.file.dir}")
    private String directory;

    @Value("${dbstore.batch.file.extension}")
    private String extension;

    @Value("${dbstore.batch.file.old.sec}")
    private Long keepSecond;

    @Autowired
    private DatabaseUtil databaseUtil;

    @Scheduled(cron = "${dbstore.batch.cron}")
    public void doStore() throws IOException {

        File dir = new File(getSlashEndedString(directory));

        // 디렉토리가 존재하지 않는 경우 종료
        if (!dir.exists()) {
            return;
        }

        // 디렉토리가 아닌 경우 Exception 발생
        if (!dir.isDirectory()) {
            throw new IOException(directory + " is not a directory");
        }

        // DB 삽입 대상 파일 필터
        List<File> targetFiles = filterFile(List.of(dir.listFiles()));

        databaseUtil.executeQuery(
                targetFiles.stream().map(file -> {
                    String data = null;

                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = fis.readAllBytes();
                        data = new String(buffer);
                    } catch (IOException e) {
                        log.error(e.toString());
                        return null;
                    }

                    moveFile(file, new File(getSlashEndedString(dir.getAbsolutePath() + "/backup")));
                    return data;
                }).filter(file -> !Objects.isNull(file)).toList());
    }

    /**
     * 슬래쉬로 끝나는 문자열을 반환
     * 
     * @param str
     * @return
     */
    private String getSlashEndedString(String str) {
        return str.endsWith("/") ? str : str + "/";
    }

    /**
     * 파일 보관 기간보다 오래된 파일 필터링
     * 
     * @param files
     * @return
     */
    private List<File> filterFile(List<File> files) {
        return files.stream().filter(file -> {
            long now = Calendar.getInstance().getTimeInMillis();
            return extension.equals(getFileExtension(file)) && (now - file.lastModified() >= keepSecond * 1_000);
        }).toList();
    }

    /**
     * 파일 확장자 반환
     * 
     * @param file
     * @return
     */
    private String getFileExtension(File file) {
        List<String> filename = List.of(file.getName().split("\\."));

        if (filename.size() == 0) {
            return file.getName();
        }

        return filename.get(filename.size() - 1);
    }

    /**
     * 파일 이동
     * 
     * @param srcFile
     * @param destDir
     */
    private void moveFile(File srcFile, File destDir) {
        try {
            FileUtils.moveFileToDirectory(srcFile, destDir, true);
        } catch (IOException e) {
            log.error(e.toString());
        }
    }
}
