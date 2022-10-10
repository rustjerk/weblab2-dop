package ru.sigsegv.dopamine.resource.studystream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.sigsegv.dopamine.resource.schedule.Schedule;
import ru.sigsegv.dopamine.resource.student.Student;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "study_stream")
public class StudyStream {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Schedule schedule;

    @ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinTable(
            name = "study_stream_student",
            joinColumns = { @JoinColumn(name = "study_stream_id") },
            inverseJoinColumns = { @JoinColumn(name = "student_id") }
    )
    @JsonIgnoreProperties({"studyGroupName", "studyGroupId", "studyStreams"})
    private Set<Student> students = new HashSet<>();

    public StudyStream() {
    }

    public StudyStream(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }

    @JsonProperty("scheduleId")
    private long getScheduleId() {
        return schedule.getId();
    }
}
