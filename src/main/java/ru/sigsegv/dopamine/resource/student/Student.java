package ru.sigsegv.dopamine.resource.student;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.sigsegv.dopamine.resource.studygroup.StudyGroup;
import ru.sigsegv.dopamine.resource.studystream.StudyStream;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "student")
public class Student {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "study_group_id")
    @JsonIgnore
    private StudyGroup studyGroup;

    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties({"schedule", "students"})
    private Set<StudyStream> studyStreams = new HashSet<>();

    public Student() {
    }

    public Student(Long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public StudyGroup getStudyGroup() {
        return studyGroup;
    }

    public void setStudyGroup(StudyGroup studyGroup) {
        this.studyGroup = studyGroup;
    }

    @JsonProperty("studyGroupId")
    private long getStudyGroupId() {
        return studyGroup.getId();
    }

    @JsonProperty("studyGroupName")
    private String getStudyGroupName() {
        return studyGroup.getName();
    }

    public Set<StudyStream> getStudyStreams() {
        return studyStreams;
    }

    public void setStudyStreams(Set<StudyStream> studyStreams) {
        this.studyStreams = studyStreams;
    }
}
