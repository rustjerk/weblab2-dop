package ru.sigsegv.dopamine.resource.student;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"studyStreams"})
public abstract class StudentShort {
}
