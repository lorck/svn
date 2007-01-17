# Made by Mr. Have fun!
# Version 0.3 by H1GHL4ND3R
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

SILVERY_SPIDERSILK = 1026
UNOS_RECEIPT = 1027
CELS_TICKET = 1028
NIGHTSHADE_LEAF = 1029
LESSER_HEALING_POTION = 1060

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event
    if event == "30370-04.htm" :
        st.set("cond","1")
        st.setState(STARTED)
        st.playSound("ItemSound.quest_accept")
        st.giveItems(SILVERY_SPIDERSILK,1)
    return htmltext

 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   htmltext = "<html><head><body>I have nothing to say you</body></html>"
   id = st.getState()
   if id == CREATED :
     if st.getPlayer().getRace().ordinal() != 1 :
       htmltext = "30370-00.htm"
     elif st.getPlayer().getLevel() >= 3 :
       htmltext = "30370-03.htm"
       st.set("cond","0")
     else:
       htmltext = "30370-02.htm"
       st.exitQuest(1)
   elif id == COMPLETED :
     htmltext = "<html><head><body>This quest have already been completed.</body></html>"
   else :
     try :
       cond = int(st.get("cond"))
     except :
       cond = None
     if cond == 1 :
       if npcId == 30370 :
         htmltext = "30370-05.htm"
       elif npcId == 30147 and st.getQuestItemsCount(SILVERY_SPIDERSILK) :
         st.takeItems(SILVERY_SPIDERSILK,1)
         st.giveItems(UNOS_RECEIPT,1)
         st.set("cond","2")
         htmltext = "30147-01.htm"
     elif cond == 2 :
       if npcId == 30370 :
         htmltext = "30370-05.htm"
       elif npcId == 30147 and st.getQuestItemsCount(UNOS_RECEIPT) :
         htmltext = "30147-02.htm"
       elif npcId == 30149 and st.getQuestItemsCount(UNOS_RECEIPT) :
         st.takeItems(UNOS_RECEIPT,1)
         st.giveItems(CELS_TICKET,1)
         st.set("cond","3")
         htmltext = "30149-01.htm"
     elif cond == 3 :
       if npcId == 30370 :
         htmltext = "30370-05.htm"
       elif npcId == 30149 and st.getQuestItemsCount(CELS_TICKET) :
         htmltext = "30149-02.htm"
       elif npcId == 30152 and st.getQuestItemsCount(CELS_TICKET) :
        st.takeItems(CELS_TICKET,st.getQuestItemsCount(CELS_TICKET))
        st.giveItems(NIGHTSHADE_LEAF,1)
        st.set("cond","4")
        htmltext = "30152-01.htm"
     elif cond == 4 :
        if npcId == 30152 and st.getQuestItemsCount(NIGHTSHADE_LEAF) :
          htmltext = "30152-02.htm"
        elif npcId == 30149 and st.getQuestItemsCount(NIGHTSHADE_LEAF) :
          htmltext = "30149-03.htm"
        elif npcId == 30147 and st.getQuestItemsCount(NIGHTSHADE_LEAF) :
          htmltext = "30147-03.htm"
        elif npcId == 30370 and st.getQuestItemsCount(NIGHTSHADE_LEAF) :
          st.takeItems(NIGHTSHADE_LEAF,1)
          st.giveItems(LESSER_HEALING_POTION,1)
          st.addExpAndSp(1000,0)
          st.unset("cond")
          st.setState(COMPLETED)
          st.playSound("ItemSound.quest_finish")
          htmltext = "30370-06.htm"
   return htmltext

QUEST       = Quest(160,"160_NerupasFavor","Nerupas Favor")
CREATED     = State('Start', QUEST)
STARTING    = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(30370)

CREATED.addTalkId(30370)
STARTING.addTalkId(30370)
COMPLETED.addTalkId(30370)

STARTED.addTalkId(30147)
STARTED.addTalkId(30149)
STARTED.addTalkId(30152)
STARTED.addTalkId(30370)

STARTED.addQuestDrop(30370,SILVERY_SPIDERSILK,1)
STARTED.addQuestDrop(30147,UNOS_RECEIPT,1)
STARTED.addQuestDrop(30149,CELS_TICKET,1)
STARTED.addQuestDrop(30152,NIGHTSHADE_LEAF,1)

print "importing quests: 160: Nerupas Favor"
