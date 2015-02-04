package org.codingpedia.demo.rest.dao.impl;

import com.fasterxml.jackson.datatype.hibernate4.HibernateProxySerializer;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.codingpedia.demo.rest.entities.Podcast;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.repository.mongo.MongoRepository;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.net.UnknownHostException;

/**
 * @author bartosz walacik
 */
public class JaversIntegrationTest {

    private Javers javers;

    //TODO autowire
    @PersistenceContext(unitName="demoRestPersistence")
    private EntityManager entityManager;

    @Before
    public void before() throws UnknownHostException {
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
        DB db = mongoClient.getDB( "test" );
        MongoRepository mongoRepo =  new MongoRepository(db);
        javers = JaversBuilder.javers().registerJaversRepository(mongoRepo).build();
    }

    @Test
    public void shouldCommitPodcastToJaversRepository(){
        //given:
        Podcast p = new Podcast("1","2","3","4");
        entityManager.persist(p);

        //read p from database
        p = entityManager.find(Podcast.class, p.getId());
        p.setTitle("Suman " + p.getTitle());
        entityManager.merge(p);

        //when:
        javers.commit("author",p);

        //TODO
        //assert
    }
}
