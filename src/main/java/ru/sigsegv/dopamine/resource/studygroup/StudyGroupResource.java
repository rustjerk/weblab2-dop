package ru.sigsegv.dopamine.resource.studygroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import ru.sigsegv.dopamine.util.DbHelper;

@Path("/study-group")
public class StudyGroupResource {
    @Context
    private ServletContext context;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public String studyGroupList() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        mapper.addMixIn(StudyGroup.class, StudyGroupShort.class);

        try (var session = DbHelper.openSession(context)) {
            session.beginTransaction();

            var query = session.createQuery("from StudyGroup", StudyGroup.class);
            var studyGroups = query.list();

            session.getTransaction().commit();

            return mapper.writeValueAsString(studyGroups);
        }
    }

    @GET
    @Path("/{group}")
    @Produces(MediaType.APPLICATION_JSON)
    public StudyGroup studyGroup(@PathParam("group") long groupID) {
        try (var session = DbHelper.openSession(context)) {
            session.beginTransaction();

            var query = session.createQuery("from StudyGroup s join fetch s.students where s.id = :id", StudyGroup.class);
            query.setParameter("id", groupID);
            var studyGroup = query.uniqueResult();

            session.getTransaction().commit();

            if (studyGroup == null) throw new NotFoundException();
            return studyGroup;
        }
    }
}
