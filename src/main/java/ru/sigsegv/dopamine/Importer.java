package ru.sigsegv.dopamine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.sigsegv.dopamine.resource.schedule.Schedule;
import ru.sigsegv.dopamine.resource.schedule.ScheduleEntry;
import ru.sigsegv.dopamine.resource.student.Student;
import ru.sigsegv.dopamine.resource.studygroup.StudyGroup;
import ru.sigsegv.dopamine.resource.studystream.StudyStream;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class Importer {
    public static void main(String[] args) throws IOException {
        try (var sessionFactory = getSessionFactory()) {
            var session = sessionFactory.openSession();
            session.beginTransaction();

            session.createMutationQuery("delete from Student").executeUpdate();
            session.createMutationQuery("delete from StudyGroup").executeUpdate();
            session.createMutationQuery("delete from ScheduleEntry").executeUpdate();
            session.createMutationQuery("delete from Schedule").executeUpdate();
            session.createMutationQuery("delete from StudyStream").executeUpdate();

            for (var path : Objects.requireNonNull(new File("/home/leshainc/Projects/isuscraper/groups/").listFiles())) {
                var mapper = new ObjectMapper();
                var tree = mapper.readTree(path);

                var studyGroup = new StudyGroup(tree.get("group").asText());

                for (var member : tree.get("members")) {
                    var student = new Student(Long.parseLong(member.get(0).asText()), member.get(1).asText());
                    student.setStudyGroup(studyGroup);
                    session.persist(student);
                }

                var schedule = new Schedule();
                session.persist(schedule);

                studyGroup.setSchedule(schedule);
                session.persist(studyGroup);

                var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                for (var sched : tree.get("schedule")) {
                    var timeStart = LocalDateTime.parse(sched.get(0).asText(), formatter);
                    var timeEnd = LocalDateTime.parse(sched.get(1).asText(), formatter);
                    var place = String.join("\n", mapper.convertValue(sched.get(2), new TypeReference<List<String>>() {}));
                    var subjectName = sched.get(3).asText();
                    var subjectKind = sched.get(4).asText();
                    var teacher = sched.get(5).asText();
                    var entry = new ScheduleEntry(schedule, timeStart, timeEnd, place, subjectName, subjectKind, teacher);
                    session.persist(entry);
                }
            }

            int i = 0;

            for (var path : Objects.requireNonNull(new File("/home/leshainc/Projects/isuscraper/streams/").listFiles())) {
                System.out.println(i + " " + path);
                i++;

                var mapper = new ObjectMapper();
                var tree = mapper.readTree(path);

                var studyStream = new StudyStream(tree.get("name").asText());
                session.persist(studyStream);

                for (var member : tree.get("members")) {
                    var student = session.createQuery("from Student where id = :id", Student.class)
                            .setParameter("id", Long.parseLong(member.get(0).asText()))
                            .uniqueResult();
                    if (student == null) continue;
                    student.getStudyStreams().add(studyStream);
                    studyStream.getStudents().add(student);
                    session.persist(student);
                }

                var schedule = new Schedule();
                session.persist(schedule);

                studyStream.setSchedule(schedule);
                session.persist(studyStream);

                var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                for (var sched : tree.get("schedule")) {
                    var timeStart = LocalDateTime.parse(sched.get(0).asText(), formatter);
                    var timeEnd = LocalDateTime.parse(sched.get(1).asText(), formatter);
                    var place = String.join("\n", mapper.convertValue(sched.get(2), new TypeReference<List<String>>() {}));
                    var subjectName = sched.get(3).asText();
                    var subjectKind = sched.get(4).asText();
                    var teacher = sched.get(5).asText();
                    var entry = new ScheduleEntry(schedule, timeStart, timeEnd, place, subjectName, subjectKind, teacher);
                    session.persist(entry);
                }
            }

            session.getTransaction().commit();
        }
    }

    private static SessionFactory getSessionFactory() {
        var standardRegistry = new StandardServiceRegistryBuilder();
        standardRegistry.configure("hibernate.cfg.xml");
        standardRegistry.applySetting("hibernate.connection.url", "jdbc:postgresql://localhost/weblab");
        standardRegistry.applySetting("hibernate.connection.username", "weblab");
        standardRegistry.applySetting("hibernate.connection.password", "pass");

        var metadata = new MetadataSources(standardRegistry.build()).getMetadataBuilder().build();
        return metadata.getSessionFactoryBuilder().build();
    }
}
