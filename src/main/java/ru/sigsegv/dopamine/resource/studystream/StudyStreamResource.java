package ru.sigsegv.dopamine.resource.studystream;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import ru.sigsegv.dopamine.util.DbHelper;

@Path("/study-stream")
public class StudyStreamResource {
    @Context
    private ServletContext context;

    @GET
    @Path("/{stream}")
    @Produces(MediaType.APPLICATION_JSON)
    public StudyStream studyStream(@PathParam("stream") long streamID) {
        try (var session = DbHelper.openSession(context)) {
            session.beginTransaction();

            var query = session.createQuery("from StudyStream s join fetch s.students where s.id = :id", StudyStream.class);
            query.setParameter("id", streamID);
            var studyStream = query.uniqueResult();

            session.getTransaction().commit();

            if (studyStream == null) throw new NotFoundException();
            return studyStream;
        }
    }
}
