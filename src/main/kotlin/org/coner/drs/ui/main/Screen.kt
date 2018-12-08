package org.coner.drs.ui.main

import org.coner.drs.Event
import java.io.File

sealed class Screen {
    object Start : Screen()
    class ChooseEvent(val dir: File) : Screen()
    class RunEvent(val event: Event) : Screen()
}