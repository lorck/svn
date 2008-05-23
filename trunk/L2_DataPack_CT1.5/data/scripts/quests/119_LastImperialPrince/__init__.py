# By L2J_JP SANDMAN
import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "119_LastImperialPrince"

#NPC
SPIRIT   = 31453    #Nameless Spirit
DEVORIN  = 32009    #Devorin

#ITEM
BROOCH   = 7262     #Antique Brooch

#REWARD
ADENA    = 57       #Adena
AMOUNT   = 68787    #Amount

class Quest (JQuest) :

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onEvent (self,event,st) :
    htmltext = event
    if event == "31453-4.htm" :
      st.set("cond","1")
      st.setState(State.STARTED)
      st.playSound("ItemSound.quest_accept")
    elif event == "32009-2.htm" :
      if st.getQuestItemsCount(BROOCH) < 1 :
        htmltext = "<html><body>Quest <font color=\"LEVEL\">Four Goblets</font> is not accomplished or the condition is not suitable.</body></html>"
        st.exitQuest(1)
    elif event == "32009-3.htm" :
      st.set("cond","2")
      st.playSound("ItemSound.quest_middle")
    elif event == "31453-7.htm" :
      st.giveItems(ADENA,AMOUNT)
      st.setState(State.COMPLETED)
      st.playSound("ItemSound.quest_finish")
      st.exitQuest(1)
    return htmltext

  def onTalk (Self,npc,player):
    st = player.getQuestState(qn)
    htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
    if not st: return htmltext

    id = st.getState()
    if id == State.CREATED :
      st.set("cond","0")

    #confirm that quest can be executed.
    if player.getLevel() < 74 :
      htmltext = "<html><body>Quest for characters level 74 and above.</body></html>"
      st.exitQuest(1)
      return htmltext
    elif id == State.COMPLETED :
      htmltext = "<html><body>This quest have already been completed.</body></html>"
      st.exitQuest(1)
      return htmltext
    elif st.getQuestItemsCount(BROOCH) < 1 :
      htmltext = "<html><body>Quest <font color=\"LEVEL\">Four Goblets</font> is not accomplished or the condition is not suitable.</body></html>"
      st.exitQuest(1)
      return htmltext

    cond = st.getInt("cond")
    npcId = npc.getNpcId()

    if npcId == SPIRIT :
      if cond == 0 :
        return "31453-1.htm"
      elif cond == 2 :
        return "31453-5.htm"
      else :
        htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
        return htmltext
    elif npcId == DEVORIN :
      if cond == 1 :
        return "32009-1.htm"
      else :
        htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
        return htmltext

QUEST = Quest(119,qn,"Last Imperial Prince")

QUEST.addStartNpc(SPIRIT)

QUEST.addTalkId(SPIRIT)
QUEST.addTalkId(DEVORIN)