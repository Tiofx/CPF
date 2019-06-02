package algorithm


class Lab4(val controlGraph: ControlGraph) {
    val s = DirectConnectionsMatrix(controlGraph)
    val st = TransitiveConnectionMatrix(s)
    val mlo = MLO(s)
    val l = LogicalIncompatibilityMatrix(s,mlo)
    val lt = TransitiveLogicalIncompatibilityMatrix(st, l)
    val m = IndependecyMatrix(st, lt)
    val cmioList = CompleteMutuallyIndependentOperators(m)
}