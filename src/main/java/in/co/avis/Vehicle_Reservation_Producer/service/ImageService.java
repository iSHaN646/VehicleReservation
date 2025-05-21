package in.co.avis.Vehicle_Reservation_Producer.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class ImageService {

    @Autowired
    private AmazonS3 s3client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file) {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        try {
            File convertedFile = convertMultiPartToFile(file);
            s3client.putObject(new PutObjectRequest(bucketName, fileName, convertedFile));
            convertedFile.delete();
            return s3client.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage());
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}
