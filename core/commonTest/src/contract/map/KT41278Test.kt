/*
 * Copyright 2016-2021 JetBrains s.r.o.
 * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 */

package tests.contract.map

import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.persistentMapOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KT41278Test {
    // Based on https://youtrack.jetbrains.com/issue/KT-42428.
    private fun doTest(map: Map<String, Int>, key: String, value: Int, createEntry: (String, Int) -> Map.Entry<String, Int>) {
        assertTrue(map.keys.contains(key))
        assertEquals(value, map[key])
        // This one requires special efforts to make it work this way.
        // map.entries can in fact be `MutableSet<MutableMap.MutableEntry>`,
        // which [contains] method takes [MutableEntry], so the compiler may generate special bridge
        // returning false for values that aren't [MutableEntry].
        assertTrue(map.entries.contains(createEntry(key, value)))
        assertTrue(map.entries.toSet().contains(createEntry(key, value)))

        assertFalse(map.entries.contains(null as Any?))
        assertFalse(map.entries.contains("not an entry" as Any?))
    }

    @Test
    fun persistentOrderedMap() {
        val mapLetterToIndex = ('a'..'z').mapIndexed { i, c -> "$c" to i }.fold(persistentMapOf<String, Int>()) { map, pair ->
            map.put(pair.first, pair.second)
        }

        doTest(mapLetterToIndex, "h", 7, ::TestMapEntry)
        doTest(mapLetterToIndex, "h", 7, ::TestMutableMapEntry)
    }

    @Test
    fun persistentHashMap() {
        val mapLetterToIndex = ('a'..'z').mapIndexed { i, c -> "$c" to i }.fold(persistentHashMapOf<String, Int>()) { map, pair ->
            map.put(pair.first, pair.second)
        }

        doTest(mapLetterToIndex, "h", 7, ::TestMapEntry)
        doTest(mapLetterToIndex, "h", 7, ::TestMutableMapEntry)
    }

    @Test
    fun persistentOrderedMapBuilder() {
        val mapLetterToIndex = persistentMapOf<String, Int>().builder().apply { putAll(('a'..'z').mapIndexed { i, c -> "$c" to i }) }

        doTest(mapLetterToIndex, "h", 7, ::TestMapEntry)
        doTest(mapLetterToIndex, "h", 7, ::TestMutableMapEntry)
    }

    @Test
    fun persistentHashMapBuilder() {
        val mapLetterToIndex = persistentHashMapOf<String, Int>().builder().apply { putAll(('a'..'z').mapIndexed { i, c -> "$c" to i }) }

        doTest(mapLetterToIndex, "h", 7, ::TestMapEntry)
        doTest(mapLetterToIndex, "h", 7, ::TestMutableMapEntry)
    }
}

private class TestMapEntry<out K, out V>(override val key: K, override val value: V) : Map.Entry<K, V> {
    override fun toString(): String = "$key=$value"
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun equals(other: Any?): Boolean =
            other is Map.Entry<*, *> && key == other.key && value == other.value
}

private class TestMutableMapEntry<K, V>(override val key: K, override val value: V) : MutableMap.MutableEntry<K, V> {
    override fun toString(): String = "$key=$value"
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun equals(other: Any?): Boolean =
            other is Map.Entry<*, *> && key == other.key && value == other.value

    override fun setValue(newValue: V): V = TODO("Not yet implemented")
}
