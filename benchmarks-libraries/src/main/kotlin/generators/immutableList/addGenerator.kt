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

package generators.immutableList

import generators.BenchmarkSourceGenerator
import java.io.PrintWriter

interface ListAddBenchmark {
    val packageName: String
    fun listType(T: String): String
}

class ListAddBenchmarkGenerator(private val impl: ListAddBenchmark) : BenchmarkSourceGenerator() {
    override val benchmarkName: String = "Add"

    override fun getPackage(): String {
        return super.getPackage() + ".immutableList." + impl.packageName
    }

    override val imports: Set<String> = super.imports + "org.openjdk.jmh.infra.Blackhole"

    override fun generateBody(out: PrintWriter) {
        out.println("""
open class Add {
    @Param("10000", "100000")
    var size: Int = 0

    @Benchmark
    fun addLast(): ${impl.listType("String")} {
        return persistentListAdd(size)
    }

    @Benchmark
    fun addLastAndIterate(bh: Blackhole) {
        val list = persistentListAdd(size)
        for (e in list) {
            bh.consume(e)
        }
    }

    @Benchmark
    fun addLastAndGet(bh: Blackhole) {
        val list = persistentListAdd(size)
        for (i in 0 until list.size) {
            bh.consume(list[i])
        }
    }
}
        """.trimIndent()
        )
    }
}