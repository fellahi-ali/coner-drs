package org.coner.drs.ui.runevent

import javafx.collections.transformation.SortedList
import org.coner.drs.domain.entity.Run
import org.coner.drs.domain.service.RunService
import org.coner.drs.ui.changedriver.ChangeRunDriverFragment
import org.coner.drs.ui.changedriver.ChangeRunDriverScope
import tornadofx.*

class RunEventTableController : Controller() {
    val model: RunEventTableModel by inject()
    val controller: RunEventController by inject()
    val runService: RunService by inject()

    fun init() {
        model.runsSortedBySequence = SortedList(controller.model.event.runs, compareBy(Run::sequence))
    }

    fun incrementCones(run: Run) {
        runService.incrementCones(run)
    }

    fun decrementCones(run: Run) {
        runService.decrementCones(run)
    }

    fun changeDidNotFinish(run: Run, newValue: Boolean) {
        runService.changeDidNotFinish(run, newValue)
    }

    fun changeRerun(run: Run, newValue: Boolean) {
        runService.changeRerun(run, newValue)
    }

    fun changeDisqualified(run: Run, newValue: Boolean) {
        runService.changeDisqualified(run, newValue)
    }

    fun showChangeDriver(run: Run) {
        val runEventModel: RunEventModel = find()
        val scope = ChangeRunDriverScope(
                runEventScope = scope,
                run = run,
                registrations = runEventModel.event.registrations
        )
        find<ChangeRunDriverFragment>(scope).openModal()
    }

}