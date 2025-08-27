package fr.qg.workbench.models

class CraftRequirement(
    val name: String,
    val type: Type,
    val value1: String,
    val value2: String
) {
    enum class Type(
        val token: String,
        val num: (Double, Double) -> Boolean,
        val str: (String, String) -> Boolean
    ) {
        GREATER_EQUAL(">=", { a, b -> a >= b }, { _, _ -> false }),
        EQUAL("==", { a, b -> a == b }, { a, b -> a.equals(b, true) }),
        NOT_EQUAL("!=", { a, b -> a != b }, { a, b -> !a.equals(b, true) }),
        GREATER(">", { a, b -> a > b }, { _, _ -> false }),
        LESS("<", { a, b -> a < b }, { _, _ -> false }),
        LESS_EQUAL("<=", { a, b -> a <= b }, { _, _ -> false })
    }
}
