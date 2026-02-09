package ezian.robert.mediasharebackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String store(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uploadDir + "/" + filename;
    }

    public String generateVideoThumbnail(String videoPath) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        String thumbnailFilename = "thumb_" + UUID.randomUUID() + ".jpg";
        Path thumbnailPath = uploadPath.resolve(thumbnailFilename);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", videoPath,
                    "-ss", "00:00:01.000",
                    "-vframes", "1",
                    "-vf", "scale=320:240",
                    thumbnailPath.toString()
            );

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return uploadDir + "/" + thumbnailFilename;
            } else {
                logger.error("FFmpeg failed with exit code: {}", exitCode);
                return createPlaceholderThumbnail();
            }

        } catch (Exception e) {
            logger.error("Error generating video thumbnail for: {}", videoPath, e);
            return createPlaceholderThumbnail();
        }
    }

    private String createPlaceholderThumbnail() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        String placeholderFilename = "placeholder_" + UUID.randomUUID() + ".jpg";
        Path placeholderPath = uploadPath.resolve(placeholderFilename);

        BufferedImage placeholder = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, 320, 240);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("No Thumbnail", 80, 120);
        g2d.dispose();

        ImageIO.write(placeholder, "jpg", placeholderPath.toFile());

        return uploadDir + "/" + placeholderFilename;
    }

    public void delete(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            logger.error("Error deleting file: {}", filePath, e);
        }
    }
}