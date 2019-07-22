/*
 * Copyright 2016-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package benchmarks.immutableMap.builder

import benchmarks.*
import kotlinx.collections.immutable.PersistentMap
import org.openjdk.jmh.annotations.*

@State(Scope.Thread)
open class Remove {
    @Param(BM_1, BM_10, BM_100, BM_1000, BM_10000, BM_100000, BM_1000000)
    var size: Int = 0

    @Param(HASH_IMPL, ORDERED_IMPL)
    var implementation = ""

    @Param(ASCENDING_HASH_CODE, RANDOM_HASH_CODE, COLLISION_HASH_CODE, NON_EXISTING_HASH_CODE)
    var hashCodeType = ""

    @Param(IP_100, IP_99_09, IP_95, IP_70, IP_50, IP_30, IP_0)
    var immutablePercentage: Double = 0.0

    private var keys = listOf<IntWrapper>()
    private var keysToRemove = listOf<IntWrapper>()

    @Setup(Level.Trial)
    fun prepare() {
        keys = generateKeys(hashCodeType, size)

        keysToRemove = if (hashCodeType == NON_EXISTING_HASH_CODE) {
            generateKeys(hashCodeType, size)
        } else {
            keys
        }
    }

    /**
     * Adds [size] entries to an empty persistent map builder and then removes each entry by key.
     *
     * Expected time: [Put.put] + logarithmic
     * Expected memory: [Put.put] + logarithmic
     */
    // Q: Why not to benchmark pure remove method?
    // A: Each invocation of remove benchmark method would clear the builder and creating new one would be needed each time.
    // Setting `@Setup(Level.Invocation)` may cause bad benchmark accuracy amid frequent `prepare` calls, especially for small `size`.
    @Benchmark
    fun putAndRemove(): PersistentMap.Builder<IntWrapper, String> {
        val builder = persistentMapBuilderPut(implementation, keys, immutablePercentage)
        repeat(times = size) { index ->
            builder.remove(keysToRemove[index])
        }
        return builder
    }
}