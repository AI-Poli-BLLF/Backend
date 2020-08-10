package it.polito.ai.virtuallabs.service;

import it.polito.ai.virtuallabs.dtos.images.ImageModel;
import it.polito.ai.virtuallabs.entities.Professor;
import it.polito.ai.virtuallabs.entities.Student;
import it.polito.ai.virtuallabs.repositories.ProfessorRepository;
import it.polito.ai.virtuallabs.repositories.StudentRepository;
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

    private final long MAX_SIZE = (1<<20)*3;
    private Tika tika;
    private Set<String> extensions;

    @Value("${app.path.images}")
    private String baseImagePath;

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
        storeRefOnDb(imageName, userId);

        return String.format("http://localhost:8080/students/%s/photo", userId);
    }

    @Override
    public ImageModel getImage(String userId) {
        Student s = getter.getStudent(userId);
        String path = String.format("%s/%s", baseImagePath, s.getPhotoName());

        try {
            File image = new File(path);
            byte[] imageBytes = FileUtils.readFileToByteArray(image);
            return new ImageModel(image.getName(), tika.detect(image), imageBytes);
        } catch (IOException | NullPointerException e) {
            throw new ImageConversionException();
        }
    }

    private void storeRefOnDb(String imageName, String userId) {
        if(userId.toLowerCase().startsWith("d")){
            Professor p = getter.getProfessor(userId);
            p.setPhotoName(imageName);
        }else if (userId.toLowerCase().startsWith("s")){
            Student s = getter.getStudent(userId);
            s.setPhotoName(imageName);
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

    private String generateRandomName() {
        String upperCaseLetters = RandomStringUtils.random(3, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(3, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(3);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
                .concat(numbers);
        List<Character> pwdChars = combinedChars.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        return pwdChars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
