package ezian.robert.mediasharebackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

        return uploadDir + "/" +filename;
    }

    public String generateVideoThumbnail(MultipartFile videoFile) throws IOException {
        String videoPath = store(videoFile);

        Path uploadPath = Paths.get(uploadDir);
        String thumbnailFilename = "thumb_" + UUID.randomUUID() + ".jpg";
        Path thumbnailPath = uploadPath.resolve(thumbnailFilename);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", uploadPath.resolve(videoPath).toString(),
                    "-ss", "00:00:01.000",
                    "-vframes", "1",
                    "-vf", "scale=320:240",
                    thumbnailPath.toString()
            );

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return thumbnailFilename;
            } else {
                return createPlaceholderThumbnail();
            }

        } catch (Exception e) {
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

        return placeholderFilename;
    }

    public void delete(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error
        }
    }
}