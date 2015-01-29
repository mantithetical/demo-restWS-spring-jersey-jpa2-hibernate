package org.codingpedia.demo.rest.dao.impl;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.gson.*;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.codingpedia.demo.rest.dao.PodcastDao;
import org.codingpedia.demo.rest.entities.Edition;
import org.codingpedia.demo.rest.entities.Podcast;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.json.JsonTypeAdapterTemplate;
import org.javers.core.metamodel.type.TypeMapper;
import org.javers.repository.mongo.MongoRepository;
import org.springframework.transaction.annotation.Transactional;

public class PodcastDaoJPA2Impl implements PodcastDao {

	@PersistenceContext(unitName="demoRestPersistence")
	private EntityManager entityManager;
	
	@Transactional
	public List<Podcast> getPodcasts() {

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Podcast> query = cb.createQuery(Podcast.class);
		query.from(Podcast.class);
		List<Podcast> podcasts = entityManager.createQuery(query).getResultList();
		for (Podcast p : podcasts) {
			Hibernate.initialize(p.getSoundEffectList());
			p.setEdition((Edition)((HibernateProxy) p.getEdition()).getHibernateLazyInitializer().getImplementation());
		}
		return podcasts;
	}

	public List<Podcast> getRecentPodcasts(int numberOfDaysToLookBack) {
		
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeZone(TimeZone.getTimeZone("UTC+1"));//Munich time 
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -numberOfDaysToLookBack);//substract the number of days to look back 
		Date dateToLookBackAfter = calendar.getTime();
		
		String qlString = "SELECT p FROM Podcast p where p.insertionDate > :dateToLookBackAfter";
		TypedQuery<Podcast> query = entityManager.createQuery(qlString, Podcast.class);		
		query.setParameter("dateToLookBackAfter", dateToLookBackAfter, TemporalType.DATE);

		return query.getResultList();
	}
	@Transactional
	public Podcast getPodcastById(Long id) {
		
		try {
			String qlString = "SELECT p FROM Podcast p WHERE p.id = ?1";
			TypedQuery<Podcast> query = entityManager.createQuery(qlString, Podcast.class);
			query.setParameter(1, id);
			Podcast p = query.getSingleResult();
			Hibernate.initialize(p.getSoundEffectList());
            p.setEdition((Edition)
                    ((HibernateProxy) p.getEdition()).getHibernateLazyInitializer().getImplementation());
			return p;
		} catch (NoResultException e) {
			return null;
		}
	}

	public Long deletePodcastById(Long id) {
		
		Podcast podcast = entityManager.find(Podcast.class, id);
		entityManager.remove(podcast);
		
		return 1L;
	}

	public Long createPodcast(Podcast podcast) {
		
		podcast.setInsertionDate(new Date());
		entityManager.persist(podcast);
		entityManager.flush();//force insert to receive the id of the podcast
		
		return podcast.getId();
	}

	@Transactional
	public int updatePodcast(Podcast podcast) {

        Podcast p = entityManager.find(Podcast.class, podcast.getId());
        p.setTitle("Suman " + p.getTitle());
        entityManager.merge(p);

		MongoClient mongoClient = null;
		Javers javers;
		try {
			mongoClient = new MongoClient( "localhost" , 27017 );
			DB db = mongoClient.getDB( "test" );
			MongoRepository mongoRepo =  new MongoRepository(db);
			javers = JaversBuilder.javers().registerJaversRepository(mongoRepo)
//                    .registerValueGsonTypeAdapter(HibernateProxy.class, new GsonHibernateProxySkipper()).build();
					.registerValueTypeAdapter(new HibernateProxySerializer()).build();
            if (p.getEdition() instanceof HibernateProxy) {
                System.out.print("yikes");
            }
			javers.commit("rest-demo", p);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mongoClient != null) mongoClient.close();
		}
		
		return 1; 
	}

    private static class GsonHibernateProxySkipper implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return aClass == HibernateProxy.class;
        }
    }

	private static class HibernateProxySerializer extends JsonTypeAdapterTemplate<HibernateProxy> {
		@Inject
		private TypeMapper typeMapper;

		@Override
		public HibernateProxy fromJson(JsonElement json,
									   JsonDeserializationContext jsonDeserializationContext) {
			return jsonDeserializationContext.deserialize(json, typeMapper.getDehydratedType(HibernateProxy.class));
		}

		@Override
		public JsonElement toJson(HibernateProxy sourceValue,
								  JsonSerializationContext jsonSerializationContext) {
			Object deProxied = sourceValue.getHibernateLazyInitializer().getImplementation();
			return jsonSerializationContext.serialize(deProxied, typeMapper.getDehydratedType(HibernateProxy.class));
//            return null;
		}

		@Override
		public Class getValueType() {
			return HibernateProxy.class;
		}
	}

	public void deletePodcasts() {
		Query query = entityManager.createNativeQuery("TRUNCATE TABLE podcasts");		
		query.executeUpdate();
	}

}
