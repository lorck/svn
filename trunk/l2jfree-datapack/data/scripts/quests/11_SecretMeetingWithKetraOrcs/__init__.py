#made by Emperorc
import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "11_SecretMeetingWithKetraOrcs"

#NPCs
Cadmon = 31296
Leon = 31256
Wahkan = 31371

#Item
Box = 7231

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
     htmltext = event
     if event == "31296-03.htm" :
       if st.getPlayer().getLevel() >= 74 :
            st.set("cond","1")
            htmltext = "31296-03.htm"
            st.setState(State.STARTED)
            st.playSound("ItemSound.quest_accept")
       else :
            htmltext = "31296-02.htm"
            st.exitQuest(1)
     elif event == "31256-02.htm" :
         st.set("cond","2")
         htmltext = "31256-02.htm"
         st.giveItems(Box,1)
     elif event == "31371-02.htm" :
         htmltext = "31371-02.htm"
         st.takeItems(Box,-1)
         st.addExpAndSp(22787,0) #Despite what stratics may say, this is the correct reward for this quest.
         st.set("cond","0")
         st.set("onlyone","1")
         st.exitQuest(False) 
         st.playSound("ItemSound.quest_finish")
     return htmltext

 def onTalk (self,npc,player):
     npcId = npc.getNpcId()
     htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
     st = player.getQuestState(qn)
     if not st : return htmltext

     cond = st.getInt("cond")
     onlyone = st.getInt("onlyone")
     if st.getState() == State.CREATED :
        st.set("cond","0")
        st.set("onlyone","0")
     if onlyone == 0 :
         if npcId == Cadmon :
             if cond == 0 :
                 htmltext = "31296-01.htm"
             elif cond == 1 :
                 htmltext = "31296-04.htm"
         if st.getState() == State.STARTED :
             if npcId == Leon :
                 if cond == 1 :
                     htmltext = "31256-01.htm"
                 elif cond == 2 :
                     htmltext = "31256-03.htm"
             elif npcId == Wahkan and cond == 2 :
                 htmltext = "31371-01.htm"
     return htmltext
     
QUEST       = Quest(11, qn, "Secret Meeting With Ketra Orcs")


QUEST.addStartNpc(Cadmon)

QUEST.addTalkId(Cadmon)

QUEST.addTalkId(Leon)
QUEST.addTalkId(Wahkan)
