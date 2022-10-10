package ru.sigsegv.dopamine.resource.schedule;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import ru.sigsegv.dopamine.resource.student.Student;
import ru.sigsegv.dopamine.util.DbHelper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Path("/schedule")
public class ScheduleResource {
    @Context
    private ServletContext context;

    @GET
    @Path("/{schedule}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ScheduleEntry> schedule(@PathParam("schedule") long scheduleId,
                                        @QueryParam("start") Long startTimestamp,
                                        @QueryParam("end") Long endTimestamp) {
        var start = convertTime(startTimestamp, 0);
        var end = convertTime(endTimestamp, 999999999999L);

        try (var session = DbHelper.openSession(context)) {
            session.beginTransaction();

            var query = session.createQuery(
                    "from ScheduleEntry where schedule.id = :id " +
                            "and timeStart >= :start " +
                            "and timeEnd <= :end", ScheduleEntry.class);
            query.setParameter("id", scheduleId);
            query.setParameter("start", start);
            query.setParameter("end", end);
            var schedule = query.list();

            session.getTransaction().commit();

            return schedule;
        }
    }

    @GET
    @Path("/student/{student}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ScheduleEntry> studentSchedule(@PathParam("student") long studentId,
                                               @QueryParam("start") Long startTimestamp,
                                               @QueryParam("end") Long endTimestamp) {
        var start = convertTime(startTimestamp, 0);
        var end = convertTime(endTimestamp, 999999999999L);

        try (var session = DbHelper.openSession(context)) {
            session.beginTransaction();

            var student = session.createQuery("from Student s join fetch s.studyGroup where s.id = :id", Student.class)
                    .setParameter("id", studentId)
                    .uniqueResult();
            if (student == null) {
                session.getTransaction().commit();
                throw new NotFoundException();
            }

            var studyStreams = student.getStudyStreams();

            var scheduleIds = new ArrayList<>(1 + studyStreams.size());
            scheduleIds.add(student.getStudyGroup().getSchedule().getId());
            for (var stream : studyStreams) {
                scheduleIds.add(stream.getSchedule().getId());
            }

            var query = session.createQuery(
                    "from ScheduleEntry where schedule.id in :ids " +
                            "and timeStart >= :start " +
                            "and timeEnd <= :end " +
                            "order by timeStart asc", ScheduleEntry.class);
            query.setParameterList("ids", scheduleIds);
            query.setParameter("start", start);
            query.setParameter("end", end);
            var schedule = query.list();

            session.getTransaction().commit();

            return schedule;
        }
    }

    private static LocalDateTime convertTime(Long timestamp, long fallback) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp == null ? fallback : timestamp), ZoneId.systemDefault());
    }
}
