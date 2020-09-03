package it.polito.ai.virtuallabs.service.implementations;

import it.polito.ai.virtuallabs.entities.Assignment;
import it.polito.ai.virtuallabs.entities.Draft;
import it.polito.ai.virtuallabs.entities.Professor;
import it.polito.ai.virtuallabs.entities.Student;
import it.polito.ai.virtuallabs.repositories.AssignmentRepository;
import it.polito.ai.virtuallabs.repositories.DraftRepository;
import it.polito.ai.virtuallabs.repositories.ProfessorRepository;
import it.polito.ai.virtuallabs.repositories.StudentRepository;
import it.polito.ai.virtuallabs.service.EntityGetter;
import it.polito.ai.virtuallabs.service.ImageUploadService;
import it.polito.ai.virtuallabs.service.exceptions.TeamServiceException;
import it.polito.ai.virtuallabs.service.exceptions.images.*;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class ImageUploadServiceImpl implements ImageUploadService {

    private Tika tika;
    private Set<String> extensions;

    @Value("${app.path.images}")
    private String baseImagePath;
    @Value("${app.default.image}")
    private String defaultImg;
    @Value("src/main/resources/static/assignments/assignmentImages")
    private String baseAssignmentImagePath;
    @Value("src/main/resources/static/assignments/draftImages")
    private String baseDraftImagePath;

    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final AssignmentRepository assignmentRepository;
    private final DraftRepository draftRepository;
    private final EntityGetter getter;

    @Autowired
    public ImageUploadServiceImpl(StudentRepository studentRepository, ProfessorRepository professorRepository, AssignmentRepository assignmentRepository, DraftRepository draftRepository, EntityGetter getter) {
        this.studentRepository = studentRepository;
        this.professorRepository = professorRepository;
        this.assignmentRepository = assignmentRepository;
        this.draftRepository = draftRepository;
        this.getter = getter;

        tika = new Tika();
        extensions = new HashSet<>(Arrays.asList("jpg", "jpeg", "png"));
    }

    @PreAuthorize("@securityApiAuth.isMe(#userId)")
    @Override
    public String store(MultipartFile image, String userId) {
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

    @PreAuthorize("@securityApiAuth.isMe(#professorId)")
    @Override
    public String storeAssignmentImage(MultipartFile image, String professorId, String assigmentId) {
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
        String imageName = String.format("%s-assignment.%s", assigmentId, extension);
        storeAssignmentOnDisk(image, imageName);
        return storeAssignmentOnDb(imageName, assigmentId);
    }

    @Override
    public String storeDraftImage(MultipartFile image, String draftId) {
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
        String imageName = String.format("%s-draft.%s", draftId, extension);
        storeDraftOnDisk(image, imageName);
        return storeDraftOnDb(imageName, draftId);
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

    @Override
    public byte[] getAssignmentImage(String assignmentId) {
        String photoName;
        Assignment assignment = getter.getAssignment(assignmentId);
        photoName = assignment.getPhotoName() == null ? defaultImg : assignment.getPhotoName();
        String path = String.format("%s/%s", baseAssignmentImagePath, photoName);
        try {
            File image = new File(path);
            return FileUtils.readFileToByteArray(image);
        }catch (IOException e){
            throw new ImageConversionException();
        }
    }

    @Override
    public byte[] getDraftImage(String draftId) {
        String photoName;
        Draft draft = getter.getDraft(draftId);
        photoName = draft.getPhotoName() == null ? defaultImg : draft.getPhotoName();
        String path = String.format("%s/%s", baseDraftImagePath, photoName);
        try {
            File image = new File(path);
            return FileUtils.readFileToByteArray(image);
        }catch (IOException e){
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

    private String storeAssignmentOnDb(String imageName, String assignmentId){
        Assignment assignment = getter.getAssignment(assignmentId);
        assignment.setPhotoName(imageName);
        return String.format("http://localhost8080/API/professors/%s/photo", assignmentId);
    }

    //non mi piace la stringa di ritorno
    private String storeDraftOnDb(String imageName, String draftId){
        Draft draft = getter.getDraft(draftId);
        draft.setPhotoName(imageName);
        return String.format("http://localhost8080/API/%s/photo", draftId); //solo di questa riga non sono sicuro
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

    private void storeAssignmentOnDisk(MultipartFile image, String imageName) {
        try{
            Path copyLocation = Paths
                    .get(baseAssignmentImagePath + File.separator + StringUtils.cleanPath(imageName));
            Files.copy(image.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e){
            e.printStackTrace();
            throw new ImageStorageException(imageName);
        }
    }

    private void storeDraftOnDisk(MultipartFile image, String imageName) {
        try{
            Path copyLocation = Paths
                    .get(baseDraftImagePath + File.separator + StringUtils.cleanPath(imageName));
            Files.copy(image.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e){
            e.printStackTrace();
            throw new ImageStorageException(imageName);
        }
    }

}
