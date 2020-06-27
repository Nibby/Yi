package codes.nibby.yi.common.payload

open class SubmitMoveParameters(val x: Int, val y: Int) : Parameters {
    var ignoreRules: Boolean = false

    constructor(x: Int, y: Int, ignoreRules: Boolean) : this(x, y) {
        this.ignoreRules = ignoreRules
    }

}
