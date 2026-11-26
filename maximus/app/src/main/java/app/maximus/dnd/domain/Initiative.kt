package app.maximus.dnd.domain

data class Combatant(
    val id: String,
    val name: String,
    val initiative: Int,
    val maxHp: Int? = null,
    val hp: Int? = maxHp
) {
    fun changeHp(delta: Int): Combatant {
        val maximum = maxHp ?: return this
        return copy(hp = ((hp ?: maximum).toLong() + delta).coerceIn(0L, maximum.toLong()).toInt())
    }
}

data class Initiative(
    val combatants: List<Combatant> = emptyList(),
    val round: Int = 1,
    val activeId: String? = null
) {
    // Stable sort keeps the order of entry on a tie. The table can select a different active turn.
    val ordered: List<Combatant> get() = combatants.sortedByDescending { it.initiative }
    val active: Combatant? get() = combatants.firstOrNull { it.id == activeId }

    fun add(combatant: Combatant): Initiative = copy(combatants = combatants + combatant)

    fun change(id: String, transform: (Combatant) -> Combatant): Initiative =
        copy(combatants = combatants.map { if (it.id == id) transform(it) else it })

    fun select(id: String): Initiative =
        if (combatants.any { it.id == id }) copy(activeId = id) else this

    fun next(): Initiative {
        val sorted = ordered
        if (sorted.isEmpty()) return this
        val index = sorted.indexOfFirst { it.id == activeId }
        return if (index == sorted.lastIndex) copy(round = round + 1, activeId = sorted.first().id)
        else copy(activeId = sorted[index + 1].id)
    }

    fun previous(): Initiative {
        val sorted = ordered
        val index = sorted.indexOfFirst { it.id == activeId }
        return when {
            index < 0 -> this
            index > 0 -> copy(activeId = sorted[index - 1].id)
            round > 1 -> copy(round = round - 1, activeId = sorted.last().id)
            else -> this
        }
    }

    fun remove(id: String): Initiative {
        val remaining = combatants.filterNot { it.id == id }
        if (remaining.isEmpty()) return Initiative()
        val nextTurn = if (id == activeId) next() else this
        return nextTurn.copy(combatants = remaining)
    }
}
