package net.sf.l2j.gameserver.dao.forum.impl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.dao.forum.ForumsDAO;
import net.sf.l2j.gameserver.model.forum.Forums;
import net.sf.l2j.gameserver.model.forum.Posts;
import net.sf.l2j.gameserver.model.forum.Topic;
import net.sf.l2j.tools.hibernate.ADAOTestCase;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.hibernate.ObjectDeletedException;

/**
 * 
 */
public class TestForumsDAOHib extends ADAOTestCase
{
	/**
	 * DAO to test
	 */
	private ForumsDAOHib __postsDAO = null;


    public TestForumsDAOHib(String name)
    {
    	super(name);
    }

	@SuppressWarnings("deprecation")
	public void setUp() throws Exception
    {
    	super.setUp();
    	// Set DAO to test
    	setForumsDAO(new ForumsDAOHib());
    	getForumsDAO().setCurrentSession(getSession());
    }

	public String[] getMappings()
    {
    	return new String [] {"Forums.hbm.xml","Posts.hbm.xml", "Topic.hbm.xml"};
    }


    /**
     * Test method for
     * {@link net.sf.l2j.gameserver.dao.impl.ForumsDAO#getForumById(int)}.
     */
    public void testFindById()
    {
    	Forums forum = getForumsDAO().getForumById(6);
    	assertNotNull(forum);
    	assertEquals(6, forum.getForumId());
    }

	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#getForumByName(String)}.
	 */
    public void testFindByName()
    {
    	Forums forum = getForumsDAO().getForumByName("Kyor");
    	assertNotNull(forum);
    	assertEquals("Kyor", forum.getForumName());
    }
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#getChildrens(Forums)}.
	 */
    public void testFindChildrenByEntity()
    {
    	Forums forum = getForumsDAO().getForumByName("ClanRoot");
    	assertNotNull(forum);
    	assertEquals("ClanRoot", forum.getForumName());
    	assertEquals(2,getForumsDAO().getChildrens(forum).size());
    }
    
    
	/**
	 * Test method for getTopics and getPosts
	 */
    public void testFindChildTopicsAndPosts()
    {
    	Forums forum = getForumsDAO().getForumById(6);
    	assertNotNull(forum);
    	assertEquals(6, forum.getForumId());
    	assertEquals(1,forum.getTopics().size());
    	assertEquals(1,forum.getPostses().size());
    }
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#getChildrens(Integer)}.
	 */
    public void testFindChildrenByKey()
    {
    	assertEquals(2,getForumsDAO().getChildrens(2).size());
    }   
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#getAllForums()}.
	 */
    public void testFindAllForums()
    {
    	assertEquals(6,getForumsDAO().getAllForums().size());
    }      
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#createForum()}.
	 */
    public void testCreateForum()
    {
    	// note that forum is not important because we use increment strategies 
    	// for forum ID
    	Forums forums = new Forums(-1,"clanTest",2,0,1,1,25555);
    	assertEquals(7,getForumsDAO().createForums(forums));
    	assertEquals(7,getForumsDAO().getAllForums().size());
    	assertEquals(3,getForumsDAO().getChildrens(2).size());
    }   
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#createForum()}.
	 */
    public void testCreateForumAndAddTopic()
    {
    	// note that forum is not important because we use increment strategies 
    	// for forum ID
    	Forums forums = new Forums(-1,"clanTest",2,0,1,1,25555);
    	assertEquals(7,getForumsDAO().createForums(forums));
    	assertEquals(7,getForumsDAO().getAllForums().size());
    	assertEquals(3,getForumsDAO().getChildrens(2).size());
    	
    	Topic topic = new Topic();
    	topic.setTopicForumId(7);
    	topic.setTopicName("topic test");
    	topic.setTopicOwnerid(9);
    	topic.setTopicOwnername("a player");
    	topic.setTopicDate(new BigDecimal(11112588));
    	
    	// add topic
    	forums.getTopics().add(topic);
    	topic.setForums(forums);
    	
    	getForumsDAO().saveOrUpdate(forums);
    	
    	// just to be sure that the session is flushed 
    	getForumsDAO().getCurrentSession().flush();
    	
    	forums = getForumsDAO().getForumById(7);
    	assertEquals(1,forums.getTopics().size());
    }       
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#deleteForum()}.
	 */
    public void testDeleteForumWithCascade()
    {
    	Forums forum = getForumsDAO().getForumById(6);
    	assertNotNull(forum);
    	assertEquals(6, forum.getForumId());
    	
    	getForumsDAO().deleteForum(forum);
    	
    	try
    	{
    		forum = getForumsDAO().getForumById(6);
    		fail("Forums found but it should be deleted.");
    	}
    	catch (ObjectDeletedException e)
    	{
    		assertNotNull(e);
    	}
    	
    	// check that children were erased
    	assertEquals(0,getForumsDAO().getCurrentSession().createQuery("from "+Topic.class.getName()+" where topicForumId="+6).list().size());
    	assertEquals(0,getForumsDAO().getCurrentSession().createQuery("from "+Posts.class.getName()+" where postForumId="+6).list().size());
    }       
    
    /**
     * @return the postsDAO
     */
    public ForumsDAOHib getForumsDAO()
    {
    	return __postsDAO;
    }

    /**
     * @param _postsDAO
     * the postsDAO to set
     */
    public void setForumsDAO(ForumsDAOHib _postsDAO)
    {
    	__postsDAO = _postsDAO;
    }

    protected List<IDataSet> getDataSet() throws Exception
    {
    	String [] dataSetNameList = {"forums.xml","topic.xml","posts.xml"};
    	String dtdName = "database/l2jdb.dtd";
    	List<IDataSet> dataSetList = new ArrayList<IDataSet>();
	
    	InputStream inDTD = this.getClass().getResourceAsStream(dtdName);
    	FlatDtdDataSet dtdDataSet = new FlatDtdDataSet(inDTD);
    	for(int indice=0; indice<dataSetNameList.length; indice++)
    	{
    		InputStream in = this.getClass().getResourceAsStream(dataSetNameList[indice]);
    		IDataSet dataSet = new FlatXmlDataSet(in, dtdDataSet);
    		dataSetList.add(dataSet);
    	}
    	return dataSetList;
    }

	/**
	 * @return the postsDAO
	 */
	public ForumsDAO getpostsDAO() {
		return __postsDAO;
	}
  	
}
