package it.ai.polito.lab2;

import it.ai.polito.lab2.dtos.CourseDTO;
import it.ai.polito.lab2.dtos.StudentDTO;
import it.ai.polito.lab2.dtos.TeamDTO;
import it.ai.polito.lab2.service.TeamService;
import it.ai.polito.lab2.service.exceptions.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CREATE A DATABASE NAMED teams_test BEFORE RUNNING THESE TESTS
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class Lab2ApplicationTests {

    @Autowired
    private TeamService teamService;

    @Test
    @Order(1)
    void student() {
        List<StudentDTO> expected = Arrays.asList(
                new StudentDTO("s1", "Limoli", "Lorenzo"),
                new StudentDTO("s2", "Loscalzo", "Stefano")
        );

        expected.forEach(s-> assertTrue(teamService.addStudent(s)));
        expected.forEach(s-> assertFalse(teamService.addStudent(s)));

        assertEquals(Optional.of(expected.get(0)), teamService.getStudent("s1"));
        assertEquals(Optional.empty(), teamService.getStudent("fake"));

        List<StudentDTO> students = teamService.getAllStudents();
        assertEquals(new HashSet<>(expected), new HashSet<>(students));
    }

    @Test
    @Order(2)
    void course(){
        List<CourseDTO> expected = Arrays.asList(
                new CourseDTO("Applicazioni Internet", 2,4),
                new CourseDTO("Programmazione di Sistema", 3,4),
                new CourseDTO("OOP", 1,2),
                new CourseDTO("Reti", 2,3)
        );

        expected.forEach(c-> assertTrue(teamService.addCourse(c)));
        expected.forEach(c-> assertFalse(teamService.addCourse(c)));

        assertEquals(Optional.of(expected.get(2)), teamService.getCourse("OOP"));
        assertEquals(Optional.empty(), teamService.getCourse("FAKE"));

        List<CourseDTO> courses = teamService.getAllCourses();
        assertEquals(new HashSet<>(expected), new HashSet<>(courses));
    }

    @Test
    @Order(3)
    void enableDisableCourse(){
        try {
            String courseName = "Applicazioni Internet";
            teamService.enableCourse(courseName);
            CourseDTO course = teamService.getCourse(courseName).get();
            assertTrue(course.isEnabled());

            teamService.disableCourse(courseName);
            course = teamService.getCourse(courseName).get();
            assertFalse(course.isEnabled());
        }catch (NoSuchElementException e){
            fail();
        }

        try {
            teamService.enableCourse("fake");
            fail();
        }catch (CourseNotFoundException ignored){
        }

        try {
            teamService.disableCourse("fake");
            fail();
        }catch (CourseNotFoundException ignored){
        }
    }

    @Test
    @Order(4)
    void addStudentToCourse(){
        String courseName = "Applicazioni Internet";
        File file = new File("src/main/resources/static/students.csv");
        try(Reader reader = new BufferedReader(new FileReader(file))){
            teamService.enableCourse(courseName);
            List<Boolean> res = teamService.addAndEnroll(reader, courseName);
            assertEquals(Arrays.asList(true,true,true,true), res);
        }catch (Exception ex) {
            fail();
        }

        List<StudentDTO> expected = Arrays.asList(
                new StudentDTO("s1", "Limoli", "Lorenzo"),
                new StudentDTO("s2", "Loscalzo", "Stefano"),
                new StudentDTO("s3", "Matteotti", "Giacomo"),
                new StudentDTO("s4", "Rossi", "Marco")
        );
        assertEquals(new HashSet<>(expected), new HashSet<>(teamService.getEnrolledStudents(courseName)));

        assertFalse(teamService.addStudentToCourse(expected.get(0).getId(), courseName));

        try{
            teamService.getEnrolledStudents("fake");
            fail();
        }catch (CourseNotFoundException ignored){
        }

        assertFalse(teamService.addStudentToCourse(expected.get(0).getId(), courseName));
        try {
            teamService.addStudentToCourse("fake", courseName);
            fail();
        }catch (StudentNotFoundException ignored){}

        try {
            teamService.addStudentToCourse(expected.get(0).getId(), "FAKE");
            fail();
        }catch (CourseNotFoundException ignored){}

        try{
            teamService.disableCourse(courseName);
            assertFalse(teamService.addStudentToCourse(expected.get(0).getId(), courseName));
        }catch (CourseNotEnabledException ignored){}

    }

    @Test
    @Order(5)
    void getCoursesByStudent(){
        StudentDTO s = new StudentDTO("prova","test", "test");
        teamService.addStudent(s);
        List<CourseDTO> courses = teamService.getAllCourses();
        courses.forEach(c->{
            teamService.enableCourse(c.getName());
            assertTrue(teamService.addStudentToCourse(s.getId(), c.getName()));
        });
        assertEquals(new HashSet<>(courses), new HashSet<>(teamService.getCourses(s.getId())));

        try{
            teamService.getCourses("fake");
            fail();
        }catch (StudentNotFoundException ignored){}
    }

    @Test
    @Order(6)
    void team(){
        String courseName = "Applicazioni Internet";
        List<String> members = teamService.getEnrolledStudents(courseName)
                .stream().map(StudentDTO::getId)
                .filter(id-> !id.equals("prova")).collect(Collectors.toList());

        try{
            List<String> tmp = new ArrayList<>(members);
            tmp.remove(0);
            tmp.add(tmp.get(0));
            teamService.proposeTeam(courseName, "AI-Team", tmp);
        }catch (DuplicateStudentException ignored){}

        try{
            teamService.proposeTeam("fake", "AI-Team", members);
            fail();
        }catch (CourseNotFoundException ignored){}

        try {
            teamService.disableCourse(courseName);
            teamService.proposeTeam(courseName, "AI-Team", members);
            fail();
        }catch (CourseNotEnabledException ignored){}


        try {
            teamService.enableCourse(courseName);
            List<String> m = new ArrayList<>(members);
            m.add("fake");
            teamService.proposeTeam(courseName, "AI-Team", m);
            fail();
        }catch (StudentNotFoundException ignored){}

        try {
            teamService.enableCourse("OOP");
            teamService.proposeTeam("OOP", "OOP-Team", members);
            fail();
        }catch (StudentNotEnrolledException ignored){}

        teamService.enableCourse(courseName);
        TeamDTO teamAI = teamService.proposeTeam(courseName, "AI-Team", members);

        try {
            teamService.proposeTeam(courseName, "AI-Team2", Arrays.asList(members.get(0)));
            fail();
        }catch (StudentAlreadyBelongsToTeam ignored){}

        try {
            teamService.proposeTeam(courseName, "AI-Team2", Arrays.asList("prova"));
            fail();
        }catch (TeamSizeOutOfBoundException ignored){}

        teamService.addStudentToCourse("s1", "OOP");
        TeamDTO teamOOP = teamService.proposeTeam("OOP", "OOP-Team", Arrays.asList("s1"));
        try {

            teamService.proposeTeam("OOP", "OOP-Team", Arrays.asList("prova"));
            fail();
        }catch (TeamAlreadyExistException ignored){}

        assertEquals(Arrays.asList(teamAI), teamService.getTeamForCourse(courseName));
        assertEquals(Arrays.asList(teamAI, teamOOP), teamService.getTeamsForStudent("s1"));

        List<StudentDTO> expected = Arrays.asList(
                new StudentDTO("s1", "Limoli", "Lorenzo"),
                new StudentDTO("s2", "Loscalzo", "Stefano"),
                new StudentDTO("s3", "Matteotti", "Giacomo"),
                new StudentDTO("s4", "Rossi", "Marco")
        );
        assertEquals(new HashSet<>(expected), new HashSet<>(teamService.getMembers(teamAI.getId())));
        assertEquals(new HashSet<>(expected), new HashSet<>(teamService.getStudentsInTeams(courseName)));

        try {
            StudentDTO s = teamService.getStudent("prova").get();
            assertEquals(new HashSet<>(Arrays.asList(s)), new HashSet<>(teamService.getAvailableStudents(courseName)));
        }catch (NoSuchElementException e){
            fail();
        }
    }

    @Test
    @Order(7)
    void inconsistentData(){
        String courseName = "Applicazioni Internet";
        File file = new File("src/main/resources/static/inconsistentDataTest.csv");
        try(Reader reader = new BufferedReader(new FileReader(file))){
            teamService.enableCourse(courseName);
            teamService.addAndEnroll(reader, courseName);
            fail();
        }catch (InconsistentStudentDataException ignored){
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
            fail();
        }
    }

}
