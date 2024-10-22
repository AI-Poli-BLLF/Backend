package it.polito.ai.virtuallabs.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polito.ai.virtuallabs.controllers.utility.ModelHelper;
import it.polito.ai.virtuallabs.controllers.utility.TransactionChain;
import it.polito.ai.virtuallabs.dtos.*;
import it.polito.ai.virtuallabs.dtos.CourseDTO;
import it.polito.ai.virtuallabs.dtos.ProfessorDTO;
import it.polito.ai.virtuallabs.dtos.StudentDTO;
import it.polito.ai.virtuallabs.dtos.TeamDTO;
import it.polito.ai.virtuallabs.dtos.tokens.TokenDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMConfigDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMInstanceDTO;
import it.polito.ai.virtuallabs.dtos.vms.VMModelDTO;
import it.polito.ai.virtuallabs.service.*;
import it.polito.ai.virtuallabs.service.exceptions.*;
import it.polito.ai.virtuallabs.service.exceptions.assignments.AssignmentServiceException;
import it.polito.ai.virtuallabs.service.exceptions.assignments.DraftNotFoundException;
import it.polito.ai.virtuallabs.service.exceptions.images.ImageServiceException;
import it.polito.ai.virtuallabs.service.exceptions.vms.VMServiceException;
import lombok.Synchronized;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/courses")
public class CourseController {

    @Autowired
    private TeamService teamService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private VMService vmService;
    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private TransactionChain transactionChain;

    @GetMapping({"", "/"})
    private List<CourseDTO> all(){
        return teamService.getAllCourses().stream().map(ModelHelper::enrich).collect(Collectors.toList());
    }

    @GetMapping("/{courseName}")
    private CourseDTO getOne(@PathVariable String courseName){
        try{
            return ModelHelper.enrich(teamService.getCourse(courseName).get());
        }catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Course not found: %s", courseName));
        }
    }

    @DeleteMapping("/{courseName}")
    private void deleteCourse(@PathVariable String courseName){
        try{
            teamService.deleteCourse(courseName);
        }catch (TeamServiceException | VMServiceException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping({"/", ""})
    @ResponseStatus(value = HttpStatus.CREATED)
    private CourseDTO addCourse(@RequestBody @Valid ModelHelper.AddCourseRequest courseRequest){
        try{
            return ModelHelper.enrich(transactionChain.updateCourseWithModel(courseRequest));
        }catch (TeamServiceException | VMServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PutMapping("/{oldCourseName}")
    private CourseDTO editCourse(@PathVariable String oldCourseName, @RequestBody @Valid ModelHelper.AddCourseRequest courseRequest){
        try{
            return ModelHelper.enrich(transactionChain.updateCourseWithModel(oldCourseName, courseRequest));
        }catch (TeamServiceException | VMServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/enrolled")
    private List<StudentDTO> enrolledStudents(@PathVariable String courseName){
        try {
            return teamService.getEnrolledStudents(courseName).stream()
                    .map(ModelHelper::enrich).collect(Collectors.toList());
        }catch (CourseNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{courseName}/enrolled/{studentId}")
    @Synchronized
    private void deleteStudentFromCourse(@PathVariable String courseName, @PathVariable String studentId){
        try {
            teamService.deleteStudentFromCourse(courseName, studentId);
        }catch (TeamServiceException e){
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("/{courseName}/enrollOne")
    @ResponseStatus(value = HttpStatus.CREATED)
    private void enrollOneStudent(@PathVariable String courseName, @RequestBody @Valid String studentID){
        try {
            if(!teamService.addStudentToCourse(studentID, courseName))
                throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Student <%s> is already enrolled", studentID));
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }


    @PostMapping("/{courseName}/enroll-many")
    @ResponseStatus(value = HttpStatus.CREATED)
    private void enrollStudents(@PathVariable String courseName, @RequestParam("file") MultipartFile file){
        try {
            Tika tika = new Tika();

            String type = tika.detect(file.getBytes());
            String contentType = file.getContentType();
            System.out.println(type);
            if(!type.equals("text/plain") || contentType==null ||
                    (!contentType.equals("text/csv") && !contentType.equals("application/vnd.ms-excel")))
                throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "text/csv file required");
        }catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An error occurred while reading the file, please try again later");
        }

        try(Reader r = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            teamService.enrollByCsv(r, courseName);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An error occurred while reading the file, please try again later");
        } catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("{courseName}/proposeTeam")
    @ResponseStatus(value = HttpStatus.CREATED)
    private TeamDTO proposeTeam(@PathVariable String courseName, @RequestBody @Valid ModelHelper.TeamProposal proposal){
        try{
            return transactionChain.proposeTeamAndNotify(courseName, proposal);
        }catch (TeamServiceException e){
            String message = e.getMessage() == null ? "Error during team proposal" : e.getMessage();
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }
    }

    @PutMapping("{courseName}/enable")
    private void enableCourse(@PathVariable String courseName){
        try{
            teamService.enableCourse(courseName);
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PutMapping("{courseName}/disable")
    private void disableCourse(@PathVariable String courseName){
        try{
            teamService.disableCourse(courseName);
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams")
    private List<TeamDTO> getTeamsForCourse(@PathVariable String courseName){
        try {
            return teamService.getTeamsForCourse(courseName)
                    .stream().map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/active")
    private List<TeamDTO> getActiveTeamsForCourse(@PathVariable String courseName){
        try {
            return teamService.getActiveTeamsForCourse(courseName)
                    .stream().map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}")
    private TeamDTO getTeamById(@PathVariable String courseName, @PathVariable String teamId){
        try{
            Long id = Long.valueOf(teamId);
            return ModelHelper.enrich(teamService.getTeam(courseName, id).get());
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team");
        }catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, new TeamNotFoundException(Long.valueOf(teamId)).getMessage());
        }
    }

    @DeleteMapping("/{courseName}/teams/{teamId}")
    private void deleteTeam(@PathVariable String courseName, @PathVariable Long teamId){
        try{
            teamService.deleteTeam(courseName, teamId);
        }catch (TeamNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }catch (TeamNotBelongToCourseException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/members")
    private List<StudentDTO> getMembers(@PathVariable String courseName, @PathVariable String teamId){
        try{
            Long id = Long.valueOf(teamId);
            return teamService.getMembers(courseName, id).stream()
                    .map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team");
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/activeMembers")
    private List<StudentDTO> getActiveMembers(@PathVariable String courseName, @PathVariable String teamId){
        try{
            Long id = Long.valueOf(teamId);
            List<String> pendingMembers = notificationService
                    .getPendingMemberIds(id);
            return teamService.getMembers(courseName, id)
                    .stream()
                    .filter(m -> !pendingMembers.contains(m.getId()))
                    .map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team");
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/pendingMembers")
    private List<StudentDTO> getPendingMembers(@PathVariable String courseName, @PathVariable String teamId){
        try{
            List<StudentDTO> activeMembers = getActiveMembers(courseName, teamId);
            List<StudentDTO> pendingMembers = getMembers(courseName, teamId);
            pendingMembers.removeAll(activeMembers);
            return pendingMembers;
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team");
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/proposer")
    private StudentDTO getProposer(@PathVariable String courseName, @PathVariable String teamId){
        try{
            Long id = Long.valueOf(teamId);
            return ModelHelper.enrich(teamService.getProposer(courseName, id));
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team");
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/pendingMembers/{memberId}/token")
    private TokenDTO getPendingMemberToken(@PathVariable String courseName, @PathVariable String teamId,
                                           @PathVariable String memberId){
        try{
            Long id = Long.valueOf(teamId);
            return notificationService.getPendingMemberToken(id, memberId);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team");
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/studentsInTeam")
    private List<StudentDTO> getStudentsInTeams(@PathVariable String courseName){
        try{
            return teamService.getStudentsInTeams(courseName).stream()
                    .map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/availableStudents")
    private List<StudentDTO> getAvailableStudents(@PathVariable String courseName){
        try{
            return teamService.getAvailableStudents(courseName).stream()
                    .map(ModelHelper::enrich)
                    .collect(Collectors.toList());
        }catch (TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    /*@PostMapping("/{courseName}/vm-model")
    @ResponseStatus(value = HttpStatus.CREATED)
    private VMModelDTO createVMModel(@PathVariable String courseName, @RequestBody @Valid VMModelDTO vmModel){
        try{
            return vmService.createVMModel(vmModel, courseName);
        }catch (VMServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }*/

    @PutMapping("/{courseName}/vm-model")
    private VMModelDTO updateVMModel(@PathVariable String courseName, @RequestBody @Valid VMModelDTO vmModel){
        try{
            return vmService.updateVMModel(vmModel, courseName);
        }catch (VMServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/vm-model")
    private VMModelDTO getVMModel(@PathVariable String courseName){
        try{
            return vmService.getCourseVMModel(courseName);
        }catch (VMServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{courseName}/teams/{teamId}/vm-config")
    @ResponseStatus(value = HttpStatus.CREATED)
    private VMConfigDTO createVMConfig(@PathVariable String courseName, @PathVariable String teamId,
                                       @RequestBody @Valid VMConfigDTO config){
        try {
            Long id = Long.valueOf(teamId);
            return vmService.createVMConfiguration(config, id, courseName);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid team");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }


    @PutMapping("/{courseName}/teams/{teamId}/vm-config/")
    private VMConfigDTO updateVMConfig(@PathVariable String courseName, @PathVariable String teamId,
                                       @RequestBody @Valid VMConfigDTO config){
        try {
            Long tId = Long.valueOf(teamId);
            return vmService.updateVMConfiguration(config, tId, courseName);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid team");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/vm-config")
    private VMConfigDTO getVMConfig(@PathVariable String courseName, @PathVariable String teamId){
        try {
            Long id = Long.valueOf(teamId);
            return vmService.getTeamConfig(courseName, id);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid team");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{courseName}/teams/{teamId}/vms")
    @ResponseStatus(value = HttpStatus.CREATED)
    private VMInstanceDTO createVMInstance(@PathVariable String courseName, @PathVariable Long teamId,
                                           @RequestBody @Valid ModelHelper.VMInstanceData instanceData){
        try{
            return vmService.createVMInstance(instanceData.getInstance(), courseName, teamId, instanceData.getStudentId());
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid team");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/vms")
    private List<VMInstanceDTO> getTeamVms(@PathVariable String courseName, @PathVariable String teamId){
        try{
            Long id = Long.valueOf(teamId);
            return vmService.getTeamVMs(courseName, id);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{courseName}/teams/{teamId}/vms/{vmId}")
    private VMInstanceDTO editVMInstance(@PathVariable String courseName, @PathVariable Long teamId, @PathVariable Long vmId,
                                           @RequestBody @Valid ModelHelper.VMInstanceData instanceData){
        if(!vmId.equals(instanceData.getInstance().getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path id and object id are not equals");
        try{
            return vmService.editVMInstance(instanceData.getInstance(), courseName, teamId, instanceData.getStudentId());
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid team");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/vms/{vmId}")
    private VMInstanceDTO getTeamVm(@PathVariable String courseName, @PathVariable String teamId,
                                    @PathVariable String vmId){
        try{
            Long tId = Long.valueOf(teamId);
            Long vId = Long.valueOf(vmId);
            return vmService.getSingleTeamVm(courseName, tId, vId);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team or vm");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/vms/{vmId}/owners")
    private List<StudentDTO> getVmOwners(@PathVariable String courseName, @PathVariable String teamId,
                                         @PathVariable String vmId){
        try{
            Long tId = Long.valueOf(teamId);
            Long vId = Long.valueOf(vmId);
            return vmService.getVMOwners(courseName, tId, vId).stream()
                    .map(ModelHelper::enrich).collect(Collectors.toList());
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team or vm");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/vms/{vmId}/creator")
    private StudentDTO getVmCreator(@PathVariable String courseName, @PathVariable String teamId,
                                         @PathVariable String vmId){
        try{
            Long tId = Long.valueOf(teamId);
            Long vId = Long.valueOf(vmId);
            return ModelHelper.enrich(vmService.getVMCreator(courseName, tId, vId));
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team or vm");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{courseName}/teams/{teamId}/vms/{vmId}/boot")
    private void bootVm(@PathVariable String courseName, @PathVariable String teamId,
                        @PathVariable String vmId){
        try{
            Long tId = Long.valueOf(teamId);
            Long vId = Long.valueOf(vmId);
            vmService.bootVMInstance(courseName, tId, vId);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team or vm");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{courseName}/teams/{teamId}/vms/{vmId}/shutdown")
    private void shutdownVm(@PathVariable String courseName, @PathVariable String teamId,
                            @PathVariable String vmId){
        try{
            Long tId = Long.valueOf(teamId);
            Long vId = Long.valueOf(vmId);
            vmService.shutdownVMInstance(courseName, tId, vId);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team or vm");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{courseName}/teams/{teamId}/vms/{vmId}")
    private void deleteVm(@PathVariable String courseName, @PathVariable String teamId,
                          @PathVariable String vmId){
        try{
            Long tId = Long.valueOf(teamId);
            Long vId = Long.valueOf(vmId);
            vmService.deleteVMInstance(courseName, tId, vId);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team or vm");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{courseName}/teams/{teamId}/vms/{vmId}/owners")
    private void setVmOwners(@PathVariable String courseName, @PathVariable String teamId,
                             @PathVariable String vmId, @RequestBody @Valid @NotEmpty List<String> memberIds){
        try{
            Long tId = Long.valueOf(teamId);
            Long vId = Long.valueOf(vmId);
            vmService.setVMOwners(courseName, tId, vId, memberIds);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team or vm");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/active-vms")
    private List<VMInstanceDTO> getActiveTeamVm(@PathVariable String courseName, @PathVariable String teamId){
        try{
            Long tId = Long.valueOf(teamId);
            return vmService.getActiveTeamVms(courseName, tId);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team or vm");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/offline-vms")
    private List<VMInstanceDTO> getOfflineTeamVm(@PathVariable String courseName, @PathVariable String teamId){
        try{
            Long tId = Long.valueOf(teamId);
            return vmService.getOfflineTeamVms(courseName, tId);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team or vm");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/teams/{teamId}/vms/{vmId}/vm-model")
    private VMModelDTO getVMModelOfInstance(@PathVariable String courseName, @PathVariable String teamId,
                                            @PathVariable String vmId){
        try{
            Long tId = Long.valueOf(teamId);
            Long vId = Long.valueOf(vmId);
            return vmService.getVMModelOfInstance(courseName, tId, vId);
        }catch (NumberFormatException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid team or vm");
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{courseName}/enrolling-course-request")
    @ResponseStatus(value = HttpStatus.CREATED)
    private void enrollRequest(@PathVariable String courseName, @RequestBody String senderStudentId){
        try {
            notificationService.requestForCourseEnrolling(senderStudentId, courseName);
        }catch (NotificationException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/professors")
    private List<ProfessorDTO> getProfessorsOfCourse(@PathVariable String courseName){
        try{
            return teamService.getProfessorsOfCourse(courseName).stream()
                    .map(ModelHelper::enrich).collect(Collectors.toList());
        }catch (VMServiceException | TeamServiceException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/assignments")
    private List<AssignmentDTO> getAssignments(@PathVariable String courseName){
        try{
            return assignmentService.getAssignmentsOfCourse(courseName);
        } catch (ProfessorNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "getAssignmentPerProfessor failed");
        }
    }

    @PostMapping(value = "/{courseName}/assignments")
    @ResponseStatus(value = HttpStatus.CREATED)
    private AssignmentDTO createAssignment(@PathVariable String courseName,
                                           @RequestParam("json") String assignmentString, @RequestParam("image") MultipartFile image,
                                           @Autowired ObjectMapper mapper){
        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            AssignmentDTO assignmentDTO = mapper.readValue(assignmentString, AssignmentDTO.class);
            System.out.println(assignmentDTO);
            return ModelHelper.enrich(transactionChain.addAssignmentAndUploadImage(assignmentDTO, courseName, image), courseName);
        }catch (TeamServiceException | AssignmentServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping(value = "/{courseName}/assignments/{assignmentId}/drafts/{draftId}/correction")
    @ResponseStatus(value = HttpStatus.CREATED)
    private CorrectionDTO correctDraft(@PathVariable String courseName,
                                       @PathVariable Long assignmentId, @PathVariable Long draftId, @RequestParam("image") MultipartFile image){
        try{
            return transactionChain.correctDraftAndUploadImage(courseName, assignmentId, draftId, 0, image);
        } catch (TeamServiceException | AssignmentServiceException | ImageServiceException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(value = "/{courseName}/assignments/{assignmentId}/image",
            produces = {MediaType.IMAGE_JPEG_VALUE,
                    MediaType.IMAGE_PNG_VALUE,
                    MediaType.IMAGE_JPEG_VALUE})
    private byte[] getAssignmentImage(@PathVariable String courseName,
                                      @PathVariable Long assignmentId){
        try{
            return imageUploadService.getAssignmentImageP(courseName, assignmentId);
        }catch (ImageServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping("/{courseName}/assignments/{assignmentId}/drafts/{draftId}/evaluate")
    @ResponseStatus(value = HttpStatus.CREATED)
    private CorrectionDTO evaluateDraft(@PathVariable String courseName,
                                        @PathVariable String assignmentId, @PathVariable String draftId,
                                        @RequestParam("grade") String grade, @RequestParam("image") MultipartFile image){
        try{
            return this.transactionChain.correctDraftAndUploadImage(courseName, Long.parseLong(assignmentId),
                    Long.parseLong(draftId), Integer.parseInt(grade), image);
        } catch (AssignmentServiceException e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        catch ( NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(value = "/{courseName}/assignments/{assignmentId}/draft/{draftId}/image",
            produces = {MediaType.IMAGE_JPEG_VALUE,
                    MediaType.IMAGE_PNG_VALUE,
                    MediaType.IMAGE_JPEG_VALUE})
    private byte[] getDraftImage(@PathVariable String courseName,
                                 @PathVariable Long assignmentId, @PathVariable Long draftId) {
        try {
            return imageUploadService.getDraftImageProf(courseName, assignmentId, draftId);
        } catch (ImageServiceException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping("/{courseName}/assignments/{assignmentId}/drafts/{draftId}/student")
    private StudentDTO getStudentForDraft(@PathVariable String courseName, @PathVariable Long assignmentId, @PathVariable Long draftId){
        try {
            return this.assignmentService.getStudentForDraft(courseName, assignmentId, draftId);
        } catch (DraftNotFoundException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "getStudentForDraft failed");
        }
    }

    @GetMapping("/{courseName}/assignments/{assignmentId}/drafts")
    private List<DraftDTO> getDrafts(@PathVariable String courseName, @PathVariable Long assignmentId){
        try{
            return assignmentService.getDrafts(courseName, assignmentId);
        } catch (AssignmentServiceException | TeamServiceException  e){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "getDrafts failed");
        }
    }

}
