package org.coner.drs.ui.changedriver

import javafx.geometry.Orientation
import javafx.scene.control.TextField
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.coner.drs.domain.entity.Registration
import org.coner.drs.ui.RegistrationCellFragment
import org.coner.drs.util.UpperCaseTextFormatter
import org.coner.drs.util.tornadofx.takeVerticalArrowKeyPressesAsSelectionsFrom
import tornadofx.*

class ChangeRunDriverFragment : Fragment("Change Run Driver") {

    override val scope = super.scope as ChangeRunDriverScope

    val model: ChangeRunDriverModel by inject()
    val controller: ChangeRunDriverController by inject()
    private var numbersTextField: TextField by singleAssign()

    override val root = form {
        prefWidth = 300.0
        fieldset(text = title, labelPosition = Orientation.VERTICAL) {
            field("Sequence", orientation = Orientation.VERTICAL) {
                textfield(model.run.sequenceProperty) {
                    isEditable = false
                }
            }
            field("_Numbers", orientation = Orientation.VERTICAL) {
                (inputContainer as VBox).spacing = 0.0
                textfield(model.numbersFieldProperty) {
                    numbersTextField = this
                    label.labelFor = this
                    label.isMnemonicParsing = true
                    required()
                    textFormatter = UpperCaseTextFormatter()
                }
                listview<Registration> {
                    id = "registrations-list-view"
                    vgrow = Priority.ALWAYS
                    model.registrationList.bindTo(this)
                    cellFragment(RegistrationCellFragment::class)
                    bindSelected(model.registrationListSelectionProperty)
                    takeVerticalArrowKeyPressesAsSelectionsFrom(numbersTextField)
                    model.registrationListAutoSelectionCandidateProperty.onChange {
                        runLater {
                            selectionModel.select(it?.registration)
                            scrollTo(it?.registration)
                        }
                    }
                }
            }
            field {
                hbox(spacing = 10) {
                    pane {
                        hgrow = Priority.ALWAYS
                    }
                    button("Cancel") {
                        action {
                            close()
                        }
                    }
                    button("Clear") {
                        action {
                            controller.clearDriver()
                            close()
                        }
                    }
                    splitmenubutton("Change") {
                        action {
                            controller.changeDriverFromRegistrationListSelection()
                            close()
                        }
                        shortcut("Enter") {
                            if (model.registrationListSelection == null) return@shortcut
                            controller.changeDriverFromRegistrationListSelection()
                            close()
                        }
                        enableWhen(
                                model.numbersFieldContainsNumbersTokensBinding
                                        .or(model.registrationListSelectionProperty.isNotNull)
                        )
                        item(name = "Force Exact Numbers", keyCombination = KeyCombination.keyCombination("Ctrl+Enter")) {
                            enableWhen(model.numbersFieldContainsNumbersTokensBinding)
                            action {
                                controller.changeDriverFromExactNumbers()
                                close()
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        controller.init()
    }

    override fun onDock() {
        super.onDock()
        runLater { numbersTextField.requestFocus() }
    }

}