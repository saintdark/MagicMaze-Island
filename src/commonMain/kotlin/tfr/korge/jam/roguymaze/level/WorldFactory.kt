package tfr.korge.jam.roguymaze.level

import tfr.korge.jam.roguymaze.GameFlow
import tfr.korge.jam.roguymaze.InputEvent.Action
import tfr.korge.jam.roguymaze.InputEvent.Action.*
import tfr.korge.jam.roguymaze.model.Room
import tfr.korge.jam.roguymaze.model.Team
import tfr.korge.jam.roguymaze.model.World


class WorldFactory {

    val undiscoveredRooms = mutableListOf<Room>()
    val maxRooms = 12

    /**
     * Number of players -> ActionSet
     */
    val actionSets = mutableMapOf<Int, ActionSet>()

    init {
        val set1 = ActionSet(1)
        val allEvents = setOf(
                HeroLeft, HeroRight, HeroDown, HeroUp, ActionSearch)
        for (player in 0..5) {
            set1.addEvents(player, *allEvents.toTypedArray())
        }
        actionSets[0] = set1
        actionSets[1] = set1

        actionSets[2] = ActionSet(2).apply {
            addEvents(1, HeroLeft, HeroRight)
            addEvents(2, HeroUp, HeroDown, ActionSearch)
        }

        actionSets[3] = ActionSet(3).apply {
            addEvents(1, HeroLeft, ActionSearch)
            addEvents(2, HeroRight)
            addEvents(3, HeroUp, HeroDown)
        }

        actionSets[4] = ActionSet(4).apply {
            addEvents(1, HeroLeft, ActionSearch)
            addEvents(2, HeroRight)
            addEvents(3, HeroUp)
            addEvents(4, HeroDown)
        }

        actionSets[5] = ActionSet(5).apply {
            addEvents(1, HeroLeft)
            addEvents(2, HeroRight)
            addEvents(3, HeroUp)
            addEvents(4, HeroDown)
            addEvents(5, ActionSearch)
        }

    }

    fun getUndiscoveredById(nextRoomId: Int): Room? {
        val next = undiscoveredRooms.firstOrNull { it.id == nextRoomId }
        if (next != null) {
            undiscoveredRooms.remove(next)
        }
        return next
    }

    fun getUndiscoveredRoom(enterDirection: GameFlow.Direction): Room? {
        val next = undiscoveredRooms.firstOrNull { it.hasExit(enterDirection) }
        if (next != null) {
            undiscoveredRooms.remove(next)
        }
        return next
    }

    fun Team.load(players: Int, numberOfTeamMates: Int): Team {
        (1..players).forEach {
            val nextPlayer = Team.Hero(it)
            val allowedSets: MutableMap<Int, MutableSet<Action>>? = actionSets[numberOfTeamMates]?.allowed
            allowedSets?.get(it)?.let { allowedSet: MutableSet<Action> ->
                nextPlayer.possibleActions.addAll(allowedSet)
            }
            this.heroes.add(nextPlayer)
        }
        return this
    }

    /**
     * numberOfTeamMates : 1-5 players
     */
    fun createWorld(numberOfTeamMates: Int): World {
        val players = Team().load(4, numberOfTeamMates)
        players[1].pos(1, 1)
        players[2].pos(1, 2)
        players[3].pos(2, 1)
        players[4].pos(2, 2)

        val roomFactory = RoomFactory()
        for (roomNumber in 1 until 12) {
            undiscoveredRooms += roomFactory.createRoom(roomNumber)
        }
        undiscoveredRooms.shuffle()
        val start = roomFactory.createRoom(0)
        return World(mutableListOf(start), players, maxRooms, factory = this)
    }

    fun getActionSet(playersCount: Int): ActionSet = actionSets[playersCount] ?: actionSets[0]!!

    data class ActionSet(val players: Int) {

        /**
         *  player number -> List<InputEvent>
         */
        val allowed = mutableMapOf<Int, MutableSet<Action>>()

        fun getAllowedActions(playerNumber: Int): MutableSet<Action> {
            return allowed[playerNumber] ?: mutableSetOf()
        }

        fun addEvent(playerNumber: Int, event: Action) {
            val events = allowed.getOrPut(playerNumber, { mutableSetOf() })
            events.add(event)
        }

        fun addEvents(playerNumber: Int, vararg event: Action) {
            val events = allowed.getOrPut(playerNumber, { mutableSetOf() })
            events.addAll(event)
        }
    }
}