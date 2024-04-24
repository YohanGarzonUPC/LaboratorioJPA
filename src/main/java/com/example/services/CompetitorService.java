/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.services;

import com.example.PersistenceManager;
import com.example.models.Competitor;
import com.example.models.CompetitorDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.persistence.Query;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import javax.ws.rs.NotAuthorizedException;

/**
 *
 * @author Mauricio
 */
@Path("/competitors")
@Produces(MediaType.APPLICATION_JSON)
public class CompetitorService {

    @PersistenceContext(unitName = "CompetitorsPU")
    EntityManager entityManager;

    @PostConstruct
    public void init() {
        try {
            entityManager
                    = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        Query q = entityManager.createQuery("select u from Competitor u order by u.surname ASC");
        List<Competitor> competitors = q.getResultList();
        return Response.status(200).header("Access-Control-Allow-Origin",
                "*").entity(competitors).build();
    }

    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCompetitor(CompetitorDTO competitor) {
        JSONObject rta = new JSONObject();
        Competitor competitorTmp = new Competitor();
        competitorTmp.setAddress(competitor.getAddress());
        competitorTmp.setAge(competitor.getAge());
        competitorTmp.setCellphone(competitor.getCellphone());
        competitorTmp.setCity(competitor.getCity());
        competitorTmp.setCountry(competitor.getCountry());
        competitorTmp.setName(competitor.getName());
        competitorTmp.setSurname(competitor.getSurname());
        competitorTmp.setTelephone(competitor.getTelephone());
        competitorTmp.setEmail(competitor.getEmail());
        competitorTmp.setPassword(competitor.getPassword());

        try {
            entityManager.getTransaction().begin();
            entityManager.persist(competitorTmp);
            entityManager.getTransaction().commit();
            entityManager.refresh(competitorTmp);
            rta.put("competitor_id", competitorTmp.getId());
        } catch (Throwable t) {
            t.printStackTrace();
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            competitorTmp = null;
        } finally {
            entityManager.clear();
            entityManager.close();
        }
        return Response.status(200).header("Access-Control-Allow-Origin",
                "*").entity(rta).build();
    }
    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(CompetitorDTO competitor) {
        JSONObject response = new JSONObject();
        try {
            Query query = entityManager.createQuery("select u from Competitor u where u.email = :email and u.password = :password");
            query.setParameter("email", competitor.getEmail());
            query.setParameter("password", competitor.getPassword());
            List<Competitor> competitors = query.getResultList();

            if (!competitors.isEmpty()) {
                Competitor competitorDTO = competitors.get(0);
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("competitor", competitorDTO);
                return Response.status(200)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(response)
                        .build();
            } else {
                response.put("success", false);
                response.put("message", "Invalid credentials");
                return Response.status(401)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(response)
                        .build();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            response.put("success", false);
            response.put("message", "An error occurred during login");
            return Response.status(500)
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(response)
                    .build();
        }
    }
}
