package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.entities.Professor;
import it.polito.ai.virtuallabs.entities.Student;
import it.polito.ai.virtuallabs.repositories.ProfessorRepository;
import it.polito.ai.virtuallabs.repositories.StudentRepository;
import it.polito.ai.virtuallabs.service.exceptions.TeamServiceException;
import it.polito.ai.virtuallabs.service.exceptions.images.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImageUploadServiceImpl implements ImageUploadService{

    private Tika tika;
    private Set<String> extensions;

    @Value("${app.path.images}")
    private String baseImagePath;
    @Value("${app.default.image}")
    private String defaultImg;

    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final EntityGetter getter;

    @Autowired
    public ImageUploadServiceImpl(StudentRepository studentRepository, ProfessorRepository professorRepository, EntityGetter getter) {
        this.studentRepository = studentRepository;
        this.professorRepository = professorRepository;
        this.getter = getter;

        tika = new Tika();
        extensions = new HashSet<>(Arrays.asList("jpg", "jpeg", "png"));
    }

    @Override
    public String store(MultipartFile image, String userId){
        String type;
        String extension;
        try {
            String[] detection = tika.detect(image.getBytes()).split("/");
            type = detection[0];
            extension = detection[1];

            if (!type.equals("image"))
                throw new UnsupportedMediaTypeException(type);
            if (!extensions.contains(extension))
                throw new ImageExtensionUnsupported(extension);
        } catch (IOException e) {
            throw new ImageDetectionException();
        }
        
        String imageName = String.format("%s-profile.%s", userId, extension);

        storeOnDisk(image, imageName);
        return storeRefOnDb(imageName, userId);
    }

    @Override
    public byte[] getImage(String userId) {
        char prefix = userId.toLowerCase().toCharArray()[0];
        String photoName;
        switch (prefix){
            case 's':
                Student s = getter.getStudent(userId);
                photoName = s.getPhotoName() == null ? defaultImg : s.getPhotoName();
                break;
            case 'd':
                Professor p = getter.getProfessor(userId);
                photoName = p.getPhotoName() == null ? defaultImg : p.getPhotoName();
                break;
            default:
                throw new TeamServiceException(String.format("User %s not found", userId));
        }

        String path = String.format("%s/%s", baseImagePath, photoName);

        try {
            File image = new File(path);
            return FileUtils.readFileToByteArray(image);
        } catch (IOException e) {
            throw new ImageConversionException();
        }
    }

    private String storeRefOnDb(String imageName, String userId) {
        if(userId.toLowerCase().startsWith("d")){
            Professor p = getter.getProfessor(userId);
            p.setPhotoName(imageName);
            return String.format("http://localhost:8080/API/professors/%s/photo", userId);
        }else if (userId.toLowerCase().startsWith("s")){
            Student s = getter.getStudent(userId);
            s.setPhotoName(imageName);
            return String.format("http://localhost:8080/API/students/%s/photo", userId);
        }else{
            throw new IllegalStateException("User not found");
        }
    }

    private void storeOnDisk(MultipartFile image, String imageName) {
        try {
            Path copyLocation = Paths
                    .get(baseImagePath + File.separator + StringUtils.cleanPath(imageName));
            Files.copy(image.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ImageStorageException(imageName);
        }
    }
}
