package org.codingpedia.demo.rest.dao.impl;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.codingpedia.demo.rest.dao.PodcastDao;
import org.codingpedia.demo.rest.entities.Edition;
import org.codingpedia.demo.rest.entities.Podcast;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.repository.mongo.MongoRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.net.UnknownHostException;

/**
 * @author bartosz walacik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/applicationContext.xml")
public class JaversIntegrationTest {

    private Javers javers;

    private Logger logger = LoggerFactory.getLogger(JaversIntegrationTest.class);

    @PersistenceContext(unitName="demoRestPersistence")
    private EntityManager entityManager;

    @Autowired
    private PodcastDao podcastDao;

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
        Edition e = entityManager.find(Edition.class, 1L);
        p.setEdition(e);
        podcastDao.createPodcast(p);
        //read p from database
        p = entityManager.find(Podcast.class, p.getId());
        p.setTitle("New Title " + p.getTitle());
        podcastDao.updatePodcast(p);
        //when:
        javers.commit("author",p);

        //TODO
        //assert
    }
}
