package ru.sigsegv.dopamine.resource.student;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.hibernate.Hibernate;
import ru.sigsegv.dopamine.util.DbHelper;
import ru.sigsegv.dopamine.util.Paginated;

@Path("/student")
public class StudentResource {
    @Context
    private ServletContext context;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public String studentList(@QueryParam("idQuery") String idQuery,
                              @QueryParam("nameQuery") String nameQuery,
                              @QueryParam("groupQuery") String groupQuery,
                              @QueryParam("orderBy") String orderBy,
                              @QueryParam("orderDir") String orderDir,
                              @QueryParam("page") int page) throws JsonProcessingException {
        if (idQuery == null) idQuery = "";
        if (nameQuery == null) nameQuery = "";
        if (groupQuery == null) groupQuery = "";
        if (orderBy == null) orderBy = "name";
        if (orderDir == null) orderDir = "asc";

        var mapper = new ObjectMapper();
        mapper.addMixIn(Student.class, StudentShort.class);

        try (var session = DbHelper.openSession(context)) {
            session.beginTransaction();

            var idPattern = "%" + idQuery.replace("%", "%%").toLowerCase() + "%";
            var namePattern = "%" + nameQuery.replace("%", "%%").toLowerCase() + "%";
            var groupPattern = "%" + groupQuery.replace("%", "%%").toLowerCase() + "%";

            var cb = session.getCriteriaBuilder();
            var query = cb.createQuery(Student.class);
            var root = query.from(Student.class);
            var studyGroup = (Join<Object, Object>) root.fetch("studyGroup");
            query.where(cb.and(
                    cb.like(cb.toString(root.get("id")), idPattern),
                    cb.like(cb.lower(root.get("fullName")), namePattern),
                    cb.like(cb.lower(studyGroup.get("name")), groupPattern)));

            var field = switch (orderBy) {
                case "id" -> root.get("id");
                case "group" -> studyGroup.get("name");
                default -> root.get("fullName");
            };

            query.orderBy(orderDir.equals("asc") ? cb.asc(field) : cb.desc(field));

            var students = Paginated.fromScrollable(session.createQuery(query).scroll(), page, 50);

            session.getTransaction().commit();

            return mapper.writeValueAsString(students);
        }
    }

    @GET
    @Path("/{student}")
    @Produces(MediaType.APPLICATION_JSON)
    public Student student(@PathParam("student") long studentId) {
        try (var session = DbHelper.openSession(context)) {
            session.beginTransaction();

            var query = session.createQuery("from Student s join fetch s.studyGroup where s.id = :id", Student.class);
            query.setParameter("id", studentId);
            var student = query.uniqueResult();
            if (student == null) {
                session.getTransaction().commit();
                throw new NotFoundException();
            }

            Hibernate.initialize(student.getStudyGroup());
            Hibernate.initialize(student.getStudyStreams());

            session.getTransaction().commit();

            return student;
        }
    }

}