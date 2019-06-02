import algorithm.*


fun Lab4.toReadable(): String = """
-- Матрица непосредственных связей:
${s.toString2()}
-------------------------------------------

-- Матрица транзитивных связей:
${st.toString2()}
-------------------------------------------

-- MLO:
${mlo.list.joinToString()}
-------------------------------------------

-- Матрица непосредсвенной логической несовместимости операторов:
${l.toString1()}
-------------------------------------------

-- Матрица транзитивных несовместимости операторов:
${lt.toString1()}
-------------------------------------------

-- Матрица независимостей операторов:
${m.toString1()}
-------------------------------------------

-- ВНО:
${cmioList.list.toReadable()}
-------------------------------------------
""".trimIndent()


fun Matrix<Boolean>.toString1() =
    (1..size).joinToString("\n") { i ->
        (1..size).map { j -> get(i, j) }.joinToString(" ") { if (it) "1" else "0" }
    }

fun Matrix<GraphElement>.toString2(): String {
    val maxLength = (1..size).map { i ->
        (1..size)
            .map { j -> get(j, i) }
            .map { it.toReadable().length }
            .max()!!
    }

    return (1..size).joinToString("\n") { i ->
        (1..size).map { j -> get(i, j) }
            .map { it.toReadable() }
            .mapIndexed { j, s -> String.format("%${maxLength[j]}s", s) }
            .joinToString(" ")
    }
}


fun GraphElement.toReadable() = when (this) {
    NoConnection -> "0"
    InformationConnection -> "1"
    is LogicConnection -> elementConnection
    is LogicComposition -> content
}


private fun List<Set<Int>>.toReadable() = joinToString { it.toReadable() }
private fun Set<Int>.toReadable() = joinToString(prefix = "{", postfix = "}")

