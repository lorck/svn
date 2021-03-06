/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.communitybbs.model.forum;

// Generated 19 f�vr. 2007 22:07:55 by Hibernate Tools 3.2.0.beta8

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Forums generated by hbm2java
 */
public class Forums implements java.io.Serializable
{

    // Fields    

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -8074717730201663809L;
    private int forumId;
    private String forumName;
    private int forumParent;
    private int forumPost;
    private int forumType;
    private int forumPerm;
    private int forumOwnerId;
    
    private Set<Topic> topics = new HashSet<Topic>(0);

    // Constructors

    /** default constructor */
    public Forums()
    {
    }
    /** 
     * minimal constructor 
     * Be carefull, forumId will be used only if strategies for the key is "assigned" in hbm.xml file
    */
    public Forums(int _forumId, String _forumName, int _forumParent, int _forumPost, int _forumType,
                    int _forumPerm, int _forumOwnerId)
    {
        forumId = _forumId;
        forumName = _forumName;
        forumParent = _forumParent;
        forumPost = _forumPost;
        forumType = _forumType;
        forumPerm = _forumPerm;
        forumOwnerId = _forumOwnerId;
    }

    /** 
     * full constructor 
     * Be carefull, forumId will be used only if strategies for the key is "assigned" in hbm.xml file
     */
    public Forums(int _forumId, String _forumName, int _forumParent, int _forumPost, int _forumType,
                  int _forumPerm, int _forumOwnerId, Set<Posts> _postses, Set<Topic> _topics)
    {
        forumId = _forumId;
        forumName = _forumName;
        forumParent = _forumParent;
        forumPost = _forumPost;
        forumType = _forumType;
        forumPerm = _forumPerm;
        forumOwnerId = _forumOwnerId;
        topics = _topics;
    }


    public Set<Topic> getTopics()
    {
        return topics;
    }

    public void setTopics(Set<Topic> _topics)
    {
        topics = _topics;
    }

    // Property accessors
    public int getForumId()
    {
        return forumId;
    }

    public void setForumId(int _forumId)
    {
        forumId = _forumId;
    }

    public String getForumName()
    {
        return forumName;
    }

    public void setForumName(String _forumName)
    {
        forumName = _forumName;
    }

    public int getForumParent()
    {
        return forumParent;
    }

    public void setForumParent(int _forumParent)
    {
        forumParent = _forumParent;
    }

    public int getForumPost()
    {
        return forumPost;
    }

    public void setForumPost(int _forumPost)
    {
        forumPost = _forumPost;
    }

    public int getForumType()
    {
        return forumType;
    }

    public void setForumType(int _forumType)
    {
        forumType = _forumType;
    }

    public int getForumPerm()
    {
        return forumPerm;
    }

    public void setForumPerm(int _forumPerm)
    {
        forumPerm = _forumPerm;
    }

    public int getForumOwnerId()
    {
        return forumOwnerId;
    }

    public void setForumOwnerId(int _forumOwnerId)
    {
        forumOwnerId = _forumOwnerId;
    }
    /**
     * @return true or false if the two objects are equals (not based on post id)
     * @param obj
     */
    @Override
    public boolean equals(Object _obj) 
    {
        if (_obj == null) 
        {
            return false;
        }
        if (this == _obj) 
        {
            return true;
        }
        Forums rhs = (Forums) _obj;
        return new EqualsBuilder()
                        .appendSuper(super.equals(_obj))
                        .append(forumName, rhs.getForumName())
                        .append(forumOwnerId, rhs.getForumOwnerId())
                        .append(forumType, rhs.getForumType())
                        .append(forumPerm, rhs.getForumPerm())
                        .append(forumParent, rhs.getForumParent())
                        .isEquals();        
    }
    
    /**
     * @return the hashcode of the object
     */
    @Override
    public int hashCode() 
    {
        return new HashCodeBuilder(17,37)
                        .append(forumName)
                        .append(forumOwnerId)
                        .append(forumType)
                        .append(forumPerm)
                        .append(forumParent)
                        .toHashCode();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }    
}