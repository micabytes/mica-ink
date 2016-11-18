package com.micabytes.ink

interface Function {
    val numParams: Int
    val isFixedNumParams: Boolean
    /**
     * Evaluation of the function

     * @param params Parameters will be passed by the expression evaluator as a [List] of [Object] values.
     * *
     * @param vmap
     * *
     * @return The function must return a new [Object] value as a computing result.
     */
    @Throws(InkRunTimeException::class)
    fun eval(params: List<Any>, vmap: VariableMap): Any

}
