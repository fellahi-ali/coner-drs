package org.coner.drs.ui.run_event

import javafx.geometry.Side
import tornadofx.*

class RunEventRightDrawerView : View() {
    override val root = drawer(side = Side.RIGHT) {
        item<TimerView>(showHeader = true)
    }
}